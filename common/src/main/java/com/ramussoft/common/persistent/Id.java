package com.ramussoft.common.persistent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to add id. Id is always primary.
 *
 * @author zdd
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    /**
     * Return id of value in database table, this id can be used for journal and
     * fast table field identification.
     */

    byte id() default 0;
}
