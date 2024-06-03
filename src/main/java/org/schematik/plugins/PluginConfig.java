package org.schematik.plugins;

import org.apache.commons.io.IOUtils;
import org.schematik.Application;
import org.schematik.util.resource.FileResourceUtil;
import org.schematik.util.xml.XMLParser;
import org.schematik.util.xml.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginConfig {
    private static Map<String, ISchematikPlugin> plugins;

    static Logger logger = LoggerFactory.getLogger(PluginConfig.class);

    public static synchronized void initialize() {
        try {
            logger.info("Initializing plugins...");
            PluginConfig.plugins = new HashMap<>();

            String pluginsConfigContent = IOUtils.toString(
                    FileResourceUtil.getFileFromResourceAsStream("plugins.config.xml"),
                    StandardCharsets.UTF_8
            );
            XmlElement xml = XMLParser.parse(pluginsConfigContent);
            List<XmlElement> plugins = xml.getElements("plugin");
            for (XmlElement plugin : plugins) {
                String pluginName = plugin.getProperty("name");
                String className = plugin.getProperty("class");
                boolean enabled = Boolean.parseBoolean(plugin.getProperty("enabled"));
                String environmentName = plugin.getProperty("env");

                if (!enabled || !environmentName.equals(Application.getProperty("env"))) {
                    continue;
                }

                if (PluginConfig.plugins.keySet().contains(pluginName)) {
                    throw new RuntimeException(String.format("Duplicate implementation for plugin with name %s: %s!", pluginName, className));
                }

                Class<?> implementation = Class.forName(className);
                ISchematikPlugin pluginImplementation = (ISchematikPlugin) implementation.getDeclaredConstructor().newInstance();

                PluginConfig.plugins.put(pluginName, pluginImplementation);
                pluginImplementation.register();

                logger.info(String.format("Registered implementation for plugin with name %s: %s", pluginName, pluginImplementation));
            }

            logger.info(String.format("Registered %d plugins.", plugins.size()));
        } catch (Exception e) {
            logger.error("Error while initializing plugins", e);
        }
    }

    public static ISchematikPlugin getPluginImplementation(String id) {
        return plugins.getOrDefault(id, null);
    }
}
