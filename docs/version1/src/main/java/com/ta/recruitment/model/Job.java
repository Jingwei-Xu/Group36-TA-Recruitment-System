package com.ta.recruitment.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Job entity that mirrors the JSON structure in data/jobs/*.json.
 */
public class Job {

    private String jobId;
    private String title;
    private Course course;
    private String department;
    private Instructor instructor;
    private Employment employment;
    private Dates dates;
    private Content content;
    private Ownership ownership;
    private Publication publication;
    private Lifecycle lifecycle;
    private Stats stats;
    private Meta meta;

    public Job() {
        this.course = new Course();
        this.instructor = new Instructor();
        this.employment = new Employment();
        this.dates = new Dates();
        this.content = new Content();
        this.ownership = new Ownership();
        this.publication = new Publication();
        this.lifecycle = new Lifecycle();
        this.stats = new Stats();
        this.meta = new Meta();
    }

    public Job(String jobId, String title, Course course, String department, Instructor instructor,
               Employment employment, Dates dates, Content content, Ownership ownership,
               Publication publication, Lifecycle lifecycle, Stats stats, Meta meta) {
        this.jobId = jobId;
        this.title = title;
        this.course = course;
        this.department = department;
        this.instructor = instructor;
        this.employment = employment;
        this.dates = dates;
        this.content = content;
        this.ownership = ownership;
        this.publication = publication;
        this.lifecycle = lifecycle;
        this.stats = stats;
        this.meta = meta;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public Employment getEmployment() {
        return employment;
    }

    public void setEmployment(Employment employment) {
        this.employment = employment;
    }

    public Dates getDates() {
        return dates;
    }

    public void setDates(Dates dates) {
        this.dates = dates;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Ownership getOwnership() {
        return ownership;
    }

    public void setOwnership(Ownership ownership) {
        this.ownership = ownership;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", title='" + title + '\'' +
                ", course=" + course +
                ", department='" + department + '\'' +
                ", instructor=" + instructor +
                ", employment=" + employment +
                ", dates=" + dates +
                ", content=" + content +
                ", ownership=" + ownership +
                ", publication=" + publication +
                ", lifecycle=" + lifecycle +
                ", stats=" + stats +
                ", meta=" + meta +
                '}';
    }

    public static class Course {
        private String courseCode;
        private String courseName;
        private String term;
        private int year;

        public Course() {
        }

        public Course(String courseCode, String courseName, String term, int year) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.term = term;
            this.year = year;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        @Override
        public String toString() {
            return "Course{" +
                    "courseCode='" + courseCode + '\'' +
                    ", courseName='" + courseName + '\'' +
                    ", term='" + term + '\'' +
                    ", year=" + year +
                    '}';
        }
    }

    public static class Instructor {
        private String name;
        private String email;

        public Instructor() {
        }

        public Instructor(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "Instructor{" +
                    "name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    public static class Employment {
        private String jobType;
        private String employmentType;
        private int weeklyHours;
        private String locationMode;
        private String locationDetail;

        public Employment() {
        }

        public Employment(String jobType, String employmentType, int weeklyHours, String locationMode, String locationDetail) {
            this.jobType = jobType;
            this.employmentType = employmentType;
            this.weeklyHours = weeklyHours;
            this.locationMode = locationMode;
            this.locationDetail = locationDetail;
        }

        public String getJobType() {
            return jobType;
        }

        public void setJobType(String jobType) {
            this.jobType = jobType;
        }

        public String getEmploymentType() {
            return employmentType;
        }

        public void setEmploymentType(String employmentType) {
            this.employmentType = employmentType;
        }

        public int getWeeklyHours() {
            return weeklyHours;
        }

        public void setWeeklyHours(int weeklyHours) {
            this.weeklyHours = weeklyHours;
        }

        public String getLocationMode() {
            return locationMode;
        }

        public void setLocationMode(String locationMode) {
            this.locationMode = locationMode;
        }

        public String getLocationDetail() {
            return locationDetail;
        }

        public void setLocationDetail(String locationDetail) {
            this.locationDetail = locationDetail;
        }

        @Override
        public String toString() {
            return "Employment{" +
                    "jobType='" + jobType + '\'' +
                    ", employmentType='" + employmentType + '\'' +
                    ", weeklyHours=" + weeklyHours +
                    ", locationMode='" + locationMode + '\'' +
                    ", locationDetail='" + locationDetail + '\'' +
                    '}';
        }
    }

    public static class Dates {
        private String postedAt;
        private String deadline;
        private String startDate;
        private String endDate;

        public Dates() {
        }

        public Dates(String postedAt, String deadline, String startDate, String endDate) {
            this.postedAt = postedAt;
            this.deadline = deadline;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getPostedAt() {
            return postedAt;
        }

        public void setPostedAt(String postedAt) {
            this.postedAt = postedAt;
        }

        public String getDeadline() {
            return deadline;
        }

        public void setDeadline(String deadline) {
            this.deadline = deadline;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        @Override
        public String toString() {
            return "Dates{" +
                    "postedAt='" + postedAt + '\'' +
                    ", deadline='" + deadline + '\'' +
                    ", startDate='" + startDate + '\'' +
                    ", endDate='" + endDate + '\'' +
                    '}';
        }
    }

    public static class Content {
        private String summary;
        private String description;
        private List<String> responsibilities;
        private List<String> requirements;
        private List<String> preferredSkills;

        public Content() {
            this.responsibilities = new ArrayList<>();
            this.requirements = new ArrayList<>();
            this.preferredSkills = new ArrayList<>();
        }

        public Content(String summary, String description, List<String> responsibilities,
                       List<String> requirements, List<String> preferredSkills) {
            this.summary = summary;
            this.description = description;
            this.responsibilities = responsibilities;
            this.requirements = requirements;
            this.preferredSkills = preferredSkills;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getResponsibilities() {
            return responsibilities;
        }

        public void setResponsibilities(List<String> responsibilities) {
            this.responsibilities = responsibilities;
        }

        public List<String> getRequirements() {
            return requirements;
        }

        public void setRequirements(List<String> requirements) {
            this.requirements = requirements;
        }

        public List<String> getPreferredSkills() {
            return preferredSkills;
        }

        public void setPreferredSkills(List<String> preferredSkills) {
            this.preferredSkills = preferredSkills;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "summary='" + summary + '\'' +
                    ", description='" + description + '\'' +
                    ", responsibilities=" + responsibilities +
                    ", requirements=" + requirements +
                    ", preferredSkills=" + preferredSkills +
                    '}';
        }
    }

    public static class Ownership {
        private String createdBy;
        private List<String> managedBy;
        private String lastEditedBy;

        public Ownership() {
            this.managedBy = new ArrayList<>();
        }

        public Ownership(String createdBy, List<String> managedBy, String lastEditedBy) {
            this.createdBy = createdBy;
            this.managedBy = managedBy;
            this.lastEditedBy = lastEditedBy;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public List<String> getManagedBy() {
            return managedBy;
        }

        public void setManagedBy(List<String> managedBy) {
            this.managedBy = managedBy;
        }

        public String getLastEditedBy() {
            return lastEditedBy;
        }

        public void setLastEditedBy(String lastEditedBy) {
            this.lastEditedBy = lastEditedBy;
        }

        @Override
        public String toString() {
            return "Ownership{" +
                    "createdBy='" + createdBy + '\'' +
                    ", managedBy=" + managedBy +
                    ", lastEditedBy='" + lastEditedBy + '\'' +
                    '}';
        }
    }

    public static class Publication {
        private String status;
        private String publishedAt;
        private String publishedBy;

        public Publication() {
        }

        public Publication(String status, String publishedAt, String publishedBy) {
            this.status = status;
            this.publishedAt = publishedAt;
            this.publishedBy = publishedBy;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPublishedAt() {
            return publishedAt;
        }

        public void setPublishedAt(String publishedAt) {
            this.publishedAt = publishedAt;
        }

        public String getPublishedBy() {
            return publishedBy;
        }

        public void setPublishedBy(String publishedBy) {
            this.publishedBy = publishedBy;
        }

        @Override
        public String toString() {
            return "Publication{" +
                    "status='" + status + '\'' +
                    ", publishedAt='" + publishedAt + '\'' +
                    ", publishedBy='" + publishedBy + '\'' +
                    '}';
        }
    }

    public static class Lifecycle {
        private String status;
        private boolean isDeleted;
        private String deletedAt;
        private String deletedBy;
        private String closeReason;

        public Lifecycle() {
        }

        public Lifecycle(String status, boolean isDeleted, String deletedAt, String deletedBy, String closeReason) {
            this.status = status;
            this.isDeleted = isDeleted;
            this.deletedAt = deletedAt;
            this.deletedBy = deletedBy;
            this.closeReason = closeReason;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public void setDeleted(boolean deleted) {
            isDeleted = deleted;
        }

        public String getDeletedAt() {
            return deletedAt;
        }

        public void setDeletedAt(String deletedAt) {
            this.deletedAt = deletedAt;
        }

        public String getDeletedBy() {
            return deletedBy;
        }

        public void setDeletedBy(String deletedBy) {
            this.deletedBy = deletedBy;
        }

        public String getCloseReason() {
            return closeReason;
        }

        public void setCloseReason(String closeReason) {
            this.closeReason = closeReason;
        }

        @Override
        public String toString() {
            return "Lifecycle{" +
                    "status='" + status + '\'' +
                    ", isDeleted=" + isDeleted +
                    ", deletedAt='" + deletedAt + '\'' +
                    ", deletedBy='" + deletedBy + '\'' +
                    ", closeReason='" + closeReason + '\'' +
                    '}';
        }
    }

    public static class Stats {
        private int applicationCount;
        private int pendingCount;
        private int acceptedCount;
        private int rejectedCount;

        public Stats() {
        }

        public Stats(int applicationCount, int pendingCount, int acceptedCount, int rejectedCount) {
            this.applicationCount = applicationCount;
            this.pendingCount = pendingCount;
            this.acceptedCount = acceptedCount;
            this.rejectedCount = rejectedCount;
        }

        public int getApplicationCount() {
            return applicationCount;
        }

        public void setApplicationCount(int applicationCount) {
            this.applicationCount = applicationCount;
        }

        public int getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(int pendingCount) {
            this.pendingCount = pendingCount;
        }

        public int getAcceptedCount() {
            return acceptedCount;
        }

        public void setAcceptedCount(int acceptedCount) {
            this.acceptedCount = acceptedCount;
        }

        public int getRejectedCount() {
            return rejectedCount;
        }

        public void setRejectedCount(int rejectedCount) {
            this.rejectedCount = rejectedCount;
        }

        @Override
        public String toString() {
            return "Stats{" +
                    "applicationCount=" + applicationCount +
                    ", pendingCount=" + pendingCount +
                    ", acceptedCount=" + acceptedCount +
                    ", rejectedCount=" + rejectedCount +
                    '}';
        }
    }

    public static class Meta {
        private String createdAt;
        private String updatedAt;

        public Meta() {
        }

        public Meta(String createdAt, String updatedAt) {
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        @Override
        public String toString() {
            return "Meta{" +
                    "createdAt='" + createdAt + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Job)) {
            return false;
        }
        Job job = (Job) o;
        return Objects.equals(jobId, job.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }
}
