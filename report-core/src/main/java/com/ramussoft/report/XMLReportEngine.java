package com.ramussoft.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.Out;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;
import com.ramussoft.report.xml.ColumnBody;
import com.ramussoft.report.xml.ColumnHeader;
import com.ramussoft.report.xml.Keywords;
import com.ramussoft.report.xml.LabelPrint;
import com.ramussoft.report.xml.NoRowsException;
import com.ramussoft.report.xml.ParagraphPrint;
import com.ramussoft.report.xml.TablePrint;

public class XMLReportEngine extends ReportEngine {

    public static final Keywords KEYWORDS = new Keywords();

    protected Out out;

    private Hashtable<String, String> attributes = new Hashtable<String, String>();

    private Hashtable<String, String> tableAttributes;

    private Hashtable<String, String> tableHeaderAttributes;

    private Hashtable<String, String> tableBodyAttributes;

    private Attributes currentAttributes;

    private List<ColumnHeader> columnHeaders;

    private List<ColumnBody> columnBodies;

    private StringBuffer text = new StringBuffer();

    private Data data;

    private List<LabelPrint> connectedLabels = new ArrayList<LabelPrint>();

    private boolean reportElement = true;

    private boolean processReportAttributes = true;

    public XMLReportEngine(Engine engine) {
        super(engine);
    }

    @Override
    public void execute(String path, OutputStream stream,
                        Map<String, Object> parameters) throws IOException {
        createOut(stream);
        Query query = (Query) parameters.get("query");

        this.data = new Data(engine, query);

        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser;
            parser = factory.newSAXParser();
            byte[] bs = engine.getStream(path);
            if (bs == null)
                throw new DataException("Error.reportEmpty",
                        "Report's form is empty");
            do {
                try {
                    parser.parse(new ByteArrayInputStream(bs),
                            new ReportHandler());
                } catch (NoRowsException e) {
                    break;
                }
                if (null == data.getBaseRows())
                    break;
                connectedLabels.clear();
                if (data.isSameBaseQualifier())
                    break;
            } while (data.getBaseRows().next() != null);
        } catch (ParserConfigurationException e) {
            throw new DataException(e);
        } catch (SAXException e) {
            throw new DataException(e);
        }

        if (out.checkError())
            throw new IOException();
        out.flush();
        out.realWrite();
    }

    protected void createOut(OutputStream stream)
            throws UnsupportedEncodingException {
        out = new Out(stream);
    }

    private class ReportHandler extends DefaultHandler {
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            currentAttributes = attributes;
            if ((reportElement) && (!qName.equals("Report"))) {
                reportElement = false;
                Query query = data.getQuery();
                if (query == null) {
                    query = new Query(XMLReportEngine.this.attributes);
                    data.setQuery(query);
                } else
                    query.setAttributes(XMLReportEngine.this.attributes);
            }
            if (qName.equals("Table")) {
                checkReportAttributes();
                tableAttributes = XMLReportEngine.this.attributes;
                columnHeaders = new ArrayList<ColumnHeader>();
                columnBodies = new ArrayList<ColumnBody>();
            } else if (qName.equals("TitleElement")) {
                XMLReportEngine.this.attributes = new Hashtable<String, String>();

                tableHeaderAttributes = XMLReportEngine.this.attributes;
            } else if (qName.equals("BodyElement")) {
                XMLReportEngine.this.attributes = new Hashtable<String, String>();
                tableBodyAttributes = XMLReportEngine.this.attributes;
            } else if ("Label".equals(qName)) {
                checkReportAttributes();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals("attribute")) {
                attributes.put(currentAttributes.getValue("name"), text
                        .toString());
            } else if (qName.equals("Label")) {
                ParagraphPrint label = new ParagraphPrint(attributes, data);
                if ("true".equals(attributes.get("ConnectWithNextTable")))
                    connectedLabels.add(label);
                else
                    label.print(out, data);
                attributes = new Hashtable<String, String>();
            } else if (qName.equals("Table")) {
                TablePrint table = new TablePrint(tableAttributes,
                        columnHeaders, columnBodies, data) {
                    @Override
                    public void onNext() {
                        for (LabelPrint label : connectedLabels) {
                            label.print(out, data);
                        }
                        connectedLabels.clear();
                    }
                };
                table.print(out, data);
                attributes = new Hashtable<String, String>();
            } else if (qName.equals("TitleElement")) {
                columnHeaders
                        .add(new ColumnHeader(tableHeaderAttributes, data));
                attributes = new Hashtable<String, String>();
            } else if (qName.equals("BodyElement")) {
                columnBodies.add(new ColumnBody(tableBodyAttributes));
                attributes = new Hashtable<String, String>();
            }
            text = new StringBuffer();
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            text.append(ch, start, length);
        }
    }

    ;

    public static Object getStaticAttribute(Row row, Rows rows,
                                            String connectionName) {
        if (KEYWORDS.hasName("Name", connectionName))
            return row;
        if (KEYWORDS.hasName("Code", connectionName))
            return new Code(row.getElement(), row.getCode());
        if (KEYWORDS.hasName("SerialNumber", connectionName)) {
            if (rows == null) {
                throw new DataException("Error.noRowsFoundForRows",
                        "Cannot get serial number for row" + row
                                + ", please use rows.getAttribute method!!!");
            }
            return new SerialNumber(rows.getNumber());
        }
        if (KEYWORDS.hasName("CodeIDEF0	", connectionName))
            return new Code(row.getElement(), getIDEF0Kod(row));
        if (KEYWORDS.hasName("QualifierName", connectionName))
            return row.getQualifier();
        return null;
    }

    public void checkReportAttributes() {
        if (processReportAttributes) {
            processReportAttributes = false;
            String br = attributes.get("BaseRow");
            if (br != null) {
                long id = Long.parseLong(br);
                Qualifier qualifier = data.getQualifier(id);
                if (qualifier == null)
                    attributes.remove("BaseRow");
            }

			/*String model = attributes.get("ReportFunction");
            if (model != null) {
				Qualifier qualifier = data.getEngine()
						.getQualifierByName(model);
				if (qualifier == null)
					attributes.remove("ReportFunction");
			}*/
        }
    }

    private static String getRecIDEF0Kod(final Row function) {
        final Row f = (Row) function.getParent();
        if (f == null || f.getParent() == null)
            return "";
        String id = Integer.toString(function.getId());
        if (id.length() > 1)
            id = "." + id + ".";
        return getRecIDEF0Kod(f) + id;
    }

    /**
     * Метод визначає код функціонального блоку у відповідності до стандарту
     * IDEF0
     *
     * @param function Функціональний блок, для якого буде визначений його код.
     * @return Код функціонального блока у відповідності до стандарту IDEF0.
     */

    private static String getIDEF0Kod(final Row function) {
        final Row f = (Row) function.getParent();
        if (f == null)
            return "A-0";
        if (f.getParent() == null)
            return "A0";
        return "A" + getRecIDEF0Kod(function);
    }
}
