package org.schematik.scheduler;

import org.apache.commons.io.IOUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.schematik.Application;
import org.schematik.plugin.ISchematikPlugin;
import org.schematik.util.resource.FileResourceUtil;
import org.schematik.util.xml.XMLParser;
import org.schematik.util.xml.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class JobSchedulerPlugin implements ISchematikPlugin {
    static Logger logger = LoggerFactory.getLogger(JobSchedulerPlugin.class);

    static Scheduler scheduler;

    @Override
    public void register() {
        try {
            logger.info("Scheduling tasks...");

            Properties props = new Properties();
            props.setProperty("org.quartz.scheduler.instanceName", "SchematikScheduler");
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty(
                    "org.quartz.threadPool.threadCount",
                    Application.getPropertyOrDefault("scheduler.thread.count", "10")
            );
            props.setProperty("org.quartz.threadPool.threadPriority", "5");

            scheduler = new StdSchedulerFactory(props).getScheduler();
            scheduler.start();

            int numberOfTasks = 0;

            String schedulerConfigContent = IOUtils.toString(
                    FileResourceUtil.getFileFromResourceAsStream("scheduler.config.xml"),
                    StandardCharsets.UTF_8
            );
            XmlElement xml = XMLParser.parse(schedulerConfigContent);

            List<XmlElement> scheduledTasks = xml.getElements("task");
            for (XmlElement task : scheduledTasks) {
                String implementationClassString = task.getProperty("class");
                Class<?> implementation = Class.forName(implementationClassString);
                IScheduledJob scheduledTask = (IScheduledJob) implementation.getDeclaredConstructor().newInstance();

                XmlElement schedule = task.getElement("schedule");
                JobDetail jobDetail = JobBuilder.newJob(scheduledTask.getClass())
                        .withIdentity(scheduledTask.getClass().getName())
                        .build();

                Trigger trigger;

                if (schedule.hasProperty("cron")) {
                    String cronExpression = schedule.getProperty("cron");

                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(scheduledTask.getClass().getName())
                            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                            .build();

                    logger.info(String.format(
                            "Scheduled task %s using \"%s\" cron expression",
                            implementationClassString,
                            cronExpression
                    ));
                } else if (schedule.hasProperty("period")) {
                    long period = Long.parseLong(schedule.getProperty("period"));
                    boolean isFixedRate = schedule.hasProperty("fixedRate")
                            && Boolean.parseBoolean(schedule.getProperty("fixedRate"));
                    long initialDelay = schedule.hasProperty("initialDelay")
                            ? Long.parseLong(schedule.getProperty("initialDelay"))
                            : 0;

                    SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds(period);

                    if (isFixedRate) {
                        scheduleBuilder.repeatForever();
                    }

                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(scheduledTask.getClass().getName())
                            .startAt(new Date(System.currentTimeMillis() + period + initialDelay))
                            .withSchedule(scheduleBuilder)
                            .build();

                    logger.info(String.format(
                            "Scheduled task %s every %d seconds with initial delay of %d seconds",
                            implementationClassString,
                            period,
                            initialDelay
                    ));
                } else {
                    logger.error(String.format("Unknown schedule type: [XmlElement=%s]", schedule));
                    continue;
                }

                scheduler.scheduleJob(jobDetail, trigger);

                numberOfTasks++;
            }

            logger.info(String.format("Scheduled %d task(s).", numberOfTasks));
        } catch (Exception exception) {
            logger.error("Error while scheduling tasks: " + exception);
        }
    }

    public static void pause(Class<IScheduledJob> scheduledTaskClass) {
        if (scheduler != null) {
            try {
                scheduler.pauseJob(JobKey.jobKey(scheduledTaskClass.getName()));

                logger.info(String.format(
                        "Paused job %s at %s",
                        scheduledTaskClass.getName(),
                        LocalDateTime.now()
                ));
            } catch (SchedulerException e) {
                logger.error(String.format(
                        "Error while pausing job %s: %s",
                        scheduledTaskClass.getName(),
                        e.getMessage()
                ), e);
            }
        } else {
            logger.error(String.format(
                    "Plugin %s has not been registered!",
                    JobSchedulerPlugin.class.getName()
            ));
        }
    }

    public static void resume(Class<IScheduledJob> scheduledTaskClass) {
        if (scheduler != null) {
            try {
                scheduler.resumeJob(JobKey.jobKey(scheduledTaskClass.getName()));

                logger.info(String.format(
                        "Resumed job %s at %s",
                        scheduledTaskClass.getName(),
                        LocalDateTime.now()
                ));
            } catch (SchedulerException e) {
                logger.error(String.format(
                        "Error while resuming job %s: %s",
                        scheduledTaskClass.getName(),
                        e.getMessage()
                ));
            }
        } else {
            logger.error(String.format(
                    "Plugin %s has not been registered!",
                    JobSchedulerPlugin.class.getName()
            ));
        }
    }

    public static void interrupt(Class<IScheduledJob> scheduledTaskClass) {
        if (scheduler != null) {
            try {
                JobKey jobKey = JobKey.jobKey(scheduledTaskClass.getName());

                // Pause job
                pause(scheduledTaskClass);

                // Execute exitJob() method
                ((IScheduledJob) scheduler.getJobDetail(jobKey)).exitJob();

                // Interrupt job
                scheduler.interrupt(jobKey);

                logger.info(String.format(
                        "Interrupted job %s at %s",
                        scheduledTaskClass.getName(),
                        LocalDateTime.now()
                ));
            } catch (SchedulerException e) {
                logger.error(String.format(
                        "Error while interrupting job %s: %s",
                        scheduledTaskClass.getName(),
                        e.getMessage()
                ));
            }
        } else {
            logger.error(String.format(
                    "Plugin %s has not been registered!",
                    JobSchedulerPlugin.class.getName()
            ));
        }
    }
}
