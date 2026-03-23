package com.ta.recruitment.service;

import com.ta.recruitment.dao.JobDao;
import com.ta.recruitment.model.Job;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JobService {

    private final JobDao jobDao;

    public JobService(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    public void createJob(Job job) throws IOException {
        validateJob(job, false);
        populateMetaForCreate(job);
        jobDao.saveJob(job);
    }

    public List<Job> getAllJobs() throws IOException {
        List<Job> jobs = new ArrayList<>(jobDao.getAllJobs());
        jobs.sort(Comparator.comparing(Job::getJobId, Comparator.nullsLast(String::compareToIgnoreCase)));
        return jobs;
    }

    public Optional<Job> getJobById(String jobId) throws IOException {
        if (jobId == null || jobId.isBlank()) {
            return Optional.empty();
        }
        return getAllJobs().stream()
                .filter(job -> Objects.equals(jobId, job.getJobId()))
                .findFirst();
    }

    public void updateJob(Job job) throws IOException {
        validateJob(job, true);
        updateMeta(job);
        jobDao.updateJob(job);
    }

    public void deleteJob(String jobId) throws IOException {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId cannot be empty.");
        }
        jobDao.deleteJob(jobId);
    }

    public void toggleOpenClosed(String jobId) throws IOException {
        Job job = getJobById(jobId).orElseThrow(() ->
                new IllegalArgumentException("Job not found for id: " + jobId));
        if (job.getLifecycle() == null) {
            job.setLifecycle(new Job.Lifecycle());
        }
        String current = safeString(job.getLifecycle().getStatus()).toLowerCase();
        job.getLifecycle().setStatus("open".equals(current) ? "closed" : "open");
        updateMeta(job);
        jobDao.updateJob(job);
    }

    private void validateJob(Job job, boolean isUpdate) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null.");
        }
        if (isBlank(job.getJobId())) {
            throw new IllegalArgumentException("Job ID is required.");
        }
        if (isBlank(job.getTitle())) {
            throw new IllegalArgumentException("Job title is required.");
        }
        if (job.getCourse() == null || isBlank(job.getCourse().getCourseCode())) {
            throw new IllegalArgumentException("Module code is required.");
        }
        if (job.getCourse() == null || isBlank(job.getCourse().getCourseName())) {
            throw new IllegalArgumentException("Module name is required.");
        }
        if (job.getInstructor() == null || isBlank(job.getInstructor().getName())) {
            throw new IllegalArgumentException("Instructor name is required.");
        }
        if (job.getEmployment() == null) {
            throw new IllegalArgumentException("Employment details are required.");
        }
        if (job.getEmployment().getWeeklyHours() <= 0) {
            throw new IllegalArgumentException("Weekly hours must be a positive number.");
        }
        if (job.getContent() == null || isBlank(job.getContent().getDescription())) {
            throw new IllegalArgumentException("Job description is required.");
        }
        if (job.getLifecycle() == null) {
            job.setLifecycle(new Job.Lifecycle());
        }
        if (isBlank(job.getLifecycle().getStatus())) {
            job.getLifecycle().setStatus("open");
        }
    }

    private void populateMetaForCreate(Job job) {
        String now = LocalDateTime.now().toString();
        if (job.getMeta() == null) {
            job.setMeta(new Job.Meta());
        }
        if (isBlank(job.getMeta().getCreatedAt())) {
            job.getMeta().setCreatedAt(now);
        }
        job.getMeta().setUpdatedAt(now);

        if (job.getPublication() == null) {
            job.setPublication(new Job.Publication());
        }
        if (isBlank(job.getPublication().getStatus())) {
            job.getPublication().setStatus("draft");
        }
    }

    private void updateMeta(Job job) {
        String now = LocalDateTime.now().toString();
        if (job.getMeta() == null) {
            job.setMeta(new Job.Meta());
        }
        if (isBlank(job.getMeta().getCreatedAt())) {
            job.getMeta().setCreatedAt(now);
        }
        job.getMeta().setUpdatedAt(now);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
