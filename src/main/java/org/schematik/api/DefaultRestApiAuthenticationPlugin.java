package org.schematik.api;

import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import org.schematik.util.resource.FileResourceUtil;
import org.schematik.util.xml.XMLParser;
import org.schematik.util.xml.XmlElement;

import java.util.*;

public class DefaultRestApiAuthenticationPlugin implements IRestApiAuthenticationPlugin {
    record Pair(String a, String b) {}
    Map<Pair, List<DefaultRestApiRole>> userRolesMap;

    @Override
    public void register() {
        userRolesMap = new HashMap<>();
        try {
            XmlElement xml = XMLParser.parse(FileResourceUtil.getFileFromResource("api/credentials.xml"));
            List<XmlElement> users = xml.getElements("user");
            for (XmlElement user : users) {
                String username = user.getElement("username").getValue();
                String password = user.getElement("password").getValue();
                List<DefaultRestApiRole> userRoles = new ArrayList<>();

                List<XmlElement> roles = user.getElement("roles").getElements("role");
                for (XmlElement role : roles) {
                    userRoles.add(DefaultRestApiRole.valueOf(role.getValue()));
                }

                userRolesMap.put(new Pair(username, password), userRoles);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean authenticate(Context context) {
        Set<RouteRole> roles = context.routeRoles();

        if (roles.isEmpty() || roles.contains(DefaultRestApiRole.ANY)) {
            return true;
        }

        List<DefaultRestApiRole> userRoles = userRoles(context);
        if (userRoles.stream().anyMatch(roles::contains) || userRoles.contains(DefaultRestApiRole.ADMIN)) {
            return true;
        }

        context.header(Header.WWW_AUTHENTICATE, "Basic");
        throw new UnauthorizedResponse();
    }

    @Override
    public RouteRole roleFromString(String roleString) {
        return DefaultRestApiRole.valueOf(roleString);
    }

    private List<DefaultRestApiRole> userRoles(Context context) {
        return Optional.ofNullable(context.basicAuthCredentials())
                .map(credentials -> userRolesMap.getOrDefault(new Pair(credentials.getUsername(), credentials.getPassword()), List.of()))
                .orElse(List.of());
    }
}
