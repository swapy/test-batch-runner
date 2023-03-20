package com.test.extracts.batch.app.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobInstance;


public interface TestBatchJobListener {

    Logger logger = LoggerFactory.getLogger(TestBatchJobListener.class);

    default JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                JobInstance jobInstance = jobExecution.getJobInstance();
                logger.info("before job id {}, and name {}", jobInstance.getInstanceId(), jobInstance.getJobName());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                JobInstance jobInstance = jobExecution.getJobInstance();
                logger.info("after job id {}, and name {}", jobInstance.getInstanceId(), jobInstance.getJobName());

            }
        };
    }
}
