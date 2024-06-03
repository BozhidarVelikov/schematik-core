package org.schematik.api;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.schematik.Application;
import org.schematik.api.annotation.Controller;

import java.util.Set;

public class RestApiUtils {
    public static Set<Class<?>> getControllerClasses() throws ClassNotFoundException {
        Class<?> mainClass = Class.forName(Application.getProperty("class.main"));

        Package mainClassPackage = mainClass.getPackage();

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(mainClassPackage.getName())
                        .addScanners(Scanners.TypesAnnotated)
        );

        // Find all classes annotated with @Controller
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Controller.class);

        return entities;
    }
}
