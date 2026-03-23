package com.ta.recruitment.dao;

import com.ta.recruitment.model.Job;

import java.io.IOException;
import java.util.List;

public interface JobDao {

    void saveJob(Job job) throws IOException;

    List<Job> getAllJobs() throws IOException;

    void updateJob(Job job) throws IOException;

    void deleteJob(String jobId) throws IOException;
}
