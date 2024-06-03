package org.schematik.api;

import io.javalin.http.Context;
import io.javalin.security.RouteRole;
import org.schematik.plugin.ISchematikPlugin;

public interface IRestApiAuthenticationPlugin extends ISchematikPlugin {
    boolean authenticate(Context context);

    RouteRole roleFromString(String roleString);
}
