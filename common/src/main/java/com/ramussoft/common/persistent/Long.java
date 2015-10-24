package com.ramussoft.common.persistent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to ad long value.
 *
 * @author zdd
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Long {
    /**
     * Return id of value in database table, this id can be used for journal and
     * fast table field identification.
     */

    byte id() default 0;

    /**
     * Is this field primary key or part of primary key, default is
     * <code>false</code>.
     */

    boolean primary() default false;

    boolean setDefaultValue() default false;

    long defaultValue() default -1l;
}
