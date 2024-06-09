package org.schematik.data.transaction;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.schematik.data.hibernate.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Bundle {
    static Logger logger = LoggerFactory.getLogger(Bundle.class);
    static SessionFactory factory;

    private final Session session;
    private final Transaction transaction;

    static Map<Session, Bundle> bundleMap;

    private Bundle() {
        session = factory.getCurrentSession();
        transaction = session.beginTransaction();
    }

    public static void initialize() {
        factory = HibernateUtil.getSessionFactory();
        bundleMap = new HashMap<>();
    }

    public static Bundle create() {
        if (factory == null) {
            factory = HibernateUtil.getSessionFactory();
        }

        Bundle bundle = new Bundle();
        bundleMap.put(bundle.getSession(), bundle);

        return bundle;
    }

    public void commit() {
        transaction.commit();
        session.close();
    }

    public void rollback() {
        if (transaction != null) {
            transaction.rollback();
        }

        closeSession();
    }

    public <T> T add(T entity) {
        if (session != null) {
            if (session.contains(entity)) {
                session.merge(entity);
            } else {
                session.persist(entity);
            }

            return entity;
        }

        return null;
    }

    public <T> T remove(T entity) {
        if (session != null) {
            session.remove(entity);

            return entity;
        }

        return null;
    }

    public void closeSession() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    public static void runWithNewBundle(Consumer<Bundle> consumer) {
        Bundle bundle = create();

        try {
            consumer.accept(bundle);
            bundle.commit();
        } catch (HibernateException e) {
            bundle.rollback();

            logger.error(
                    "Error while committing transaction to the database! The current transaction has been rolled back.",
                    e
            );
        } finally {
            bundleMap.remove(bundle.getSession());
            bundle.closeSession();
        }
    }

    public static Session getSessionForCurrentBundle() {
        try {
            return factory.getCurrentSession();
        } catch (HibernateException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public Session getSession() {
        return session;
    }

    public static Bundle getCurrentBundle() {
        return bundleMap.get(getSessionForCurrentBundle());
    }
}
