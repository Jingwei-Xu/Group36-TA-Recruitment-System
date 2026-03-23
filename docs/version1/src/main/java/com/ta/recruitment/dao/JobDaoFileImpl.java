package com.ta.recruitment.dao;

import com.ta.recruitment.model.Job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * File-based DAO implementation for Job management.
 * It persists each job into data/jobs/{jobId}.json and maintains data/indexes/jobs_index.json.
 */
public class JobDaoFileImpl implements JobDao {

    private static final String JOBS_DIR = "jobs";
    private static final String INDEXES_DIR = "indexes";
    private static final String JOBS_INDEX_FILE = "jobs_index.json";

    private final Path jobsDirectoryPath;
    private final Path jobsIndexPath;
    private final ScriptEngine scriptEngine;

    public JobDaoFileImpl() {
        this(Path.of("data"));
    }

    public JobDaoFileImpl(Path dataRootPath) {
        this.jobsDirectoryPath = dataRootPath.resolve(JOBS_DIR);
        this.jobsIndexPath = dataRootPath.resolve(INDEXES_DIR).resolve(JOBS_INDEX_FILE);
        this.scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
        if (this.scriptEngine == null) {
            throw new IllegalStateException("JavaScript engine is unavailable. JSON parsing cannot proceed.");
        }
    }

    @Override
    public synchronized void saveJob(Job job) throws IOException {
        validateJobForPersistence(job);
        ensureStorageInitialized();

        Path targetFile = getJobFilePath(job.getJobId());
        if (Files.exists(targetFile)) {
            throw new IOException("Job already exists with id: " + job.getJobId());
        }

        writeJobFile(targetFile, job);
        updateJobsIndex(job, false);
    }

    @Override
    public synchronized List<Job> getAllJobs() throws IOException {
        ensureStorageInitialized();
        if (!Files.exists(jobsDirectoryPath)) {
            return new ArrayList<>();
        }

        List<Job> jobs = new ArrayList<>();
        try (var stream = Files.list(jobsDirectoryPath)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Job job = mapToJob(readJsonFromFile(path));
                            if (job.getLifecycle() == null || !job.getLifecycle().isDeleted()) {
                                jobs.add(job);
                            }
                        } catch (IOException exception) {
                            throw new RuntimeException("Failed to parse job file: " + path, exception);
                        }
                    });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof IOException) {
                throw (IOException) exception.getCause();
            }
            throw exception;
        }
        return jobs;
    }

    @Override
    public synchronized void updateJob(Job job) throws IOException {
        validateJobForPersistence(job);
        ensureStorageInitialized();

        Path targetFile = getJobFilePath(job.getJobId());
        if (!Files.exists(targetFile)) {
            throw new IOException("Cannot update. Job not found: " + job.getJobId());
        }

        writeJobFile(targetFile, job);
        updateJobsIndex(job, true);
    }

    @Override
    public synchronized void deleteJob(String jobId) throws IOException {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId must not be empty.");
        }
        ensureStorageInitialized();

        Path targetFile = getJobFilePath(jobId);
        if (!Files.exists(targetFile)) {
            return;
        }

        Files.delete(targetFile);
        removeFromJobsIndex(jobId);
    }

    private void validateJobForPersistence(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null.");
        }
        if (job.getJobId() == null || job.getJobId().isBlank()) {
            throw new IllegalArgumentException("jobId must not be empty.");
        }
    }

    private void ensureStorageInitialized() throws IOException {
        Files.createDirectories(jobsDirectoryPath);
        Files.createDirectories(jobsIndexPath.getParent());
        if (!Files.exists(jobsIndexPath)) {
            Map<String, List<Map<String, Object>>> emptyIndex = new HashMap<>();
            emptyIndex.put("jobs", new ArrayList<>());
            writeJsonAtomically(jobsIndexPath, emptyIndex);
        }
    }

    private Path getJobFilePath(String jobId) {
        return jobsDirectoryPath.resolve(jobId + ".json");
    }

    private void writeJobFile(Path path, Job job) throws IOException {
        writeJsonAtomically(path, jobToMap(job));
    }

    private void updateJobsIndex(Job job, boolean updateIfExists) throws IOException {
        Map<String, Object> index = readJobsIndex();
        List<Map<String, Object>> jobs = getJobsList(index);
        Map<String, Object> row = toIndexRow(job);

        Optional<Map<String, Object>> existing = jobs.stream()
                .filter(item -> job.getJobId().equals(String.valueOf(item.get("jobId"))))
                .findFirst();

        if (existing.isPresent()) {
            if (updateIfExists) {
                jobs.remove(existing.get());
                jobs.add(row);
            }
        } else {
            jobs.add(row);
        }
        index.put("jobs", jobs);
        writeJsonAtomically(jobsIndexPath, index);
    }

    private void removeFromJobsIndex(String jobId) throws IOException {
        Map<String, Object> index = readJobsIndex();
        List<Map<String, Object>> jobs = getJobsList(index);
        Iterator<Map<String, Object>> iterator = jobs.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> row = iterator.next();
            if (jobId.equals(String.valueOf(row.get("jobId")))) {
                iterator.remove();
                break;
            }
        }
        index.put("jobs", jobs);
        writeJsonAtomically(jobsIndexPath, index);
    }

    private Map<String, Object> readJobsIndex() throws IOException {
        if (!Files.exists(jobsIndexPath)) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("jobs", new ArrayList<>());
            return fallback;
        }
        return readJsonFromFile(jobsIndexPath);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getJobsList(Map<String, Object> index) {
        Object jobs = index.get("jobs");
        if (!(jobs instanceof List<?>)) {
            return new ArrayList<>();
        }
        return (List<Map<String, Object>>) jobs;
    }

    private Map<String, Object> toIndexRow(Job job) {
        Map<String, Object> row = new HashMap<>();
        row.put("jobId", job.getJobId());
        row.put("title", job.getTitle());
        row.put("courseCode", job.getCourse() != null ? job.getCourse().getCourseCode() : "");
        row.put("department", job.getDepartment());
        row.put("instructorName", job.getInstructor() != null ? job.getInstructor().getName() : "");
        row.put("weeklyHours", job.getEmployment() != null ? job.getEmployment().getWeeklyHours() : 0);
        row.put("deadline", job.getDates() != null ? safeDateOnly(job.getDates().getDeadline()) : "");
        row.put("locationMode", job.getEmployment() != null ? job.getEmployment().getLocationMode() : "");
        row.put("employmentType", job.getEmployment() != null ? job.getEmployment().getEmploymentType() : "");
        row.put("status", job.getLifecycle() != null ? job.getLifecycle().getStatus() : "");
        return row;
    }

    private String safeDateOnly(String dateTime) {
        if (dateTime == null) {
            return "";
        }
        int tIndex = dateTime.indexOf('T');
        return tIndex > 0 ? dateTime.substring(0, tIndex) : dateTime;
    }

    private void writeJsonAtomically(Path filePath, Object payload) throws IOException {
        Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        Files.writeString(tempFile, toJson(payload, 0));
        Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJsonFromFile(Path path) throws IOException {
        String json = Files.readString(path);
        try {
            scriptEngine.put("jsonText", json);
            Object result = scriptEngine.eval("Java.asJSONCompatible(JSON.parse(jsonText))");
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            throw new IOException("Unexpected JSON root format in file: " + path);
        } catch (ScriptException e) {
            throw new IOException("Invalid JSON format in file: " + path, e);
        }
    }

    private Job mapToJob(Map<String, Object> root) {
        Job job = new Job();
        job.setJobId(str(root.get("jobId")));
        job.setTitle(str(root.get("title")));
        job.setDepartment(str(root.get("department")));

        Map<String, Object> courseMap = map(root.get("course"));
        job.setCourse(new Job.Course(
                str(courseMap.get("courseCode")),
                str(courseMap.get("courseName")),
                str(courseMap.get("term")),
                integer(courseMap.get("year"))
        ));

        Map<String, Object> instructorMap = map(root.get("instructor"));
        job.setInstructor(new Job.Instructor(
                str(instructorMap.get("name")),
                str(instructorMap.get("email"))
        ));

        Map<String, Object> employmentMap = map(root.get("employment"));
        job.setEmployment(new Job.Employment(
                str(employmentMap.get("jobType")),
                str(employmentMap.get("employmentType")),
                integer(employmentMap.get("weeklyHours")),
                str(employmentMap.get("locationMode")),
                str(employmentMap.get("locationDetail"))
        ));

        Map<String, Object> datesMap = map(root.get("dates"));
        job.setDates(new Job.Dates(
                str(datesMap.get("postedAt")),
                str(datesMap.get("deadline")),
                str(datesMap.get("startDate")),
                str(datesMap.get("endDate"))
        ));

        Map<String, Object> contentMap = map(root.get("content"));
        job.setContent(new Job.Content(
                str(contentMap.get("summary")),
                str(contentMap.get("description")),
                strList(contentMap.get("responsibilities")),
                strList(contentMap.get("requirements")),
                strList(contentMap.get("preferredSkills"))
        ));

        Map<String, Object> ownershipMap = map(root.get("ownership"));
        job.setOwnership(new Job.Ownership(
                str(ownershipMap.get("createdBy")),
                strList(ownershipMap.get("managedBy")),
                str(ownershipMap.get("lastEditedBy"))
        ));

        Map<String, Object> publicationMap = map(root.get("publication"));
        job.setPublication(new Job.Publication(
                str(publicationMap.get("status")),
                str(publicationMap.get("publishedAt")),
                str(publicationMap.get("publishedBy"))
        ));

        Map<String, Object> lifecycleMap = map(root.get("lifecycle"));
        job.setLifecycle(new Job.Lifecycle(
                str(lifecycleMap.get("status")),
                bool(lifecycleMap.get("isDeleted")),
                nullableStr(lifecycleMap.get("deletedAt")),
                nullableStr(lifecycleMap.get("deletedBy")),
                str(lifecycleMap.get("closeReason"))
        ));

        Map<String, Object> statsMap = map(root.get("stats"));
        job.setStats(new Job.Stats(
                integer(statsMap.get("applicationCount")),
                integer(statsMap.get("pendingCount")),
                integer(statsMap.get("acceptedCount")),
                integer(statsMap.get("rejectedCount"))
        ));

        Map<String, Object> metaMap = map(root.get("meta"));
        job.setMeta(new Job.Meta(
                str(metaMap.get("createdAt")),
                str(metaMap.get("updatedAt"))
        ));
        return job;
    }

    private Map<String, Object> jobToMap(Job job) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("jobId", job.getJobId());
        root.put("title", job.getTitle());

        Map<String, Object> course = new LinkedHashMap<>();
        course.put("courseCode", job.getCourse() != null ? job.getCourse().getCourseCode() : "");
        course.put("courseName", job.getCourse() != null ? job.getCourse().getCourseName() : "");
        course.put("term", job.getCourse() != null ? job.getCourse().getTerm() : "");
        course.put("year", job.getCourse() != null ? job.getCourse().getYear() : 0);
        root.put("course", course);

        root.put("department", job.getDepartment());

        Map<String, Object> instructor = new LinkedHashMap<>();
        instructor.put("name", job.getInstructor() != null ? job.getInstructor().getName() : "");
        instructor.put("email", job.getInstructor() != null ? job.getInstructor().getEmail() : "");
        root.put("instructor", instructor);

        Map<String, Object> employment = new LinkedHashMap<>();
        employment.put("jobType", job.getEmployment() != null ? job.getEmployment().getJobType() : "");
        employment.put("employmentType", job.getEmployment() != null ? job.getEmployment().getEmploymentType() : "");
        employment.put("weeklyHours", job.getEmployment() != null ? job.getEmployment().getWeeklyHours() : 0);
        employment.put("locationMode", job.getEmployment() != null ? job.getEmployment().getLocationMode() : "");
        employment.put("locationDetail", job.getEmployment() != null ? job.getEmployment().getLocationDetail() : "");
        root.put("employment", employment);

        Map<String, Object> dates = new LinkedHashMap<>();
        dates.put("postedAt", job.getDates() != null ? job.getDates().getPostedAt() : "");
        dates.put("deadline", job.getDates() != null ? job.getDates().getDeadline() : "");
        dates.put("startDate", job.getDates() != null ? job.getDates().getStartDate() : "");
        dates.put("endDate", job.getDates() != null ? job.getDates().getEndDate() : "");
        root.put("dates", dates);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("summary", job.getContent() != null ? job.getContent().getSummary() : "");
        content.put("description", job.getContent() != null ? job.getContent().getDescription() : "");
        content.put("responsibilities", job.getContent() != null ? nullSafeList(job.getContent().getResponsibilities()) : new ArrayList<>());
        content.put("requirements", job.getContent() != null ? nullSafeList(job.getContent().getRequirements()) : new ArrayList<>());
        content.put("preferredSkills", job.getContent() != null ? nullSafeList(job.getContent().getPreferredSkills()) : new ArrayList<>());
        root.put("content", content);

        Map<String, Object> ownership = new LinkedHashMap<>();
        ownership.put("createdBy", job.getOwnership() != null ? job.getOwnership().getCreatedBy() : "");
        ownership.put("managedBy", job.getOwnership() != null ? nullSafeList(job.getOwnership().getManagedBy()) : new ArrayList<>());
        ownership.put("lastEditedBy", job.getOwnership() != null ? job.getOwnership().getLastEditedBy() : "");
        root.put("ownership", ownership);

        Map<String, Object> publication = new LinkedHashMap<>();
        publication.put("status", job.getPublication() != null ? job.getPublication().getStatus() : "");
        publication.put("publishedAt", job.getPublication() != null ? job.getPublication().getPublishedAt() : null);
        publication.put("publishedBy", job.getPublication() != null ? job.getPublication().getPublishedBy() : null);
        root.put("publication", publication);

        Map<String, Object> lifecycle = new LinkedHashMap<>();
        lifecycle.put("status", job.getLifecycle() != null ? job.getLifecycle().getStatus() : "");
        lifecycle.put("isDeleted", job.getLifecycle() != null && job.getLifecycle().isDeleted());
        lifecycle.put("deletedAt", job.getLifecycle() != null ? job.getLifecycle().getDeletedAt() : null);
        lifecycle.put("deletedBy", job.getLifecycle() != null ? job.getLifecycle().getDeletedBy() : null);
        lifecycle.put("closeReason", job.getLifecycle() != null ? job.getLifecycle().getCloseReason() : "");
        root.put("lifecycle", lifecycle);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("applicationCount", job.getStats() != null ? job.getStats().getApplicationCount() : 0);
        stats.put("pendingCount", job.getStats() != null ? job.getStats().getPendingCount() : 0);
        stats.put("acceptedCount", job.getStats() != null ? job.getStats().getAcceptedCount() : 0);
        stats.put("rejectedCount", job.getStats() != null ? job.getStats().getRejectedCount() : 0);
        root.put("stats", stats);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("createdAt", job.getMeta() != null ? job.getMeta().getCreatedAt() : "");
        meta.put("updatedAt", job.getMeta() != null ? job.getMeta().getUpdatedAt() : "");
        root.put("meta", meta);

        return root;
    }

    private String toJson(Object value, int indent) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append("  ".repeat(indent + 1)).append(toJson(list.get(i), indent + 1));
                if (i < list.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("  ".repeat(indent)).append("]");
            return sb.toString();
        }
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty()) {
                return "{}";
            }
            StringBuilder sb = new StringBuilder("{\n");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sb.append("  ".repeat(indent + 1))
                        .append("\"").append(escape(String.valueOf(entry.getKey()))).append("\": ")
                        .append(toJson(entry.getValue(), indent + 1));
                if (i < map.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
                i++;
            }
            sb.append("  ".repeat(indent)).append("}");
            return sb.toString();
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    private String escape(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    private List<String> strList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                result.add(str(item));
            }
        }
        return result;
    }

    private List<String> nullSafeList(List<String> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String nullableStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int integer(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private boolean bool(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}
