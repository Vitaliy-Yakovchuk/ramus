package com.ramussoft.common.attribute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to link persist to some attribute.
 *
 * @author zdd
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {
    /**
     * Return id of value in database table, this id can be used for journal and
     * fast table field identification.
     */

    int id();

    /**
     * <code>true</code> if element sets automatically.
     */

    boolean autoset();

    /**
     * Is this field primary key or part of primary key, default is
     * <code>false</code>.
     */

    boolean primary() default false;
}
