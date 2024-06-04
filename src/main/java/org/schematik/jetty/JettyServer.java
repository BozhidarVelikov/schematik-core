package org.schematik.jetty;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinGson;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.schematik.Application;
import org.schematik.gson.GsonUtils;
import org.schematik.plugin.PluginConfig;
import org.schematik.api.RestApiConfig;
import org.schematik.scheduler.TaskSchedulerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Objects;
import java.util.function.Consumer;

public class JettyServer {
    public static JettyServer instance;

    static Logger logger = LoggerFactory.getLogger(JettyServer.class);

    int minThreads = 200;
    int maxThreads = 250;
    int idleTimeout = 120;

    public Javalin app;

    AnnotationConfigApplicationContext context;

    public void start() {
        start(GsonUtils.getDefaultGson());
    }

    public void start(Gson gson) {
        start(javalinConfig -> {
            javalinConfig.jetty.threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

            javalinConfig.showJavalinBanner = false;

            javalinConfig.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));

            javalinConfig.staticFiles.add(staticFileConfig -> {
                staticFileConfig.directory = "/swagger-ui";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.precompress = false;
            });

            javalinConfig.jsonMapper(new JavalinGson(gson, false));

            // javalinConfig.bundledPlugins.enableRouteOverview("/");
        });
    }

    public void start(Consumer<JavalinConfig> javalinConfig) {
        instance = this;

        app = Javalin.create(javalinConfig);

        // Load properties
        Application.initialize();

        // Custom plugins
        PluginConfig.initialize();

        // Rest API config
        RestApiConfig.initialize();

        // Task scheduler
        context = new AnnotationConfigApplicationContext(TaskSchedulerConfig.class);

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
