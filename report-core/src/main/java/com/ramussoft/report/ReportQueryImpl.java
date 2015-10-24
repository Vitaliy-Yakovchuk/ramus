package com.ramussoft.report;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.Out;

import static com.ramussoft.report.ReportPlugin.TYPE_XML;
import static com.ramussoft.report.ReportPlugin.TYPE_JSSP;
import static com.ramussoft.report.ReportPlugin.TYPE_JSSP_DOC_BOOK;

public class ReportQueryImpl implements ReportQuery {

    private final Engine engine;

    private Attribute reportType;

    private Attribute reportName;

    public ReportQueryImpl(Engine engine) {
        this.engine = engine;
    }

    @Override
    public String getHTMLReport(Element element, HashMap<String, Object> map) {
        String type = getReportType(element);

        if (type.equals(TYPE_XML)) {
            return getHTMLXMLReport(element, map);
        } else if (type.equals(TYPE_JSSP))
            return getHTMLJSSPReport(element, map);
        else if (type.equals(TYPE_JSSP_DOC_BOOK))
            return getHTMLJSSPDocBookReport(element, map);
        throw new RuntimeException("Unknown report type \"" + type + "\"");
    }

    private String getReportType(Element element) {
        String type = (String) engine.getAttribute(element, getReportType());
        if (type == null)
            type = TYPE_XML;
        return type;
    }

    private String getHTMLXMLReport(Element element, HashMap<String, Object> map) {
        XMLReportEngine engine = new XMLReportEngine(this.engine) {
            @Override
            protected void createOut(OutputStream stream)
                    throws UnsupportedEncodingException {
                this.out = ReportQueryImpl.this.createOut(stream);
            }
        };
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            engine.execute(getXMLReportPath(element), stream, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(stream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("UTF-8 encoding not found :)");
        }
    }

    protected Out createOut(OutputStream stream) {
        try {
            return new Out(stream);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String getHTMLJSSPReport(Element element,
                                     HashMap<String, Object> map) {
        JSSPReportEngine engine = new JSSPReportEngine(this.engine, this) {

            @Override
            protected Out createOut(ByteArrayOutputStream outputStream)
                    throws UnsupportedEncodingException {
                return ReportQueryImpl.this.createOut(outputStream);
            }
        };
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            engine.execute(getJSSPReportPath(element), stream, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(stream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private String getHTMLJSSPDocBookReport(Element element,
                                            HashMap<String, Object> map) {
        JSSPReportEngine engine = new JSSPDocBookReportEngine(this.engine, this) {

            @Override
            protected Out createOut(ByteArrayOutputStream outputStream)
                    throws UnsupportedEncodingException {
                return ReportQueryImpl.this.createOut(outputStream);
            }
        };
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            engine.execute(getJSSPReportPath(element), stream, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(stream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private String getJSSPReportPath(Element element) {
        return "/elements/" + element.getId() + "/" + reportName.getId()
                + "/report.jssp";
    }

    public Attribute getReportType() {
        if (reportType == null) {
            reportType = ReportPlugin.getReportTypeAttribute(engine);
            reportName = ReportPlugin.getReportNameAttribute(engine);
        }
        return reportType;
    }

    private String getXMLReportPath(Element element) {
        return "/elements/" + element.getId() + "/" + reportName.getId()
                + "/report.1.xml";
    }

    @Override
    public List<Element> getHTMLReports() {
        RowSet rowSet = new RowSet(engine,
                ReportPlugin.getReportsQualifier(engine), new Attribute[]{},
                null, true);
        List<Row> rows = rowSet.getAllRows();
        List<Element> elements = new ArrayList<Element>(rows.size());
        for (Row row : rows)
            elements.add(row.getElement());
        return elements;
    }

    @Override
    public Qualifier getHTMLReportQuery(Element element) {
        String type = getReportType(element);
        if (type.equals(TYPE_XML)) {
            String value = getXMLReportAttribute("BaseRow", element);
            if (value != null) {
                return engine.getQualifier(Long.parseLong(value));
            }
        }
        return null;
    }

    private String getXMLReportAttribute(String attributeName, Element element) {
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser;

            final Hashtable<String, String> attrs = new Hashtable<String, String>();

            parser = factory.newSAXParser();
            byte[] bs = engine.getStream(getXMLReportPath(element));
            if ((bs == null) || (bs.length == 0))
                return null;
            parser.parse(new ByteArrayInputStream(bs), new DefaultHandler() {

                private StringBuffer sb = new StringBuffer();

                private String attribute;

                @Override
                public void startElement(String uri, String localName,
                                         String qName, Attributes attributes)
                        throws SAXException {
                    if (qName.equals("attribute")) {
                        attribute = attributes.getValue("name");
                    }
                    sb = new StringBuffer();
                }

                @Override
                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {
                    if (qName.equals("attribute")) {
                        attrs.put(attribute, sb.toString());
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length)
                        throws SAXException {
                    sb.append(ch, start, length);
                }
            });
            return attrs.get(attributeName);
        } catch (ParserConfigurationException e) {
            throw new DataException(e);
        } catch (SAXException e) {
            throw new DataException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
