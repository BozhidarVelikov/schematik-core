package org.schematik.api.security;

import io.javalin.security.RouteRole;

public enum DefaultUserRole implements RouteRole {
    USER("USER"),
    ADMIN("ADMIN"),
    GUEST("GUEST");

    private final String name;

    DefaultUserRole(String s) {
        this.name = s;
    }

    public String toString() {
        return this.name;
    }
}
