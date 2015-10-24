package com.ramussoft.pb.print.web;

import java.io.IOException;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class ElementListAttributeViewer extends AbstractAttributeViewer {

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        Engine engine = dataPlugin.getEngine();
        Qualifier qualifier = engine.getQualifier(element.getQualifierId());
        List<ElementListPersistent> list = (List<ElementListPersistent>) engine
                .getAttribute(element, attribute);
        ElementListPropertyPersistent pp = (ElementListPropertyPersistent) engine
                .getAttribute(null, attribute);

        boolean left = qualifier.getId() == pp.getQualifier2();
        if (list.size() > 0) {
            callback.beforePrint(printStream);
            printStream.println("<br>");
            for (ElementListPersistent p : list) {
                long id;
                if (left)
                    id = p.getElement1Id();
                else
                    id = p.getElement2Id();
                Element element2 = engine.getElement(id);
                if (element2 != null) {
                    printLinkToElement(element2, parser, printStream);
                    printStream.println("<br>");
                }
            }
            callback.afterPrint(printStream);
        }
    }

}
