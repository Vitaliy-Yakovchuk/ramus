package com.ramussoft.common.persistent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this annotation to specify table name. The name will be used no as
 * absolute table name in database. It will be used to as end of table name. For
 * example if name of table for attribute "integer" if core plug-in will, the
 * table in the database will be: "core_attribute_integer".
 *
 * @author zdd
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name();

    TableType type() default TableType.ONE_TO_ONE;
}
