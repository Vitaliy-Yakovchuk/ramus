package com.ramussoft.core;

import java.io.ByteArrayInputStream;
import java.util.jar.JarOutputStream;

public class Pack200 {
    public static Unpacker newUnpacker() {
        return new Unpacker();
    }

    public static class Unpacker {
        public void unpack(ByteArrayInputStream byteArrayInputStream, JarOutputStream jostream) {
            throw new UnsupportedOperationException();
        }
    }
}
