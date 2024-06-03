package org.schematik;

import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.schematik.gson.GsonUtils;
import org.schematik.jetty.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Enumeration;

public class App {

    static Logger logger;

    public static void main(String[] args) {
        logger = LoggerFactory.getLogger(App.class);
        logger.info("Starting server...");

        JettyServer server = new JettyServer();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                server.stop();
            }
        });
    }
}