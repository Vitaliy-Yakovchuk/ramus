package com.ramussoft.pack200;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Pack200 {
    public static Unpacker newUnpacker() {
        return new Unpacker();
    }

    public static Packer newPacker() {
        return new Packer();
    }

    public static class Unpacker {
        public void unpack(File file, JarOutputStream jostream) {
            throw new UnsupportedOperationException();
        }
    }

    public static class Packer {
        public void pack(JarFile input, OutputStream stream) {
            throw new UnsupportedOperationException();
        }
    }
}
