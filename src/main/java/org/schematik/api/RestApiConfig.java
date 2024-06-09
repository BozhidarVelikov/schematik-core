package org.schematik.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.security.RouteRole;
import org.schematik.api.annotation.Controller;
import org.schematik.api.annotation.parameter.*;
import org.schematik.api.annotation.request.*;
import org.schematik.api.security.RouteRoleUtils;
import org.schematik.gson.LocalDateAdapter;
import org.schematik.jetty.JettyServer;
import org.schematik.plugin.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class RestApiConfig {
    static Logger logger = LoggerFactory.getLogger(RestApiConfig.class);

    static Gson gson;

    static Set<Class<? extends Annotation>> requestTypeAnnotations = Set.of(
            Get.class,
            Post.class,
            Put.class,
            Delete.class,
            Patch.class,
            Head.class,
            Options.class
    );

    static Set<Class<? extends Annotation>> parameterTypeAnnotations = Set.of(
            PathParam.class,
            QueryParam.class,
            RequestBody.class
    );

    public static synchronized void initialize() {
        GsonBuilder builder = (new GsonBuilder())
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gson = builder.create();

        try {
            logger.info("Initializing REST apis...");

            // Get the registered authentication plugin
            IRestApiAuthenticationPlugin authenticationPlugin =
                    (IRestApiAuthenticationPlugin) PluginConfig.getPluginImplementation("WebserviceAuthentication");

            // If no plugin is registered, log a warning
            if (authenticationPlugin == null) {
                logger.warn(
                        "No WebserviceAuthentication plugin has been registered. Webservice security will be disabled."
                );
            }

            Set<Class<?>> controllerClasses = RestApiUtils.getControllerClasses();
            controllerClasses.forEach(controllerClass -> {
                try {
                    Object controllerInstance = controllerClass.getConstructor().newInstance();

                    String controllerEndpoint = processEndpointString(
                            controllerClass.getAnnotation(Controller.class).endpoint()
                    );


                    Method[] methods = controllerClass.getDeclaredMethods();
                    Arrays.stream(methods).forEach(method -> {
                        int numberOfRequestAnnotations = 0;
                        Annotation requestAnnotation = null;
                        for (Annotation annotation : method.getDeclaredAnnotations()) {
                            if (requestTypeAnnotations.contains(annotation.annotationType())) {
                                numberOfRequestAnnotations++;
                                requestAnnotation = annotation;
                            }
                        }

                        if (numberOfRequestAnnotations == 0) {
                            return;
                        }

                        if (numberOfRequestAnnotations > 1) {
                            logger.error(String.format(
                                    "%s::%s has more than one request type annotations. Only one request type annotation is allowed per method. Skipping...",
                                    controllerClass.getName(),
                                    method.getName()
                            ));
                            return;
                        }

                        String endpoint;
                        List<RouteRole> roles = new ArrayList<>();
                        try {
                            endpoint = processEndpointString(requestAnnotation.annotationType()
                                    .getDeclaredMethod("endpoint")
                                    .invoke(requestAnnotation)
                                    .toString()
                            );
                            endpoint = controllerEndpoint + endpoint;

                            Class<? extends Enum<? extends RouteRole>> roleClass =
                                    (Class<? extends Enum<? extends RouteRole>>) requestAnnotation.annotationType()
                                    .getDeclaredMethod("roleClass")
                                    .invoke(requestAnnotation);
                            String[] roleNames = (String[]) requestAnnotation.annotationType()
                                    .getDeclaredMethod("roles")
                                    .invoke(requestAnnotation);
                            Arrays.stream(roleNames).forEach(roleName ->
                                    roles.add(RouteRoleUtils.routeRoleFromString(roleClass, roleName))
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        handleRequestAnnotation(
                                requestAnnotation,
                                endpoint,
                                method,
                                controllerInstance,
                                authenticationPlugin,
                                roles.toArray(new RouteRole[0])
                        );

                        logEndpointInfo(requestAnnotation, endpoint);
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });


        } catch (Exception e) {
            logger.error("Error while initializing REST apis", e);
        }
    }

    private static String processEndpointString(String endpoint) {
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        return endpoint;
    }

    private static Object stringToTypedObject(Type type, String value) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Float.class || type == float.class) {
            return Float.parseFloat(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == Character.class || type == char.class) {
            return value;
        } else if (type == String.class) {
            return value;
        } else if (type == BigDecimal.class) {
            return new BigDecimal(value);
        } else if (type == BigInteger.class) {
            return new BigInteger(value);
        } else if (type == LocalDate.class) {
            return LocalDate.parse(value);
        } else if (type == LocalTime.class) {
            return LocalTime.parse(value);
        } else if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type.getTypeName());
        }
    }

    private static List<Object> buildParametersForMethod(Method method, Context context) {
        List<Object> parameters = new ArrayList<>();

        Parameter[] methodParameters = method.getParameters();
        if (methodParameters.length == 1 && methodParameters[0].getType().equals(Context.class)) {
            return Collections.singletonList(context);
        }

        for (Parameter parameter : methodParameters) {
            int numberOfParameterAnnotations = 0;
            Annotation parameterAnnotation = null;
            for (Annotation annotation : parameter.getDeclaredAnnotations()) {
                if (parameterTypeAnnotations.contains(annotation.annotationType())) {
                    numberOfParameterAnnotations++;
                    parameterAnnotation = annotation;
                }
            }

            if (numberOfParameterAnnotations == 0) {
                break;
            }

            if (numberOfParameterAnnotations > 1) {
                throw new RuntimeException(String.format(
                        "Parameter %s in method %s has more than one parameter annotations. Only one parameter annotation is allowed per parameter.",
                        parameter.getName(),
                        method.getName()
                ));
            }

            Type parameterType = parameter.getType();
            if (parameterAnnotation instanceof PathParam pathParamAnnotation) {
                String parameterName = pathParamAnnotation.name().isEmpty()
                        ? parameter.getName()
                        : pathParamAnnotation.name();

                String parameterValue = context.pathParam(parameterName);

                parameters.add(stringToTypedObject(parameterType, parameterValue));
            } else if (parameterAnnotation instanceof QueryParam queryParamAnnotation) {
                String parameterName = queryParamAnnotation.name().isEmpty()
                        ? parameter.getName()
                        : queryParamAnnotation.name();

                String parameterValue = context.queryParam(parameterName);

                parameters.add(stringToTypedObject(parameterType, parameterValue));
            } else if (parameterAnnotation instanceof RequestBody) {
                parameters.add(context.jsonMapper().fromJsonString(context.body(), parameterType));
            }
        }

        return parameters;
    }

    private static void sendResponse(Context context, Object value) {
        if (value == null) {
            context.result();
            return;
        }

        Class<?> valueClass = value.getClass();

        String typedObjectString = typedObjectToString(valueClass, value);

        if (typedObjectString != null) {
            context.result(typedObjectString);
        } else if (value instanceof Collection<?> collection) {
            StringBuilder result = new StringBuilder("[");
            result.append(collection.stream()
                    .map(collectionItem -> {
                        Class<?> collectionItemClass = collectionItem.getClass();
                        return context.jsonMapper().toJsonString(collectionItem, collectionItemClass);
                    })
                    .collect(Collectors.joining(", "))
            );
            result.append("]");
            context.result(result.toString());
        } else { // if (valueClass.isAnnotationPresent(Entity.class)) {
            context.result(context.jsonMapper().toJsonString(value, valueClass));
        }
    }

    private static String typedObjectToString(Type type, Object value) {
        if (type == Integer.class || type == int.class
            || type == Long.class || type == long.class
            || type == Double.class || type == double.class
            || type == Float.class || type == float.class
            || type == Boolean.class || type == boolean.class
            || type == Character.class || type == char.class
            || type == String.class
            || type == BigDecimal.class
            || type == BigInteger.class
            || type == LocalDate.class
            || type == LocalTime.class
            || type == LocalDateTime.class) {
            return value.toString();
        }

        return null;
    }

    private static void executeMethodForContext(
            Method method,
            Context context,
            Object controllerInstance,
            IRestApiAuthenticationPlugin authenticationPlugin
    ) throws InvocationTargetException, IllegalAccessException {
        List<Object> parameters = buildParametersForMethod(method, context);

        Object returnValue;

        // Authenticate if needed
        if(authenticationPlugin != null) {
            if (authenticationPlugin.authenticate(context)) {
                returnValue = method.invoke(controllerInstance, parameters.toArray());
            } else {
                context.status(HttpStatus.UNAUTHORIZED);
                context.result("Forbidden: You don't have access to this resource!");
                return;
            }
        } else {
            returnValue = method.invoke(controllerInstance, parameters.toArray());
        }

        if (returnValue instanceof ResponseEntity<?> responseEntity) {
            context.status(responseEntity.statusCode);

            // Set headers
            for (Map.Entry<String, String> header : responseEntity.headers.entrySet()) {
                context.header(header.getKey(), header.getValue());
            }

            sendResponse(context, responseEntity.entity);
        } else {
            context.status(HttpStatus.OK);
            sendResponse(context, returnValue);
        }
    }

    private static void handleRequestAnnotation(
            Annotation requestAnnotation,
            String endpoint,
            Method method,
            Object controllerInstance,
            IRestApiAuthenticationPlugin authenticationPlugin,
            RouteRole[] roles
    ) {
        if (requestAnnotation instanceof Get) {
            JettyServer.instance.app.get(
                    endpoint,
                    context -> executeMethodForContext(
                            method,
                            context,
                            controllerInstance,
                            authenticationPlugin
                    ),
                    roles
            );
        } else if (requestAnnotation instanceof Post) {
            JettyServer.instance.app.post(
                    endpoint,
                    context -> executeMethodForContext(
                            method,
                            context,
                            controllerInstance,
                            authenticationPlugin
                    ),
                    roles
            );
        } else if (requestAnnotation instanceof Put) {
            JettyServer.instance.app.put(
                    endpoint,
                    context -> executeMethodForContext(
                            method,
                            context,
                            controllerInstance,
                            authenticationPlugin
                    ),
                    roles
            );
        } else if (requestAnnotation instanceof Delete) {
            JettyServer.instance.app.delete(
                    endpoint,
                    context -> executeMethodForContext(
                            method,
                            context,
                            controllerInstance,
                            authenticationPlugin
                    ),
                    roles
            );
        } else if (requestAnnotation instanceof Patch) {
            JettyServer.instance.app.patch(
                    endpoint,
                    context -> executeMethodForContext(
                            method,
                            context,
                            controllerInstance,
                            authenticationPlugin
                    ),
                    roles
            );
        } else if (requestAnnotation instanceof Head) {
            JettyServer.instance.app.head(
                    endpoint,
                    context -> executeMethodForContext(
                            method,
                            context,
                            controllerInstance,
                            authenticationPlugin
                    ),
                    roles
            );
        } else if (requestAnnotation instanceof Options) {
            JettyServer.instance.app.options(
                    endpoint,
                    context -> executeMethodForContext(
                            method,
                            context,
                            controllerInstance,
                            authenticationPlugin
                    ),
                    roles
            );
        }
    }

    private static String httpRequestFromAnnotation(Annotation annotation) {
        if (annotation instanceof Get) {
            return "GET";
        }

        if (annotation instanceof Post) {
            return "POST";
        }

        if (annotation instanceof Put) {
            return "PUT";
        }

        if (annotation instanceof Delete) {
            return "DELETE";
        }

        if (annotation instanceof Patch) {
            return "PATCH";
        }

        if (annotation instanceof Head) {
            return "HEAD";
        }

        if (annotation instanceof Options) {
            return "OPTIONS";
        }

        throw new RuntimeException("Unknown HTTP request type annotation: " + annotation.annotationType());
    }

    private static void logEndpointInfo(Annotation requestAnnotation, String endpoint) {
        if (logger.isDebugEnabled()) {
            String httpRequest = httpRequestFromAnnotation(requestAnnotation);

            logger.debug("Registered " + httpRequest + " endpoint at " + endpoint);
        }
    }
}
