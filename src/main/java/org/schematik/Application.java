package org.schematik;

import org.schematik.util.resource.FileResourceUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {
    public static Properties applicationProperties;

    public static void initialize() {
        applicationProperties = new Properties();
        try {
            applicationProperties.load(
                    FileResourceUtil.getFileFromResourceAsStream("application.properties")
            );

            for (Object key : applicationProperties.keySet()) {
                String propertyValue = applicationProperties.getProperty((String) key);
                List<String> extractedEnvironmentVariables = extractPlaceholders(propertyValue);
                for (String environmentVariable : extractedEnvironmentVariables) {
                    propertyValue = replacePlaceholder(
                            propertyValue,
                            environmentVariable,
                            System.getenv(environmentVariable)
                    );
                }

                applicationProperties.setProperty((String) key, propertyValue);
            }
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

    public static List<String> extractPlaceholders(String input) {
        List<String> placeholders = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        return placeholders;
    }

    public static String replacePlaceholder(String input, String placeholder, String replacement) {
        String regex = "\\$\\{" + Pattern.quote(placeholder) + "\\}";
        return input.replaceAll(regex, Matcher.quoteReplacement(replacement));
    }
}
