package org.schematik.api.security;

import io.javalin.security.RouteRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteRoleUtils {
    static Logger logger = LoggerFactory.getLogger(RouteRoleUtils.class);

    public static <E extends Enum<? extends RouteRole>> RouteRole routeRoleFromString(Class<E> enumClass, String value) {
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equals(value)) {
                return (RouteRole) enumConstant;
            }
        }

        throw new IllegalArgumentException("Enum constant does not implement RouteRole interface");
    }
}
