package blue.endless.enoki.util;

import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that all parameters, return types, and fields in the annotated package or type are non-nullable if not
 * otherwise annotated.
 */
@Retention(RUNTIME)
@Target({ TYPE, PACKAGE, MODULE })
public @interface NotNullByDefault {
}
