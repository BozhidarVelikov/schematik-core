package org.schematik.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public interface IScheduledJob extends Job {
    boolean interrupted = false;

    @Override
    default void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (checkInitialConditions()) {
            doJob();
            exitJob();
        }
    }

    boolean checkInitialConditions();

    void doJob();

    void exitJob();
}
