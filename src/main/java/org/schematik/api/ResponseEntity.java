package org.schematik.api;

import io.javalin.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntity<T> {
    T entity;
    HttpStatus statusCode;

    Map<String, String> headers;

    public ResponseEntity(T entity, HttpStatus statusCode) {
        this.entity = entity;
        this.statusCode = statusCode;
        this.headers = new HashMap<>();
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public void removeHeader(String header) {
        headers.remove(header);
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
