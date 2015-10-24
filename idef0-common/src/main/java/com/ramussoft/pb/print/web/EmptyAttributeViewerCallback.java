package com.ramussoft.pb.print.web;

import java.io.IOException;

import com.ramussoft.web.HTTPParser.PrintStream;

public class EmptyAttributeViewerCallback implements AttributeViewerCallback {

    private boolean before;

    private boolean after;

    @Override
    public void afterPrint(PrintStream printStream) throws IOException {
        after = true;
    }

    @Override
    public void beforePrint(PrintStream printStream) throws IOException {
        before = true;
    }

    public boolean isBefore() {
        return before;
    }

    public boolean isAfter() {
        return after;
    }

}
