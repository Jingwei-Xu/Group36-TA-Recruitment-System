package edu.ebu6304.app.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.ebu6304.app.model.ApplicationRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DataImportService {

    public List<ApplicationRecord> loadApplications(String dataRootPath) {
        Path applicationsDir = Paths.get(dataRootPath).resolve("applications");
        if (!Files.exists(applicationsDir) || !Files.isDirectory(applicationsDir)) {
            throw new IllegalArgumentException("applications folder does not exist: " + applicationsDir);
        }

        try {
            return Files.list(applicationsDir)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .map(this::readApplication)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read applications folder", e);
        }
    }

    public void persistReviewDecision(ApplicationRecord record, String decision, String notes, String reviewerId) {
        if (record.getSourceFilePath() == null || record.getSourceFilePath().isBlank()) {
            throw new IllegalArgumentException("Missing source file path for application: " + record.getApplicationId());
        }

        Path file = Paths.get(record.getSourceFilePath());
        try {
            String raw = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(raw).getAsJsonObject();

            JsonObject review = root.has("review") && root.get("review").isJsonObject()
                    ? root.getAsJsonObject("review")
                    : new JsonObject();

            String now = LocalDateTime.now().toString();
            String decisionLabel = mapDecisionLabel(decision);

            review.addProperty("decision", decision);
            review.addProperty("decisionReason", notes == null ? "" : notes);
            review.addProperty("reviewerNotes", notes == null ? "" : notes);
            review.addProperty("statusMessage", "Reviewed: " + decisionLabel);
            review.addProperty("nextSteps", decision.equals("accepted") ? "Proceed to allocation." : "Application closed.");
            review.addProperty("reviewedBy", reviewerId == null || reviewerId.isBlank() ? "u_mo_001" : reviewerId);
            review.addProperty("reviewedAt", now);

            root.add("review", review);

            JsonObject status = root.has("status") && root.get("status").isJsonObject()
                    ? root.getAsJsonObject("status")
                    : new JsonObject();
            status.addProperty("current", decision);
            status.addProperty("label", decisionLabel);
            status.addProperty("lastUpdated", now);
            status.addProperty("updatedBy", reviewerId == null || reviewerId.isBlank() ? "u_mo_001" : reviewerId);
            root.add("status", status);

            JsonObject meta = root.has("meta") && root.get("meta").isJsonObject()
                    ? root.getAsJsonObject("meta")
                    : new JsonObject();
            if (!meta.has("submittedAt")) {
                meta.addProperty("submittedAt", now);
            }
            meta.addProperty("updatedAt", now);
            if (!meta.has("isDeleted")) {
                meta.addProperty("isDeleted", false);
            }
            root.add("meta", meta);

            appendTimeline(root, decision, reviewerId, now);

            String output = root.toString();
            Files.writeString(file, output, StandardCharsets.UTF_8);

            record.setReviewDecision(decision);
            record.setReviewNotes(notes);
            record.setReviewDecisionReason(notes);
            record.setReviewedBy(reviewerId == null || reviewerId.isBlank() ? "u_mo_001" : reviewerId);
            record.setReviewedAt(now);
            record.setStatusCurrent(decision);
            record.setStatusLabel(decisionLabel);
            record.setRawJson(output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write review decision for " + record.getApplicationId(), e);
        }
    }

    private void appendTimeline(JsonObject root, String decision, String reviewerId, String now) {
        JsonArray timeline;
        if (root.has("timeline") && root.get("timeline").isJsonArray()) {
            timeline = root.getAsJsonArray("timeline");
        } else {
            timeline = new JsonArray();
        }

        JsonObject entry = new JsonObject();
        entry.addProperty("timelineId", "tl_" + now.replaceAll("[-:.T]", "").substring(0, 14));
        entry.addProperty("stepKey", decision);
        entry.addProperty("stepLabel", mapDecisionLabel(decision));
        entry.addProperty("status", "completed");
        entry.addProperty("timestamp", now);
        entry.addProperty("updatedBy", reviewerId == null || reviewerId.isBlank() ? "u_mo_001" : reviewerId);
        entry.addProperty("note", "Application reviewed with decision: " + mapDecisionLabel(decision));

        timeline.add(entry);
        root.add("timeline", timeline);
    }

    private ApplicationRecord readApplication(Path path) {
        try {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(raw).getAsJsonObject();
            ApplicationRecord record = new ApplicationRecord();

            record.setApplicationId(stringOf(obj, "applicationId"));
            record.setStudentId(stringOf(obj, "studentId"));
            record.setUserId(stringOf(obj, "userId"));

            JsonObject applicantSnapshot = objectOf(obj, "applicantSnapshot");
            record.setApplicantName(stringOf(applicantSnapshot, "fullName"));
            record.setApplicantEmail(stringOf(applicantSnapshot, "email"));
            record.setApplicantPhone(stringOf(applicantSnapshot, "phoneNumber"));
            record.setApplicantMajor(stringOf(applicantSnapshot, "programMajor"));
            record.setApplicantYear(stringOf(applicantSnapshot, "year"));
            record.setApplicantGpa(doubleOf(applicantSnapshot, "gpa"));

            JsonObject jobSnapshot = objectOf(obj, "jobSnapshot");
            record.setJobId(stringOf(obj, "jobId"));
            record.setJobTitle(stringOf(jobSnapshot, "title"));
            record.setCourseCode(stringOf(jobSnapshot, "courseCode"));
            record.setCourseName(stringOf(jobSnapshot, "courseName"));
            record.setDepartment(stringOf(jobSnapshot, "department"));
            record.setInstructorName(stringOf(jobSnapshot, "instructorName"));
            record.setWeeklyHours(intOf(jobSnapshot, "weeklyHours"));

            JsonObject status = objectOf(obj, "status");
            record.setStatusCurrent(stringOf(status, "current"));
            record.setStatusLabel(stringOf(status, "label"));

            JsonObject form = objectOf(obj, "applicationForm");
            record.setRelevantSkills(arrayOfStrings(form, "relevantSkills"));
            record.setRelevantExperience(stringOf(form, "relevantExperience"));
            record.setAvailability(stringOf(form, "availability"));
            record.setMotivationCoverLetter(stringOf(form, "motivationCoverLetter"));

            JsonObject review = objectOf(obj, "review");
            record.setReviewDecision(stringOf(review, "decision"));
            record.setReviewNotes(stringOf(review, "reviewerNotes"));
            record.setReviewDecisionReason(stringOf(review, "decisionReason"));
            record.setReviewedBy(stringOf(review, "reviewedBy"));
            record.setReviewedAt(stringOf(review, "reviewedAt"));

            record.setSourceFilePath(path.toString());
            record.setRawJson(raw);
            return record;
        } catch (IOException e) {
            throw new RuntimeException("Failed reading application json: " + path, e);
        }
    }

    private String mapDecisionLabel(String decision) {
        if ("accepted".equalsIgnoreCase(decision)) {
            return "Accepted";
        }
        if ("rejected".equalsIgnoreCase(decision)) {
            return "Rejected";
        }
        return decision;
    }

    private JsonObject objectOf(JsonObject parent, String key) {
        if (parent == null || !parent.has(key) || !parent.get(key).isJsonObject()) {
            return new JsonObject();
        }
        return parent.getAsJsonObject(key);
    }

    private String stringOf(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return "";
        }
        JsonElement e = obj.get(key);
        if (e == null || e.isJsonNull()) {
            return "";
        }
        try {
            return e.getAsString();
        } catch (Exception ex) {
            return "";
        }
    }

    private Integer intOf(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception ex) {
            return null;
        }
    }

    private Double doubleOf(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> arrayOfStrings(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonArray()) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        JsonArray arr = obj.getAsJsonArray(key);
        for (JsonElement e : arr) {
            if (e != null && !e.isJsonNull()) {
                list.add(Optional.ofNullable(e.getAsString()).orElse(""));
            }
        }
        return list;
    }
}
