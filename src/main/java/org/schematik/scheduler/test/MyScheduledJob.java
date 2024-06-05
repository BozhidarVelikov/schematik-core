package org.schematik.scheduler.test;

import org.schematik.scheduler.IScheduledJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyScheduledJob implements IScheduledJob {
    Logger logger = LoggerFactory.getLogger(MyScheduledJob.class);

    @Override
    public boolean checkInitialConditions() {
        logger.info("checkInitialConditions started");

        return true;
    }

    @Override
    public void doJob() {
        logger.info("Current task is running on thread [" + Thread.currentThread().getName() + "].");
        logger.info("doJob finished");
    }

    @Override
    public void exitJob() {
        logger.info("exitJob finished");
    }
}
