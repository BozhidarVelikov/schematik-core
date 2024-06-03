package org.schematik.data.criterion;

import org.schematik.data.CompareOperator;

public class NotCriterion<T> extends CompareCriterion<T> {
    public NotCriterion(Class<T> entityClass, String field, CompareOperator operator, Object value) {
        super(entityClass, field, operator, value);
    }
}
