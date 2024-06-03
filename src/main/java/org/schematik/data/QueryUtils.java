package org.schematik.data;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.schematik.data.criterion.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryUtils {
    public static <T> CriteriaQuery<T> generateCriteriaQuery(
            Session session,
            Query<T> query
    ) {
        Class<T> entityClass = query.getType();

        HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = builder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        for (QueryCriterion<T> criterion : query.getCriteria()) {
            predicates.add(criterionToWhereClause(criterion, builder, root));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return criteriaQuery;
    }

    public static <T> CriteriaQuery<Long> countResults(
            Session session,
            Query<T> query
    ) {
        Class<T> entityClass = query.getType();

        HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(builder.count(root));

        List<Predicate> predicates = new ArrayList<>();
        for (QueryCriterion<T> criterion : query.getCriteria()) {
            predicates.add(criterionToWhereClause(criterion, builder, root));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));



        return criteriaQuery;
    }

    private static <T> Predicate criterionToWhereClause(
            QueryCriterion<T> queryCriterion,
            HibernateCriteriaBuilder builder,
            Root<T> root
    ) {
        if (queryCriterion instanceof OrCriterion<T> orCriterion) {
            Predicate[] orCriteriaPredicates = orCriterion.criteria
                    .stream()
                    // .map(criterion -> criterionToPredicate(criterion, builder, root))
                    .map(criterion -> criterionToPredicate(criterion, builder, root))
                    .toList()
                    .toArray(new Predicate[0]);

            return builder.or(orCriteriaPredicates);
        } else {
            // criteriaQuery.where(criterionToPredicate(queryCriterion, builder, root));
            return criterionToPredicate(queryCriterion, builder, root);
        }
    }

    private static <T, X extends Comparable<? super X>> Predicate criterionToPredicate(
            QueryCriterion<T> criterion,
            HibernateCriteriaBuilder builder,
            Root<T> root
    ) {
        Predicate result = null;

        if (criterion instanceof CompareCriterion<T> compareCriterion) {
            Comparable value = (Comparable) compareCriterion.getValue();
            switch (compareCriterion.getOperator()) {
                case EQUALS -> result = builder.equal(
                        root.get(compareCriterion.getFieldName()),
                        value
                );
                case NOT_EQUALS -> result = builder.notEqual(
                        root.get(compareCriterion.getFieldName()),
                        value
                );
                case GREATER_THAN -> result = builder.greaterThan(
                        root.get(compareCriterion.getFieldName()),
                        value
                );
                case GREATER_THAN_OR_EQUALS -> result = builder.greaterThanOrEqualTo(
                        root.get(compareCriterion.getFieldName()),
                        value
                );
                case LESS_THAN -> result = builder.lessThan(
                        root.get(compareCriterion.getFieldName()),
                        value
                );
                case LESS_THAN_OR_EQUALS -> result = builder.lessThanOrEqualTo(
                        root.get(compareCriterion.getFieldName()),
                        value
                );
                case LIKE -> result = builder.like(
                        root.get(compareCriterion.getFieldName()),
                        "%" + value + "%"
                );
                case NOT_LIKE -> result = builder.notLike(
                        root.get(compareCriterion.getFieldName()),
                        "%" + value + "%"
                );
            }

            if (criterion instanceof NotCriterion<T>) {
                result = result.not();
            }
        } else if (criterion instanceof BetweenCriterion<T> betweenCriterion) {
            Comparable min = (Comparable) betweenCriterion.getMin();
            Comparable max = (Comparable) betweenCriterion.getMax();
            result = builder.and(
                    builder.greaterThanOrEqualTo(
                            root.get(betweenCriterion.getFieldName()),
                            min
                    ),
                    builder.lessThanOrEqualTo(
                            root.get(betweenCriterion.getFieldName()),
                            max
                    )
            );
        } else if (criterion instanceof InCriterion<T> inCriterion) {
            result = root.get(inCriterion.getFieldName()).in(inCriterion.getValues());
        } else if (criterion instanceof ContainsCriterion<T> containsCriterion) {
            try {
                Class<?> fieldClass =
                        containsCriterion.getEntityClass().getDeclaredField(containsCriterion.getFieldName()).getType();

                result = builder.isMember(
                        containsCriterion.getValue(),
                        root.get(containsCriterion.getFieldName())
                );

//                if (Collection.class.isAssignableFrom(fieldClass)) {
//                    result = builder.isMember(
//                            containsCriterion.getValue(),
//                            root.get(containsCriterion.getFieldName())
//                    );
//                } else if (Array.class.isAssignableFrom(fieldClass)) {
//                    result = builder.arrayContains(
//                            root.get(containsCriterion.getFieldName()),
//                            containsCriterion.getValue()
//                    );
//                } else {
//                    throw new RuntimeException(String.format(
//                            "Contains criterion can only be used with arrays and collections. [%s::%s is of type %s]",
//                            containsCriterion.getEntityClass().getName(),
//                            containsCriterion.getFieldName(),
//                            fieldClass.getName()
//                    ));
//                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Unknown criterion type!");
        }

        return result;
    }
}
