package com.ramussoft.pb.data.negine;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.OutputStream;

public class IDLWriter {

    private final OutputStream out;

    private final String encoding;

    private String beforeLine = "";

    public IDLWriter(OutputStream out, String encoding) {
        this.out = out;
        this.encoding = encoding;
    }

    public void println(String string) throws IOException {
        out.write(beforeLine.getBytes(encoding));
        out.write(string.getBytes(encoding));
        println();
    }

    public void println() throws IOException {
        out.write("\r\n".getBytes(encoding));
    }

    public void close() throws IOException {
        out.close();
    }

    public void p1(String string) throws IOException {
        println(string + " ;");
    }

    public void p2(String pattern, Object... arguments) throws IOException {
        Object[] c = new Object[arguments.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = IDLExporter.q(String.valueOf(arguments[i]));
        }
        p1(format(pattern, c));
    }

    public void println(String pattern, Object... arguments) throws IOException {
        println(format(pattern, arguments));
    }

    public void right() {
        beforeLine += "  ";
    }

    public void left() {
        if (beforeLine.length() >= 2) {
            beforeLine = beforeLine.substring(2);
        }
    }
}
