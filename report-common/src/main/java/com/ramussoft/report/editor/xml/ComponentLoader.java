package com.ramussoft.report.editor.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ramussoft.common.Engine;
import com.ramussoft.report.editor.xml.components.Label;
import com.ramussoft.report.editor.xml.components.Report;
import com.ramussoft.report.editor.xml.components.Table;
import com.ramussoft.report.editor.xml.components.TableColumn;
import com.ramussoft.report.editor.xml.components.XMLComponent;

public class ComponentLoader extends DefaultHandler {

    private Report report;

    private ReportEditor reportEditor;

    private XMLComponent current;

    private Table currentTable;

    private Attributes attributes;

    private int currentTableHeadColumn;

    private List<XMLComponent> yComponents = new ArrayList<XMLComponent>();

    private String text = "";

    private int currentTableBodyColumn;

    private Engine engine;

    private boolean ignoreBaseQualifier;

    public ComponentLoader(Engine engine, boolean ignoreBaseQualifier) {
        this.engine = engine;
        this.ignoreBaseQualifier = ignoreBaseQualifier;
    }

    public void parse(ReportEditor reportEditor, InputStream stream)
            throws IOException, SAXException {
        this.report = new Report();
        this.current = report;
        this.reportEditor = reportEditor;

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
            parser.parse(stream, this);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        text = "";
        if (qName.equals("attribute")) {
            this.attributes = attributes;
        } else if (qName.equals("Table")) {
            currentTable = new Table();
            current = currentTable;
            yComponents.add(current);
            currentTableHeadColumn = 0;
            currentTableBodyColumn = 0;
        } else if (qName.equals("Label")) {
            current = new Label();
            yComponents.add(current);
        } else if (qName.equals("TitleElement")) {
            TableColumn[] columns = currentTable.getColumns();
            if (columns.length <= currentTableHeadColumn) {
                columns = Arrays.copyOf(columns, columns.length + 1);
                currentTable.setColumns(columns);
                TableColumn tableColumn = new TableColumn();
                tableColumn.setTable(currentTable);
                columns[columns.length - 1] = tableColumn;
            }
            currentTable.setColumns(columns);
            current = columns[currentTableHeadColumn].getColumnHeader();
            currentTableHeadColumn++;
        } else if (qName.equals("BodyElement")) {
            TableColumn[] columns = currentTable.getColumns();
            if (columns.length <= currentTableBodyColumn) {
                columns = Arrays.copyOf(columns, columns.length + 1);
                currentTable.setColumns(columns);
                TableColumn tableColumn = new TableColumn();
                tableColumn.setTable(currentTable);
                columns[columns.length - 1] = tableColumn;
            }
            currentTable.setColumns(columns);

            current = columns[currentTableBodyColumn].getColumnBody();
            currentTableBodyColumn++;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("attribute")) {
            String name = attributes.getValue("name");
            if ((name.equals("BaseRow")) && (ignoreBaseQualifier))
                return;
            for (Attribute attribute : current.getXMLAttributes()) {
                if (attribute.getName().equals(name)) {
                    try {
                        current.setXMLAttribute(attribute, attribute
                                .convertXmlAttributeToObject(text, engine));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        reportEditor.setReport(report);
        reportEditor.loadFromYComponents(yComponents);
        this.yComponents = null;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        text += new String(ch, start, length);
    }

}
