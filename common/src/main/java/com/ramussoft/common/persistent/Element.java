package com.ramussoft.common.persistent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation for foreign keys. If persistent object has such field, it
 * will be automatically deleted, when some element will be deleted.
 *
 * @author zdd
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Element {
    /**
     * Return id of value in database table, this id can be used for journal and
     * fast table field identification.
     */

    byte id() default 0;

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
