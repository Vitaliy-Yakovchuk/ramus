package com.ramussoft.common.persistent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to add date value.
 *
 * @author zdd
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface IntegerType {
    /**
     * Return id of value in database table, this id can be used for journal and
     * fast table field identification.
     */

    byte id() default 0;

    boolean primary() default false;
}
