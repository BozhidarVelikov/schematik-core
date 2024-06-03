package org.schematik.data.criterion;

import org.schematik.data.CompareOperator;

public class CompareCriterion<T> extends QueryCriterion<T> {
    Class<T> entityClass;
    String fieldName;
    CompareOperator operator;
    Object value;

    public CompareCriterion(Class<T> entityClass, String fieldName, CompareOperator operator, Object value) {
        this.entityClass = entityClass;
        this.fieldName = fieldName;
        this.operator = operator;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public CompareOperator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }
}
