package com.ramussoft.pb.print.web;

import java.io.IOException;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.OtherElementPropertyPersistent;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class OtherElementAttributeViewer extends AbstractAttributeViewer {

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        Engine engine = dataPlugin.getEngine();
        Long id = (Long) engine.getAttribute(element, attribute);
        if (id != null) {
            Element element2 = engine.getElement(id);
            if (element2 != null) {
                OtherElementPropertyPersistent pp = (OtherElementPropertyPersistent) engine
                        .getAttribute(null, attribute);
                Attribute other = engine.getAttribute(pp
                        .getQualifierAttribute());
                if (other != null) {
                    callback.beforePrint(printStream);

                    printLinkToElement(element2, engine.getAttribute(element2,
                            other), parser, printStream);

                    callback.afterPrint(printStream);
                }
            }

        }
    }

}
