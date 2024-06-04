package org.schematik.data;

import java.util.List;

public class QueryResult<T> {
    List<T> results;

    Query<T> query;

    public QueryResult(Query<T> query, List<T> results) {
        this.query = query;
        this.results = results;
    }

    public int count() {
        return results == null ? 0 : results.size();
    }

    public T ensureSingleResult() {
        if (results != null && results.size() == 1) {
            return results.get(0);
        }

        return null;
    }

    public List<T> getResults() {
        return results;
    }

    public T getFirst() {
        return results != null && !results.isEmpty() ? results.get(0) : null;
    }

    public T getLast() {
        return results != null && !results.isEmpty() ? results.get(results.size() - 1) : null;
    }

    public Long nextPage() {
        QueryResult<T> result = query.select();
        this.results = result.getResults();

        return (long) results.size();
    }

    public Long countNextPageResults() {
        if (query.isPageQuery()) {
            return query.count();
        }

        throw new RuntimeException("Query is not a paged query!");
    }

    public int getCurrentPageNumber() {
        return query.getCurrentPage();
    }
}
