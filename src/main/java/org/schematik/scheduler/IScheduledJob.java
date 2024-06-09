package org.schematik.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IScheduledJob extends Job {
    Logger logger = LoggerFactory.getLogger(IScheduledJob.class);

    boolean interrupted = false;

    @Override
    default void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (checkInitialConditions()) {
                doJob();
                exitJob();
            } else {
                exitJob();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            exitJob();
        }
    }

    boolean checkInitialConditions();

    void doJob();

    void exitJob();
}
