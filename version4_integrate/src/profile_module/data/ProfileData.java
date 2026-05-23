package profile_module.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProfileData {
    public String fullName = "";
    public String studentId = "";
    public String year = "";
    public String programMajor = "";
    public String email = "";
    public String phoneNumber = "";

    public String address = "";
    public String shortBio = "";

    public List<SkillItem> skills = new ArrayList<>();

    public CvInfo cv = new CvInfo();

    public int profileCompletionPercent = 0;
    public int numberOfApplications = 0;

    public static ProfileData demo() {
        ProfileData d = new ProfileData();
        d.fullName = "John Smith";
        d.studentId = "20230001";
        d.year = "3rd Year";
        d.programMajor = "Computer Science";
        d.email = "john.smith@university.edu";
        d.phoneNumber = "(555) 123-4567";
        d.address = "123 Main St, City, State, ZIP";
        d.shortBio = "Interested in teaching and software engineering.";
        d.skills = new ArrayList<>();
        d.skills.add(new SkillItem("Python", "Programming", "Advanced"));
        d.skills.add(new SkillItem("JavaScript", "Programming", "Intermediate"));
        d.skills.add(new SkillItem("Java", "Programming", "Intermediate"));
        d.skills.add(new SkillItem("C++", "Programming", "Beginner"));
        d.skills.add(new SkillItem("Tutoring", "Teaching / Tutoring", "Advanced"));
        d.skills.add(new SkillItem("Lab Instruction", "Teaching / Tutoring", "Intermediate"));
        d.skills.add(new SkillItem("Grading", "Teaching / Tutoring", "Advanced"));
        d.skills.add(new SkillItem("English (Native)", "Communication", "Advanced"));
        d.skills.add(new SkillItem("Spanish (Intermediate)", "Communication", "Intermediate"));
        d.cv = new CvInfo();
        d.cv.fileName = "John_Smith_CV.pdf";
        d.cv.status = "Uploaded";
        d.cv.sizeLabel = "245 KB";
        d.cv.lastUpdated = LocalDate.now().minusDays(15).toString();
        d.profileCompletionPercent = 85;
        d.numberOfApplications = 3;
        return d;
    }

    /** Each of basic info, skills, and CV contributes one third; total is rounded to nearest integer. */
    private static final double COMPLETION_SEGMENT = 100.0 / 3.0;

    public void recomputeCompletion() {
        double score = 0.0;
        if (isBasicInfoComplete()) score += COMPLETION_SEGMENT;
        if (skills != null && !skills.isEmpty()) score += COMPLETION_SEGMENT;
        if (cv != null && !blank(cv.fileName)) score += COMPLETION_SEGMENT;
        profileCompletionPercent = (int) Math.round(score);
    }

    /** Required personal-info fields (Edit Profile / onboarding step 1). */
    private boolean isBasicInfoComplete() {
        return !blank(fullName)
                && !blank(studentId)
                && !blank(year)
                && !"Select year".equalsIgnoreCase(year.trim())
                && !blank(programMajor)
                && !blank(email)
                && !blank(phoneNumber);
    }

    private static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public SkillItem addSkill(String name, String category, String proficiency) {
        SkillItem item = new SkillItem(
                Objects.requireNonNullElse(name, "").trim(),
                Objects.requireNonNullElse(category, "").trim(),
                Objects.requireNonNullElse(proficiency, "").trim()
        );
        if (skills == null) skills = new ArrayList<>();
        skills.add(item);
        return item;
    }
}

