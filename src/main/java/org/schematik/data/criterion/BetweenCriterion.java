package org.schematik.data.criterion;

public class BetweenCriterion<T> extends QueryCriterion<T> {
    Class<T> entityClass;
    String fieldName;
    Object min;
    Object max;

    public BetweenCriterion(
            Class<T> entityClass,
            String fieldName,
            Object min,
            Object max
    ) {
        this.entityClass = entityClass;
        this.fieldName = fieldName;
        this.min = min;
        this.max = max;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getMin() {
        return min;
    }

    public Object getMax() {
        return max;
    }
}
