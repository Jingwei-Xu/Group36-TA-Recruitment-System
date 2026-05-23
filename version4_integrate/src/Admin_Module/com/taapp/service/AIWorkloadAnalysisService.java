package Admin_Module.com.taapp.service;

import Admin_Module.com.taapp.data.DataStore;
import Admin_Module.com.taapp.model.TA;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI workload analysis for Admin dashboard.
 * Uses OpenAI-compatible endpoint if configured; otherwise falls back to deterministic local analysis.
 */
public class AIWorkloadAnalysisService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEFAULT_ARK_ENDPOINT = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String DEFAULT_ARK_MODEL = "doubao-seed-1-8-251228";
    private static final String DEFAULT_ADMIN_ARK_API_KEY = "ark-3f55124e-4818-4a82-a89f-d567f01535fd-81f95";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    public record AnalysisResult(
            int systemHealthScore,
            int workloadBalanceScore,
            int resourceUtilizationPercent,
            String healthSummary,
            String workloadSummary,
            String utilizationSummary,
            String recommendation,
            String analysisSource
    ) {
    }

    public AnalysisResult analyze() {
        List<TA> tas = DataStore.defaultStore().getTAs();
        AnalysisResult base = localAnalyze(tas);

        ResolvedAiConfig cfg = resolveAiConfig();
        if (cfg == null) {
            return withSource(base, "Local");
        }

        try {
            return aiEnhance(base, tas, cfg);
        } catch (Exception ignored) {
            return withSource(base, "Local");
        }
    }

    private AnalysisResult aiEnhance(AnalysisResult base, List<TA> tas, ResolvedAiConfig cfg) throws IOException, InterruptedException {
        ObjectNode body = MAPPER.createObjectNode();
        body.put("model", cfg.model());
        body.put("temperature", 0.2);
        body.put("max_tokens", 260);

        var messages = body.putArray("messages");
        messages.addObject().put("role", "system").put("content",
                "You are an assistant for TA operations. Return STRICT JSON only with keys: healthSummary, workloadSummary, utilizationSummary, recommendation. " +
                        "Use neutral, factual language. Avoid subjective words such as perfect, excellent, optimal, outstanding, terrible. " +
                        "Use one short sentence per field. Keep each field under 90 chars.");
        messages.addObject().put("role", "user").put("content", buildPrompt(base, tas));

        HttpRequest req = HttpRequest.newBuilder(URI.create(cfg.endpoint()))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + cfg.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            return base;
        }

        JsonNode root = MAPPER.readTree(resp.body());
        String content = root.path("choices").path(0).path("message").path("content").asText("").trim();
        if (content.isBlank()) {
            return base;
        }
        JsonNode json = MAPPER.readTree(content);

        return new AnalysisResult(
                base.systemHealthScore,
                base.workloadBalanceScore,
                base.resourceUtilizationPercent,
                textOrDefault(json, "healthSummary", base.healthSummary),
                textOrDefault(json, "workloadSummary", base.workloadSummary),
                textOrDefault(json, "utilizationSummary", base.utilizationSummary),
                textOrDefault(json, "recommendation", base.recommendation),
                cfg.source()
        );
    }

    private record ResolvedAiConfig(String apiKey, String endpoint, String model, String source) {
    }

    private static ResolvedAiConfig resolveAiConfig() {
        String arkKey = firstNonBlank(
                DEFAULT_ADMIN_ARK_API_KEY,
                env("ADMIN_ARK_API_KEY"),
                env("MO_ARK_API_KEY"),
                env("ARK_API_KEY")
        );
        if (!isUsableApiKey(arkKey)) {
            String openAiKey = env("OPENAI_API_KEY");
            String openAiEndpoint = env("OPENAI_BASE_URL");
            String openAiModel = env("OPENAI_MODEL").isBlank() ? "gpt-4o-mini" : env("OPENAI_MODEL");
            if (isUsableApiKey(openAiKey) && !openAiEndpoint.isBlank()) {
                return new ResolvedAiConfig(openAiKey, openAiEndpoint, openAiModel, "OpenAI-Compatible");
            }
            return null;
        }

        String arkEndpoint = firstNonBlank(env("ADMIN_ARK_BASE_URL"), env("ARK_BASE_URL"), DEFAULT_ARK_ENDPOINT);
        String arkModel = firstNonBlank(env("ADMIN_ARK_MODEL"), env("ARK_MODEL"), DEFAULT_ARK_MODEL);
        return new ResolvedAiConfig(arkKey, arkEndpoint, arkModel, "Doubao");
    }

    private static boolean isUsableApiKey(String key) {
        return key != null && !key.isBlank() && !"your_api_key_here".equalsIgnoreCase(key.trim());
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String textOrDefault(JsonNode n, String key, String d) {
        String v = n.path(key).asText("").trim();
        return v.isBlank() ? d : v;
    }

    private static String buildPrompt(AnalysisResult base, List<TA> tas) {
        long active = tas.stream().filter(t -> "active".equalsIgnoreCase(t.getStatus())).count();
        int totalHours = tas.stream().mapToInt(TA::getTotalWorkload).sum();
        return "Given this data, write concise executive summaries. " +
                "Scores: health=" + base.systemHealthScore + ", balance=" + base.workloadBalanceScore + ", utilization=" + base.resourceUtilizationPercent + "%. " +
                "TA total=" + tas.size() + ", active=" + active + ", totalHours=" + totalHours + ".";
    }

    private AnalysisResult localAnalyze(List<TA> tas) {
        if (tas == null || tas.isEmpty()) {
            return new AnalysisResult(60, 60, 0,
                    "No TA data found yet",
                    "Upload or assign workload to compute balance",
                    "No utilization data available",
                    "Start by assigning positions and workloads to TAs.",
                    "Local");
        }

        int n = tas.size();
        int totalHours = tas.stream().mapToInt(TA::getTotalWorkload).sum();
        double avg = totalHours / (double) n;

        double variance = tas.stream()
                .mapToDouble(t -> Math.pow(t.getTotalWorkload() - avg, 2))
                .sum() / n;
        double stdDev = Math.sqrt(variance);

        int heavy = (int) tas.stream().filter(t -> t.getTotalWorkload() > avg * 1.35).count();
        int idle = (int) tas.stream().filter(t -> t.getTotalWorkload() < Math.max(2, avg * 0.4)).count();
        int active = (int) tas.stream().filter(t -> "active".equalsIgnoreCase(t.getStatus())).count();

        int utilization = clamp((int) Math.round((avg / 8.0) * 100), 15, 100);
        int balance = clamp(100 - (int) Math.round((stdDev / Math.max(1, avg)) * 65) - heavy * 4, 0, 100);
        int health = clamp((int) Math.round(balance * 0.45 + utilization * 0.35 + ((active * 100.0 / n) * 0.20)), 0, 100);

        String healthText = health >= 85 ? "Current indicators show high operational stability."
                : health >= 70 ? "Current indicators show generally stable operations with optimization opportunities."
                : "Current indicators show operational pressure requiring follow-up review.";
        String workloadText = heavy > 0 ? (heavy + " TAs are above the workload threshold; rebalancing is recommended.")
                : (idle > 0 ? "Some TAs are below baseline utilization; redistribution can be considered."
                : "Workload distribution is within expected range.");
        String utilText = utilization >= 80 ? "Utilization is at a high level relative to baseline capacity."
                : utilization >= 60 ? "Utilization is at a moderate level relative to baseline capacity."
                : "Utilization is at a low level relative to baseline capacity.";
        String reco = "Reassign high-load courses, use " + String.format(Locale.ROOT, "%.0f", avg * 1.3)
                + "h as an overload reference, and route pending tasks to lower-load TAs.";

        return new AnalysisResult(health, balance, utilization, healthText, workloadText, utilText, reco, "Local");
    }

    private static AnalysisResult withSource(AnalysisResult r, String source) {
        return new AnalysisResult(
                r.systemHealthScore(),
                r.workloadBalanceScore(),
                r.resourceUtilizationPercent(),
                r.healthSummary(),
                r.workloadSummary(),
                r.utilizationSummary(),
                r.recommendation(),
                source
        );
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String env(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        String fallback = loadDotEnvValues().get(key);
        return fallback == null ? "" : fallback.trim();
    }

    private static Map<String, String> loadDotEnvValues() {
        Path userDir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path[] candidates = new Path[] {
                userDir.resolve(".vscode").resolve(".env.local"),
                userDir.resolve(".env.local"),
                userDir.resolve(".env"),
                userDir.resolve("Admin_Module").resolve(".vscode").resolve(".env.local"),
                userDir.resolve("Admin_Module").resolve(".env.local"),
                userDir.resolve("Admin_Module").resolve(".env")
        };

        for (Path p : candidates) {
            try {
                if (!Files.isRegularFile(p)) {
                    continue;
                }
                return parseDotEnv(Files.readAllLines(p));
            } catch (Exception ignored) {
                // try next
            }
        }
        return Map.of();
    }

    private static Map<String, String> parseDotEnv(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (String raw : lines) {
            if (raw == null) {
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String k = line.substring(0, idx).trim();
            String v = line.substring(idx + 1).trim();
            if (v.length() >= 2 && ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'")))) {
                v = v.substring(1, v.length() - 1);
            }
            if (!k.isEmpty()) {
                values.put(k, v);
            }
        }
        return values;
    }
}
