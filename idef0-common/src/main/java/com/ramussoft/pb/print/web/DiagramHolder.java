package com.ramussoft.pb.print.web;

import java.util.ArrayList;

public class DiagramHolder {

    private final ArrayList<String> lines = new ArrayList<String>();

    void println(final String s) {
        lines.add(s + "\n");
    }

    public void print(final String s) {
        lines.add(s);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final String l : lines)
            sb.append(l);
        return sb.toString();
    }
}
