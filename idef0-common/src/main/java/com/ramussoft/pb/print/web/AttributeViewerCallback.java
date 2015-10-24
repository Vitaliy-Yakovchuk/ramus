package com.ramussoft.pb.print.web;

import java.io.IOException;

import com.ramussoft.web.HTTPParser;

public interface AttributeViewerCallback {

    void beforePrint(HTTPParser.PrintStream printStream) throws IOException;

    void afterPrint(HTTPParser.PrintStream printStream) throws IOException;
}
