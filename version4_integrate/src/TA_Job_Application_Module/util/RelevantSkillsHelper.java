package TA_Job_Application_Module.util;

import TA_Job_Application_Module.model.Application;
import TA_Job_Application_Module.model.TAUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats and parses relevant skills for TA job applications (name + proficiency).
 */
public final class RelevantSkillsHelper {

    private static final Pattern LEVEL_SUFFIX = Pattern.compile(
            "\\s*\\(\\s*(Beginner|Intermediate|Advanced)\\s*\\)\\s*$",
            Pattern.CASE_INSENSITIVE);

    private RelevantSkillsHelper() {
    }

    public static List<Application.RelevantSkill> fromUser(TAUser user) {
        List<Application.RelevantSkill> result = new ArrayList<>();
        if (user == null || user.getSkills() == null) {
            return result;
        }
        List<TAUser.Skill> selected = user.getSkills().getSelectedSkills();
        if (selected == null) {
            return result;
        }
        for (TAUser.Skill sk : selected) {
            if (sk == null || sk.getName() == null || sk.getName().trim().isEmpty()) {
                continue;
            }
            Application.RelevantSkill rs = new Application.RelevantSkill();
            rs.setName(sk.getName().trim());
            String prof = sk.getProficiency();
            if (prof != null && !prof.trim().isEmpty()) {
                rs.setProficiency(prof.trim());
            }
            result.add(rs);
        }
        return result;
    }

    public static String formatDisplay(Application.RelevantSkill skill) {
        if (skill == null || skill.getName() == null || skill.getName().trim().isEmpty()) {
            return "";
        }
        String name = skill.getName().trim();
        String prof = skill.getProficiency();
        if (prof != null && !prof.trim().isEmpty()) {
            return name + " (" + prof.trim() + ")";
        }
        return name;
    }

    public static String formatSkillsText(List<Application.RelevantSkill> skills) {
        if (skills == null || skills.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Application.RelevantSkill sk : skills) {
            String line = formatDisplay(sk);
            if (!line.isEmpty()) {
                parts.add(line);
            }
        }
        return String.join(", ", parts);
    }

    public static String formatSkillsText(TAUser user) {
        return formatSkillsText(fromUser(user));
    }

    /**
     * Parses comma-separated skills text, e.g. "Java (Beginner), Python".
     */
    public static List<Application.RelevantSkill> parseSkillsText(String text) {
        List<Application.RelevantSkill> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }
        for (String part : text.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            result.add(parseSingle(trimmed));
        }
        return result;
    }

    public static Application.RelevantSkill parseSingle(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new Application.RelevantSkill("", null);
        }
        String trimmed = raw.trim();
        Matcher m = LEVEL_SUFFIX.matcher(trimmed);
        if (m.find()) {
            String name = trimmed.substring(0, m.start()).trim();
            String level = m.group(1);
            if (!level.isEmpty()) {
                level = level.substring(0, 1).toUpperCase() + level.substring(1).toLowerCase();
            }
            return new Application.RelevantSkill(name, level);
        }
        return new Application.RelevantSkill(trimmed, null);
    }

    /** Skill name only, for fuzzy matching against job requirements. */
    public static String nameForMatching(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        Matcher m = LEVEL_SUFFIX.matcher(raw.trim());
        if (m.find()) {
            return raw.substring(0, m.start()).trim().toLowerCase();
        }
        return raw.trim().toLowerCase();
    }

    public static String nameForMatching(Application.RelevantSkill skill) {
        if (skill == null) {
            return "";
        }
        if (skill.getName() != null && !skill.getName().isBlank()) {
            return skill.getName().trim().toLowerCase();
        }
        return nameForMatching(formatDisplay(skill));
    }
}
