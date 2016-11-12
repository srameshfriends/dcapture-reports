package excel.accounting.db;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ColumnIndex {

    String[] columns() default {};
}
