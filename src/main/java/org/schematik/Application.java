package org.schematik;

import org.schematik.util.resource.FileResourceUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Application {
    public static Properties applicationProperties;

    public static void initialize() {
        applicationProperties = new Properties();
        try {
            applicationProperties.load(
                    new FileInputStream(
                            Objects.requireNonNull(
                                    FileResourceUtil.getFileFromResource("application.properties")
                            )
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return applicationProperties.getProperty(key);
    }

    public static String getPropertyOrDefault(String key, String defaultValue) {
        return applicationProperties.getProperty(key, defaultValue);
    }
}
