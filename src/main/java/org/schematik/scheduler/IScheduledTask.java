package org.schematik.scheduler;

import org.springframework.stereotype.Component;

@Component
public interface IScheduledTask extends Runnable {

    @Override
    default void run() {
        beforeJob();
        doJob();
        afterJob();
    }

    void beforeJob();

    void doJob();

    void afterJob();
}
