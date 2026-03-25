package edu.ebu6304.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplicationRecord {
    private String applicationId;
    private String studentId;
    private String userId;

    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String applicantMajor;
    private String applicantYear;
    private Double applicantGpa;

    private String jobId;
    private String jobTitle;
    private String courseCode;
    private String courseName;
    private String department;
    private String instructorName;
    private Integer weeklyHours;

    private String statusCurrent;
    private String statusLabel;

    private List<String> relevantSkills = new ArrayList<>();
    private String relevantExperience;
    private String availability;
    private String motivationCoverLetter;

    private String reviewDecision;
    private String reviewNotes;
    private String reviewDecisionReason;
    private String reviewedBy;
    private String reviewedAt;

    private String sourceFilePath;
    private String rawJson;

    public boolean isReviewed() {
        return reviewDecision != null && !reviewDecision.isBlank();
    }

    public String normalizedStatus() {
        if (isReviewed()) {
            return "reviewed";
        }
        if (statusCurrent == null) {
            return "pending";
        }
        return statusCurrent.toLowerCase();
    }

    public LocalDateTime reviewedAtTime() {
        try {
            return (reviewedAt == null || reviewedAt.isBlank()) ? null : LocalDateTime.parse(reviewedAt);
        } catch (Exception ex) {
            return null;
        }
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }
    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }
    public String getApplicantMajor() { return applicantMajor; }
    public void setApplicantMajor(String applicantMajor) { this.applicantMajor = applicantMajor; }
    public String getApplicantYear() { return applicantYear; }
    public void setApplicantYear(String applicantYear) { this.applicantYear = applicantYear; }
    public Double getApplicantGpa() { return applicantGpa; }
    public void setApplicantGpa(Double applicantGpa) { this.applicantGpa = applicantGpa; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public Integer getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(Integer weeklyHours) { this.weeklyHours = weeklyHours; }
    public String getStatusCurrent() { return statusCurrent; }
    public void setStatusCurrent(String statusCurrent) { this.statusCurrent = statusCurrent; }
    public String getStatusLabel() { return statusLabel; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public List<String> getRelevantSkills() { return relevantSkills; }
    public void setRelevantSkills(List<String> relevantSkills) { this.relevantSkills = relevantSkills; }
    public String getRelevantExperience() { return relevantExperience; }
    public void setRelevantExperience(String relevantExperience) { this.relevantExperience = relevantExperience; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getMotivationCoverLetter() { return motivationCoverLetter; }
    public void setMotivationCoverLetter(String motivationCoverLetter) { this.motivationCoverLetter = motivationCoverLetter; }
    public String getReviewDecision() { return reviewDecision; }
    public void setReviewDecision(String reviewDecision) { this.reviewDecision = reviewDecision; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public String getReviewDecisionReason() { return reviewDecisionReason; }
    public void setReviewDecisionReason(String reviewDecisionReason) { this.reviewDecisionReason = reviewDecisionReason; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getSourceFilePath() { return sourceFilePath; }
    public void setSourceFilePath(String sourceFilePath) { this.sourceFilePath = sourceFilePath; }
    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
}
