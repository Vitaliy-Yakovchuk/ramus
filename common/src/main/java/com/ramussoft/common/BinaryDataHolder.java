package com.ramussoft.common;

import java.io.Serializable;

/**
 * Class holds attribute data in bytes.
 *
 * @author zdd
 */

public class BinaryDataHolder implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 18753149250181111L;

    byte[][] objects;

    public byte[][] getObjects() {
        return objects;
    }

}
