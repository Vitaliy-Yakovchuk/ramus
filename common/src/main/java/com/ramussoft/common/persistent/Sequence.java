package com.ramussoft.common.persistent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Can be used in some packages, not using by Ramus.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface Sequence {
    String name();
}
