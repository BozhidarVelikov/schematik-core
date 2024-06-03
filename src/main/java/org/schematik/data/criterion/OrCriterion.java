package org.schematik.data.criterion;

import org.schematik.data.CompareOperator;
import org.schematik.data.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class OrCriterion<T> extends QueryCriterion<T> {
    Class<T> entityClass;
    public List<QueryCriterion<T>> criteria;
    public OrCriterion(Class<T> entityClass) {
        this.entityClass = entityClass;
        criteria = new ArrayList<>();
    }

    public OrCriterion<T> compare(String fieldName, CompareOperator operator, Object value) {
        criteria.add(new CompareCriterion<>(entityClass, fieldName, operator, value));

        return this;
    }

    public OrCriterion<T> or(Function<OrCriterion<T>, OrCriterion<T>> criteria) {
        this.criteria.add(criteria.apply(new OrCriterion<T>(entityClass)));

        return this;
    }

    public OrCriterion<T> not(String fieldName, CompareOperator operator, Object value) {
        criteria.add(new NotCriterion<>(entityClass, fieldName, operator, value));

        return this;
    }

    public OrCriterion<T> between(String fieldName, Object min, Object max) {
        criteria.add(new BetweenCriterion<>(entityClass, fieldName, min, max));

        return this;
    }

    public OrCriterion<T> in(String fieldName, Object... values) {
        criteria.add(new InCriterion<>(entityClass, fieldName, values));

        return this;
    }

    public OrCriterion<T> isNull(String fieldName) {
        criteria.add(new CompareCriterion<>(entityClass, fieldName, CompareOperator.EQUALS, null));

        return this;
    }

    public OrCriterion<T> isNotNull(String fieldName) {
        criteria.add(new CompareCriterion<>(entityClass, fieldName, CompareOperator.NOT_EQUALS, null));
        // criteria.add(new NotCriterion<>(type, fieldName, CompareOperator.EQUALS, null));

        return this;
    }

    public OrCriterion<T> contains(String fieldName, Object value) {
        criteria.add(new ContainsCriterion<>(entityClass, fieldName, value));

        return this;
    }
}
