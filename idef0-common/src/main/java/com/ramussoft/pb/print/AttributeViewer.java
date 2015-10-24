package com.ramussoft.pb.print;

import java.io.IOException;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.print.web.AttributeViewerCallback;
import com.ramussoft.web.HTTPParser;

public interface AttributeViewer {

    void printAttribute(HTTPParser.PrintStream printStream,
                        DataPlugin dataPlugin, Element element, Attribute attribute,
                        HTTPParser parser, AttributeViewerCallback callback)
            throws IOException;
}
