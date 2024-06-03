package org.schematik.data;

import org.hibernate.Session;
import org.schematik.data.criterion.*;
import org.schematik.data.transaction.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class Query<T> {
    static Logger logger = LoggerFactory.getLogger(Query.class);

    Class<T> type;
    List<QueryCriterion<T>> criteria;
    T entity;

    private Query(Class<T> type) {
        this.type = type;
        this.criteria = new ArrayList<>();
    }

    public static <T> Query<T> make(Class<T> type) {
        return new Query<>(type);
    }

    public Query<T> compare(String fieldName, CompareOperator operator, Object value) {
        criteria.add(new CompareCriterion<>(type, fieldName, operator, value));

        return this;
    }

    public Query<T> or(Function<OrCriterion<T>, OrCriterion<T>> criteria) {
        this.criteria.add(criteria.apply(new OrCriterion<>(type)));

        return this;
    }

    public Query<T> not(String fieldName, CompareOperator operator, Object value) {
        criteria.add(new NotCriterion<>(type, fieldName, operator, value));

        return this;
    }

    public Query<T> between(String fieldName, Object min, Object max) {
        criteria.add(new BetweenCriterion<>(type, fieldName, min, max));

        return this;
    }

    public Query<T> in(String fieldName, Object... values) {
        criteria.add(new InCriterion<>(type, fieldName, values));

        return this;
    }

    public Query<T> isNull(String fieldName) {
        criteria.add(new CompareCriterion<>(type, fieldName, CompareOperator.EQUALS, null));

        return this;
    }

    public Query<T> isNotNull(String fieldName) {
        criteria.add(new CompareCriterion<>(type, fieldName, CompareOperator.NOT_EQUALS, null));
        // criteria.add(new NotCriterion<>(type, fieldName, CompareOperator.EQUALS, null));

        return this;
    }

    public Query<T> contains(String fieldName, Object value) {
        criteria.add(new ContainsCriterion<>(type, fieldName, value));

        return this;
    }

    public QueryResult<T> select() {
        Session session = Bundle.getSessionForCurrentBundle();

        AtomicReference<QueryResult<T>> result = new AtomicReference<>();

        if (session != null) {
            if (!session.getTransaction().isActive()) {
                logger.debug(String.format("%s - Selecting an entity outside of a bundle!", this.getClass()));
                Bundle.runWithNewBundle(bundle -> result.set(select()));
            } else {
                result.set(new QueryResult<>(session
                        .createQuery(QueryUtils.generateCriteriaQuery(session, this))
                        .getResultList()
                ));
            }
        } else {
            return null;
        }

        return result.get();
    }

    public long count() {
        Session session = Bundle.getSessionForCurrentBundle();

        AtomicReference<Long> result = new AtomicReference<>();

        if (session != null) {
            if (!session.getTransaction().isActive()) {
                logger.debug(String.format("%s - Selecting entity count outside of a bundle!", this.getClass()));
                Bundle.runWithNewBundle(bundle -> result.set(count()));
            } else {
                result.set(session
                        .createQuery(QueryUtils.countResults(session, this))
                        .getSingleResult()
                );
            }
        } else {
            return 0;
        }

        return result.get();
    }

    public List<QueryCriterion<T>> getCriteria() {
        return criteria;
    }

    public T getEntity() {
        return entity;
    }

    public Query<T> setEntity(T entity) {
        this.entity = entity;

        return this;
    }

    public Class<T> getType() {
        return type;
    }
}
