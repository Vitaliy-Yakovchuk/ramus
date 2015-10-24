package com.ramussoft.pb.print.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.TableGroupablePropertyPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.print.AttributeViewer;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class TableAttributeViewer extends AbstractAttributeViewer {

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        Engine engine = dataPlugin.getEngine();
        List<Element> list = StandardAttributesPlugin.getTableElements(engine,
                attribute, element);
        Qualifier qualifier = StandardAttributesPlugin
                .getTableQualifierForAttribute(engine, attribute);
        if (list.size() > 0) {
            callback.beforePrint(printStream);
            printStream.println("<br>");
            printStream.println("<table border=1><tr>");
            List<Attribute> add = new ArrayList<Attribute>();
            List<TableGroupablePropertyPersistent> l = (List) engine
                    .getAttribute(null, attribute);

            for (Attribute attr : qualifier.getAttributes())
                if (add.indexOf(attr) < 0) {
                    String name = attr.getName();
                    int colspan = 1;
                    int rowspan = 2;
                    for (TableGroupablePropertyPersistent p : l) {
                        if ((p.getOtherAttribute() == attr.getId())
                                && (p.getName() != null)
                                && (p.getName().length() > 0)) {
                            name = p.getName();
                            rowspan = 1;
                            colspan = 0;
                            for (Attribute a1 : qualifier.getAttributes())
                                for (TableGroupablePropertyPersistent p1 : l) {
                                    if ((name.equals(p1.getName()))
                                            && (p1.getOtherAttribute() == a1
                                            .getId())) {
                                        colspan++;
                                        add.add(a1);
                                    }
                                }
                        }
                    }
                    printStream.print("<td align=\"center\" rowspan=\""
                            + rowspan + "\" colspan=\"" + colspan + "\"><b>"
                            + name + "</b></td>");
                }
            printStream.println("</tr>");

            printStream.println("<tr>");
            for (Attribute a : add) {
                printStream.print("<td align=\"center\"><b>" + a.getName()
                        + "</b></td>");
            }
            printStream.println("</tr>");

            AttributeViewerFactory factory = AttributeViewerFactory
                    .getAttributeViewverFactory();

            for (Element element2 : list) {
                printStream.println("<tr>");
                for (Attribute attribute2 : qualifier.getAttributes()) {
                    printStream.print("<td>");

                    AttributeViewer viewer = factory
                            .getAttributeViewer(attribute2);
                    if (viewer != null) {
                        EmptyAttributeViewerCallback callback2 = new EmptyAttributeViewerCallback();
                        viewer.printAttribute(printStream, dataPlugin,
                                element2, attribute2, parser, callback2);
                        if (!callback2.isBefore())
                            printStream.print("<center>-</center>");
                    } else
                        printStream.print("<center>-</center>");

                    printStream.println("</td>");
                }
                printStream.println("</tr>");
            }

            printStream.println("</table>");
            callback.afterPrint(printStream);
        }
    }

}
