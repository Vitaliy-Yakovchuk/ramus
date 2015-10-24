package com.ramussoft.core.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;

public class TableToXML {

    private JDBCTemplate template;

    private OutputStream stream;

    private String tableName;

    private AttributesImpl attrs = new AttributesImpl();

    private TransformerHandler th;

    private String prefix;

    private final static String[] digits = {"00", "01", "02", "03", "04", "05",
            "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B",
            "1C", "1D", "1E", "1F", "20", "21", "22", "23", "24", "25", "26",
            "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F", "30", "31",
            "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C",
            "3D", "3E", "3F", "40", "41", "42", "43", "44", "45", "46", "47",
            "48", "49", "4A", "4B", "4C", "4D", "4E", "4F", "50", "51", "52",
            "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D",
            "5E", "5F", "60", "61", "62", "63", "64", "65", "66", "67", "68",
            "69", "6A", "6B", "6C", "6D", "6E", "6F", "70", "71", "72", "73",
            "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E",
            "7F", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89",
            "8A", "8B", "8C", "8D", "8E", "8F", "90", "91", "92", "93", "94",
            "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
            "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA",
            "AB", "AC", "AD", "AE", "AF", "B0", "B1", "B2", "B3", "B4", "B5",
            "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF", "C0",
            "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB",
            "CC", "CD", "CE", "CF", "D0", "D1", "D2", "D3", "D4", "D5", "D6",
            "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF", "E0", "E1",
            "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC",
            "ED", "EE", "EF", "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7",
            "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"};

    private interface Converter {
        String toString(Object object) throws SQLException;
    }

    ;

    private class ToStringConverter implements Converter {

        @Override
        public String toString(Object object) throws SQLException {
            return object.toString();
        }

    }

    private class BoolConverter implements Converter {

        @Override
        public String toString(Object object) throws SQLException {
            return object.toString();
        }

    }

    private class DateConverter implements Converter {

        @Override
        public String toString(Object object) throws SQLException {
            return XMLToTable.DATE_FORMAT.format(object);
        }

    }

    public static class ByteAConverter implements Converter {

        public String toHexString(byte bytes[]) {
            StringBuffer retString = new StringBuffer();
            for (int i = 0; i < bytes.length; ++i) {
                int j = bytes[i];
                j += 128;
                retString.append(digits[j]);
            }
            return retString.toString();
        }

        @Override
        public String toString(Object object) throws SQLException {
            byte[] bs = (byte[]) object;
            return toHexString(bs);
        }
    }

    public TableToXML(JDBCTemplate template, OutputStream stream,
                      String tableName, String prefix) {
        this.template = template;
        this.stream = stream;
        this.tableName = tableName;
        this.prefix = prefix;
    }

    public void store() throws IOException, TransformerConfigurationException,
            SAXException {
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
                .newInstance();
        th = tf.newTransformerHandler();
        StreamResult result = new StreamResult(stream);
        th.setResult(result);
        th.startDocument();
        attrs.addAttribute("", "", "generate-from-table", "CDATA", tableName);
        attrs.addAttribute("", "", "generate-time", "CDATA", new Date()
                .toString());
        attrs.addAttribute("", "", "prefix", "CDATA", prefix);
        startElement("table");
        attrs.clear();

        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM " + prefix
                        + tableName);
                ResultSetMetaData meta = rs.getMetaData();
                startElement("fields");
                int cc = meta.getColumnCount();
                Converter[] converters = new Converter[cc];
                for (int i = 0; i < cc; i++) {
                    attrs.addAttribute("", "", "id", "CDATA", Integer
                            .toString(i));
                    attrs.addAttribute("", "", "name", "CDATA", meta
                            .getColumnName(i + 1));
                    attrs.addAttribute("", "", "type", "CDATA", meta
                            .getColumnTypeName(i + 1));
                    startElement("field");
                    attrs.clear();
                    endElement("field");
                    if ((meta.getColumnType(i + 1) == Types.BLOB)
                            || (meta.getColumnType(i + 1) == Types.VARBINARY)
                            || (meta.getColumnTypeName(i + 1)
                            .equalsIgnoreCase("bytea"))) {
                        converters[i] = new ByteAConverter();
                    } else if (meta.getColumnType(i + 1) == Types.CLOB) {
                        converters[i] = new ToStringConverter();
                    } else if (meta.getColumnTypeName(i + 1).equalsIgnoreCase(
                            "bool")) {
                        converters[i] = new BoolConverter();
                    } else if (meta.getColumnTypeName(i + 1).equalsIgnoreCase(
                            "timestamp")) {
                        converters[i] = new DateConverter();
                    } else {
                        converters[i] = new ToStringConverter();
                    }
                }
                endElement("fields");
                startElement("data");
                while (resultSetNext(rs)) {
                    startElement("row");
                    for (int i = 0; i < cc; i++) {
                        Object object;
                        if (converters[i] instanceof BoolConverter)
                            object = rs.getBoolean(i + 1);
                        else if (converters[i] instanceof ToStringConverter)
                            object = rs.getString(i + 1);
                        else if (converters[i] instanceof ByteAConverter)
                            object = rs.getBytes(i + 1);
                        else
                            object = rs.getObject(i + 1);
                        if (object != null) {
                            attrs.addAttribute("", "", "id", "CDATA", Integer
                                    .toString(i));
                            startElement("f");
                            attrs.clear();
                            characters(converters[i].toString(object));
                            endElement("f");
                        }
                    }
                    endElement("row");
                }
                endElement("data");
                rs.close();
                st.close();
                return null;
            }

        });

        endElement("table");
        th.endDocument();
    }

    protected boolean resultSetNext(ResultSet rs) throws SQLException {
        return rs.next();
    }

    protected void characters(String string) {
        char[] chars = string.toCharArray();
        try {
            th.characters(chars, 0, chars.length);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private void startElement(String qName) {
        try {
            th.startElement("", "", qName, attrs);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private void endElement(String qName) {
        try {
            th.endElement("", "", qName);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

}
