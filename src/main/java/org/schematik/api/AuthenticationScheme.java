package org.schematik.api;

public enum AuthenticationScheme {
    BASIC_AUTH("BASIC_AUTH"),
    API_KEY("API_KEY"),
    BEARER_TOKEN("BEARER_TOKEN"),
    O_AUTH_2("O_AUTH_2");

    private final String name;

    AuthenticationScheme(String s) {
        name = s;
    }

    public String toString() {
        return this.name;
    }
}
