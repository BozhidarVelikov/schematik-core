package org.schematik.api;

import io.javalin.security.RouteRole;

public enum DefaultRestApiRole implements RouteRole {
    ANY ("ANY"),
    USER ("USER"),
    ADMIN ("ADMIN");

    private final String name;

    DefaultRestApiRole(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
