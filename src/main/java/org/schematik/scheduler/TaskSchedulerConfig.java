package org.schematik.scheduler;

import org.schematik.util.resource.FileResourceUtil;
import org.schematik.util.xml.XMLParser;
import org.schematik.util.xml.XmlElement;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableScheduling
@EnableAsync
public class TaskSchedulerConfig implements SchedulingConfigurer {
    Logger logger = LoggerFactory.getLogger(TaskSchedulerConfig.class);

    int maxNumberOfThreads = 10;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(maxNumberOfThreads);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Override
    public void configureTasks(@NotNull ScheduledTaskRegistrar taskRegistrar) {
        try {
            logger.info("Scheduling tasks...");
            int numberOfTasks = 0;

            XmlElement xml = XMLParser.parse(FileResourceUtil.getFileFromResource("scheduler.config.xml"));

            List<XmlElement> scheduledTasks = xml.getElements("task");
            for (XmlElement task : scheduledTasks) {
                String implementationClassString = task.getProperty("class");
                Class<?> implementation = Class.forName(implementationClassString);
                IScheduledTask scheduledTask = (IScheduledTask) implementation.getDeclaredConstructor().newInstance();

                List<XmlElement> schedules = task.getElements("schedule");
                for (XmlElement schedule : schedules) {
                    Trigger trigger;

                    if (schedule.hasProperty("cron")) {
                        trigger = new CronTrigger(schedule.getProperty("cron"));
                    } else if (schedule.hasProperty("period")) {
                        PeriodicTrigger periodicTrigger = new PeriodicTrigger(Duration.ofMillis(Long.parseLong(schedule.getProperty("period"))));
                        periodicTrigger.setFixedRate(schedule.hasProperty("fixedRate") && Boolean.parseBoolean(schedule.getProperty("fixedRate")));
                        if (schedule.hasProperty("initialDelay")) {
                            periodicTrigger.setInitialDelay(Duration.ofMillis(Long.parseLong(schedule.getProperty("initialDelay"))));
                        }

                        trigger = periodicTrigger;
                    } else {
                        logger.error(String.format("Unknown schedule type: [XmlElement=%s]", schedule));
                        continue;
                    }

                    taskScheduler().schedule(scheduledTask, trigger);

                    if (trigger.getClass() == CronTrigger.class) {
                        logger.info(String.format("Scheduled task %s using \"%s\" cron expression", implementationClassString, trigger));
                    } else {
                        PeriodicTrigger periodicTrigger = (PeriodicTrigger) trigger;
                        String logMessage = "Scheduled " + (periodicTrigger.isFixedRate() ? "fixed-rate" : "");

                        logMessage += String.format(
                                " task %s every %d seconds with initial delay of %d seconds",
                                implementationClassString,
                                periodicTrigger.getPeriodDuration().getSeconds(),
                                periodicTrigger.getInitialDelayDuration().getSeconds());

                        logger.info(logMessage);
                    }

                    numberOfTasks++;
                }
            }

            logger.info(String.format("Scheduled %d task(s).", numberOfTasks));
        } catch (Exception exception) {
            logger.error("Error while scheduling tasks: " + exception);
        }
    }
}
