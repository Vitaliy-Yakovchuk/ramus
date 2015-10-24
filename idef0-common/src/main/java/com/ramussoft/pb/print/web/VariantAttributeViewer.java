package com.ramussoft.pb.print.web;

import java.io.IOException;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.VariantPropertyPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class VariantAttributeViewer extends AbstractAttributeViewer {

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        Engine engine = dataPlugin.getEngine();
        String value = (String) engine.getAttribute(element, attribute);

        if (value != null) {
            List<VariantPropertyPersistent> list = (List<VariantPropertyPersistent>) engine
                    .getAttribute(null, attribute);
            VariantPropertyPersistent vp = null;
            for (VariantPropertyPersistent persistent : list) {
                if (persistent.getValue().equals(value))
                    vp = persistent;
            }
            if (vp != null) {
                long id = StandardAttributesPlugin.getElement(engine,
                        element.getQualifierId()).getId();
                callback.beforePrint(printStream);
                parser.printStartATeg("rows/index.html?id=" + id + "&var="
                        + vp.getVariantId() + "&attr=" + attribute.getId());
                printStream.print(value);
                parser.printEndATeg();
                callback.afterPrint(printStream);
            }
        }
    }
}
