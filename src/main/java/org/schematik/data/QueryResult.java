package org.schematik.data;

import java.util.ArrayList;
import java.util.List;

public class QueryResult<T> {
    List<T> results;

    public QueryResult(List<T> results) {
        this.results = results;
    }

    public QueryResult(T result) {
        this.results = new ArrayList<>();
        results.add(result);
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
}
