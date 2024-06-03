package org.schematik.data.criterion;

public class InCriterion<T> extends QueryCriterion<T> {
    Class<T> entityClass;
    String fieldName;
    Object[] values;

    public InCriterion(Class<T> entityClass, String fieldName, Object... values) {
        this.entityClass = entityClass;
        this.fieldName = fieldName;
        this.values = values;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object[] getValues() {
        return values;
    }
}
