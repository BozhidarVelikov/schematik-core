package org.schematik.jetty;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.schematik.Application;
import org.schematik.plugins.PluginConfig;
import org.schematik.api.RestApiConfig;
import org.schematik.scheduler.TaskSchedulerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Objects;

public class JettyServer {
    public static JettyServer instance;

    static Logger logger = LoggerFactory.getLogger(JettyServer.class);

    int minThreads = 200;
    int maxThreads = 250;
    int idleTimeout = 120;

    public Javalin app;

    AnnotationConfigApplicationContext context;

    public void start() {
        instance = this;

        app = Javalin.create(javalinConfig -> {
            javalinConfig.jetty.threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

            javalinConfig.showJavalinBanner = false;

            javalinConfig.bundledPlugins.enableCors(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });

            javalinConfig.staticFiles.add(staticFileConfig -> {
                staticFileConfig.directory = "/swagger-ui";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.precompress = false;
            });

            // javalinConfig.bundledPlugins.enableRouteOverview("/");
        });

        // Load properties
        Application.initialize();

        // Task scheduler
        context = new AnnotationConfigApplicationContext(TaskSchedulerConfig.class);

        // Custom plugins
        PluginConfig.initialize();

        // Rest API config
        RestApiConfig.initialize();

        app.start(8090);

        logger.info("Server ready...");

        try {
            Objects.requireNonNull(app.jettyServer()).server().join();
        } catch (InterruptedException e) {
            stop(e);
        }
    }

    public void stop() {
        logger.info("Server exiting...");
        app.stop();
        System.exit(0);
    }

    public void stop(Throwable t) {
        logger.error("Server exiting with error...", t);
        app.stop();
        System.exit(0);
    }
}
