package org.schematik.api;

import io.javalin.http.HttpStatus;

public class ResponseEntity<T> {
    T entity;
    HttpStatus statusCode;

    public ResponseEntity(T entity, HttpStatus statusCode) {
        this.entity = entity;
        this.statusCode = statusCode;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }
}
