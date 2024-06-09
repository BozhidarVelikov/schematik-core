package org.schematik.api.annotation.request;

import io.javalin.security.RouteRole;
import org.schematik.api.security.DefaultUserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Post {
    String endpoint() default "";
    Class<? extends Enum<? extends RouteRole>> roleClass() default DefaultUserRole.class;
    String[] roles() default {};
}
