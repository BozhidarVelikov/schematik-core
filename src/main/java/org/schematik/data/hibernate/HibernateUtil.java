package org.schematik.data.hibernate;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.schematik.Application;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class HibernateUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // Create registry
                registry = new StandardServiceRegistryBuilder().applySettings(Application.applicationProperties).build();
                // Create MetadataSources
                MetadataSources sources = new MetadataSources(registry);

                // Add all annotated classes from the main class package
                sources.addAnnotatedClasses(getEntityClasses().toArray(new Class<?>[0]));

                // Create Metadata
                Metadata metadata = sources.getMetadataBuilder().build();
                // Create SessionFactory
                sessionFactory = metadata.getSessionFactoryBuilder().build();
            } catch (Throwable e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                throw new RuntimeException(e);
            }
        }
        return sessionFactory;
    }

    public static Set<Class<?>> getEntityClasses() throws ClassNotFoundException {
        Class<?> mainClass = Class.forName(Application.getProperty("class.main"));

        Package mainClassPackage = mainClass.getPackage();

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(mainClassPackage.getName())
                        .addScanners(Scanners.TypesAnnotated)
        );

        // Find all classes annotated with @Entity
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

        return entities;
    }
}
