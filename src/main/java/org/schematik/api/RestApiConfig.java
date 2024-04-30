package org.schematik.api;

import io.javalin.http.Context;
import io.javalin.security.RouteRole;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.schematik.Application;
import org.schematik.jetty.JettyServer;
import org.schematik.plugins.PluginConfig;
import org.schematik.util.resource.FileResourceUtil;
import org.schematik.util.xml.XMLParser;
import org.schematik.util.xml.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

public class RestApiConfig {
    static Logger logger = LoggerFactory.getLogger(RestApiConfig.class);

    static String defaultSwaggerLocation;
    static String swaggerRootLocation;
    static boolean lockRootEndpoints;

    public static synchronized void initialize() {
        try {
            logger.info("Initializing REST apis...");

            // Get properties to be used later to set up swagger-ui
            defaultSwaggerLocation =
                    Application.getPropertyOrDefault("swagger.configUrl", "/endpoints/list");
            swaggerRootLocation =
                    Application.getPropertyOrDefault("swagger.rootUrl", "http://localhost:8090");
            lockRootEndpoints = Boolean.parseBoolean(
                    Application.getPropertyOrDefault("swagger.lockRootEndpoints", "false")
            );

            List<String> yamlEndpoints = new ArrayList<>();

            // Get the registered authentication plugin
            IRestApiAuthenticationPlugin authenticationPlugin =
                    (IRestApiAuthenticationPlugin) PluginConfig.getPluginImplementation("WebserviceAuthentication");

            // If no plugin is registered, log a warning saying this
            if (authenticationPlugin == null) {
                logger.warn(
                        "No WebserviceAuthentication plugin has been registered. Webservice security will be disabled."
                );
            }

            // Each webservice is described in a separate yaml file in the api folder in the resources.
            // Each tag in the file api.config.xml points to such a yaml file.
            XmlElement xml = XMLParser.parse(FileResourceUtil.getFileFromResource("api.config.xml"));
            List<XmlElement> webservices = xml.getElements("webservice");
            for (XmlElement webservice : webservices) {
                String descriptorYamlFile = webservice.getProperty("descriptor");
                String className = webservice.getProperty("class");

                String yamlFileContent = FileResourceUtil.readFile(
                        FileResourceUtil.getFileFromResource("api/" + descriptorYamlFile)
                );
                // Parse the yaml file specified in the <webservice> tag
                SwaggerParseResult swaggerResult = new OpenAPIParser().readContents(yamlFileContent, null, null);
                OpenAPI openApi = swaggerResult.getOpenAPI();
                Paths paths = openApi.getPaths();
                Map<String, Object> extension = openApi.getExtensions();

                Class<?> implementationClass = Class.forName(className);
                Controller controller = (Controller) implementationClass.getDeclaredConstructor().newInstance();

                String currentWebserviceYamlEndpoint = "/" + descriptorYamlFile
                        .substring(0, descriptorYamlFile.length() - 4)
                        .replace(".", "-");

                // Prepare the roles for the current webservice endpoint and register a path that returns the yaml file
                // if the user has required permissions (permissions are not compulsory).
                List<RouteRole> currentWebserviceRoles = getRolesFromExtensions(extension, authenticationPlugin);
                JettyServer.instance.app.get(
                        currentWebserviceYamlEndpoint,
                        context -> {
                            if(authenticationPlugin != null && authenticationPlugin.authenticate(context)) {
                                context.contentType("application/yaml").result(yamlFileContent);
                            }
                        },
                        currentWebserviceRoles.toArray(new RouteRole[0])
                );
                yamlEndpoints.add(currentWebserviceYamlEndpoint);

                for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                    Map<PathItem.HttpMethod, Operation> operations = pathEntry.getValue().readOperationsMap();
                    String route = pathEntry.getKey();

                    for (Map.Entry<PathItem.HttpMethod, Operation> operation : operations.entrySet()) {
                        Operation operationValue = operation.getValue();

                        // Get the operation id (the method that will be called in the handler class)
                        String method = operationValue.getOperationId();

                        // Get all roles that are specified by x-schematik-security -> x-schematik-roles
                        // of the current operation.
                        List<RouteRole> endpointRoles = new ArrayList<>();
                        List<SecurityRequirement> securityRequirements = operationValue.getSecurity();
                        if (securityRequirements != null && !securityRequirements.isEmpty()) {
                            endpointRoles = getRolesFromExtensions(
                                    operationValue.getExtensions(),
                                    authenticationPlugin
                            );
                        }

                        switch (operation.getKey()) {
                            case GET -> {
                                Method getMethod;
                                try {
                                    getMethod = controller.getClass().getDeclaredMethod(method, Context.class);
                                } catch (NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                                JettyServer.instance.app.get(
                                        route,
                                        context -> {
                                            if (authenticationPlugin != null && authenticationPlugin.authenticate(context)) {
                                                getMethod.invoke(controller, context);
                                            }
                                        },
                                        endpointRoles.toArray(new RouteRole[0])
                                );

                                logger.info(
                                        controller.getClass().getName() + "::" + method
                                        + " serves GET requests on route " + route
                                );
                            }
                            case POST -> {
                                Method postMethod;
                                try {
                                    postMethod = controller.getClass().getDeclaredMethod(method, Context.class);
                                } catch (NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                                JettyServer.instance.app.post(
                                        route,
                                        context -> {
                                            if (authenticationPlugin != null && authenticationPlugin.authenticate(context)) {
                                                postMethod.invoke(controller, context);
                                            }
                                        },
                                        endpointRoles.toArray(new RouteRole[0])
                                );

                                logger.info(
                                        controller.getClass().getName() + "::" + method
                                        + " serves POST requests on route " + route
                                );
                            }
                            case PUT -> {
                                Method putMethod;
                                try {
                                    putMethod = controller.getClass().getDeclaredMethod(method, Context.class);
                                } catch (NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                                JettyServer.instance.app.put(
                                        route,
                                        context -> {
                                            if (authenticationPlugin != null && authenticationPlugin.authenticate(context)) {
                                                putMethod.invoke(controller, context);
                                            }
                                        },
                                        endpointRoles.toArray(new RouteRole[0])
                                );

                                logger.info(
                                        controller.getClass().getName() + "::" + method
                                        + " serves PUT requests on route " + route
                                );
                            }
                            case DELETE -> {
                                Method deleteMethod;
                                try {
                                    deleteMethod = controller.getClass().getDeclaredMethod(method, Context.class);
                                } catch (NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                                JettyServer.instance.app.delete(
                                        route,
                                        context -> {
                                            if (authenticationPlugin != null && authenticationPlugin.authenticate(context)) {
                                                deleteMethod.invoke(controller, context);
                                            }
                                        },
                                        endpointRoles.toArray(new RouteRole[0])
                                );

                                logger.info(
                                        controller.getClass().getName() + "::" + method
                                        + " serves DELETE requests on route " + route
                                );
                            }
                            default -> {
                            }
                        }
                    }
                }
            }

            String yamlString = generateYamlStringForEndpoints(yamlEndpoints);
            JettyServer.instance.app.get(
                    defaultSwaggerLocation,
                    context -> context.contentType("application/yaml").result(yamlString)
            );

            // Register Swagger routes
            JettyServer.instance.app.get("/swagger", context -> {
                context.redirect("swagger-ui/index.html");
            });

//            JettyServer.instance.app.get("/", context -> {
//                JettyServer.instance.app.
//            });
        } catch (Exception e) {
            logger.error("Error while initializing REST apis", e);
        }
    }

    private static OpenAPI getOpenApi() {
        return new OpenAPI()
                .info(new Info().title(Application.getPropertyOrDefault("swagger.title", "Swagger UI"))
                        .description(Application.getPropertyOrDefault(
                                "swagger.description",
                                "Swagger UI api description")
                        )
                        .version("1.0.0")
                );
    }

    private static String generateYamlStringForEndpoints(List<String> endpoints) {
        String apiVersion = Application.getPropertyOrDefault("swagger.apiVersion", "1.0.0");

        StringBuilder result = new StringBuilder("openapi: 3.0.0\n" +
                "info:\n" +
                "  title: Endpoints\n" +
                "  version: " + apiVersion + "\n" +
                "  description: A list of all root points in the application\n" +
                "paths:\n");

        for (String endpoint : endpoints) {
            String currentEndpointString = "  " + endpoint + ":\n" +
                    "    get:\n" +
                    "      summary: Get all endpoints at " + endpoint + "\n" +
                    "      tags:\n" +
                    "        - " + endpoint + "\n" +
                    "      operationId: " + endpoint + "\n" +
                    "      responses:\n" +
                    "        '200':\n" +
                    "          description: Successfully obtain a yaml representation of all endpoints at " + swaggerRootLocation + endpoint + "\n" +
                    "          content:\n" +
                    "            text/plain:\n" +
                    "              schema:\n" +
                    "                type: string\n";
            result.append(currentEndpointString);
        }

        return result.toString();
    }

    private static List<RouteRole> getRolesFromExtensions(
            Map<String, Object> extensions,
            IRestApiAuthenticationPlugin authenticationPlugin
    ) {
        List<RouteRole> endpointRoles = new ArrayList<>();

        if (extensions == null) {
            return endpointRoles;
        }

        // Get all roles that are specified by the x-schematik-security -> x-schematik-roles
        // path of the current operation.
        for (var param : extensions.entrySet()) {
            if (param.getKey().equals("x-schematik-security")) {
                if (param.getValue() instanceof Map<?, ?> rolesMap
                        && rolesMap.get("x-schematik-roles") instanceof List<?> tempEndpointRoles) {
                    for (var endpointRole : tempEndpointRoles) {
                        RouteRole currentEndpointRole =
                                Objects.requireNonNull(authenticationPlugin)
                                        .roleFromString((String) endpointRole);
                        endpointRoles.add(currentEndpointRole);
                    }
                }
            }
        }

        return endpointRoles;
    }
}
