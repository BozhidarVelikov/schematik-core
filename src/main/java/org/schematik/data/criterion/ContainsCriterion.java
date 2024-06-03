package org.schematik.data.criterion;

import org.schematik.data.CompareOperator;

public class ContainsCriterion<T> extends QueryCriterion<T> {
    Class<T> entityClass;
    String fieldName;
    Object value;

    public ContainsCriterion(Class<T> entityClass, String fieldName, Object value) {
        this.entityClass = entityClass;
        this.fieldName = fieldName;
        this.value = value;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }
}
