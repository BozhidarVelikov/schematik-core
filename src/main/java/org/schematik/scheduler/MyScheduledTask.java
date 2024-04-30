package org.schematik.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyScheduledTask implements IScheduledTask {
    Logger logger = LoggerFactory.getLogger(MyScheduledTask.class);

    @Override
    public void beforeJob() {
        logger.info("beforeJob finished");
    }

    @Override
    public void doJob() {
        logger.info("Current task is running on thread [" + Thread.currentThread().getName() + "].");
        logger.info("doJob finished");
    }

    @Override
    public void afterJob() {
        logger.info("afterJob finished");
    }
}
