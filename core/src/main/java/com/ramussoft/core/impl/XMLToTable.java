package com.ramussoft.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;

public class XMLToTable {

    private JDBCTemplate template;

    private String tableName;

    private String prefix;

    private InputStream stream;

    public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH);

    private interface Converter {
        void fill(PreparedStatement ps, int column, String value)
                throws SQLException;
    }

    private class StringConverter implements Converter {

        @Override
        public void fill(PreparedStatement ps, int column, String value)
                throws SQLException {
            if (value == null)
                ps.setObject(column, null);
            else
                ps.setString(column, value);
        }

    }

    private class IntegerConverter implements Converter {

        @Override
        public void fill(PreparedStatement ps, int column, String value)
                throws SQLException {
            if (value == null)
                ps.setObject(column, null);
            else
                ps.setInt(column, Integer.parseInt(value));
        }

    }

    private class BooleanConverter implements Converter {

        @Override
        public void fill(PreparedStatement ps, int column, String value)
                throws SQLException {
            if (value == null)
                ps.setObject(column, null);
            else
                ps.setBoolean(column, Boolean.parseBoolean(value));
        }

    }

    private class DoubleConverter implements Converter {

        @Override
        public void fill(PreparedStatement ps, int column, String value)
                throws SQLException {
            if (value == null)
                ps.setObject(column, null);
            else
                ps.setDouble(column, Double.parseDouble(value));
        }

    }

    private class LongConverter implements Converter {

        @Override
        public void fill(PreparedStatement ps, int column, String value)
                throws SQLException {
            if (value == null)
                ps.setObject(column, null);
            else
                ps.setLong(column, Long.parseLong(value));
        }

    }

    private class ByteAConverter implements Converter {

        @Override
        public void fill(PreparedStatement ps, int column, String value)
                throws SQLException {
            if (value == null)
                ps.setObject(column, null);
            else {
                try {
                    if (value.length() == 0)
                        ps.setBytes(column, new byte[]{});
                    else {
                        byte[] bs = new byte[value.length() / 2];
                        int len = value.length();
                        for (int i = 0; i < len; i += 2) {
                            int val;
                            char c = value.charAt(i);
                            if (c >= 'A')
                                val = 16 * (c - 'A' + 10);
                            else
                                val = 16 * (c - '0');
                            c = value.charAt(i + 1);
                            if (c >= 'A')
                                val += (c - 'A' + 10);
                            else
                                val += (c - '0');
                            bs[i / 2] = (byte) (val - 128);
                        }
                        ps.setBytes(column, bs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class DateConverter implements Converter {

        @Override
        public void fill(PreparedStatement ps, int column, String value)
                throws SQLException {
            if (value == null)
                ps.setObject(column, null);
            else
                try {
                    ps.setTimestamp(column,
                            new Timestamp(DATE_FORMAT.parse(value).getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
        }

    }

    public XMLToTable(JDBCTemplate template, InputStream stream,
                      String tableName, String prefix) {
        this.template = template;
        this.tableName = tableName;
        this.prefix = prefix;
        this.stream = stream;
    }

    public void load() throws IOException, SQLException {
        final Hashtable<String, Integer> positions = new Hashtable<String, Integer>();
        final List<String> columns = new ArrayList<String>();
        final Hashtable<String, String> ids = new Hashtable<String, String>();

        final PreparedStatement ps = (PreparedStatement) template
                .execute(new JDBCCallback() {

                    @Override
                    public Object execute(Connection connection)
                            throws SQLException {
                        Statement st = connection.createStatement();
                        ResultSet rs = st.executeQuery("SELECT * FROM "
                                + prefix + tableName);
                        ResultSetMetaData meta = rs.getMetaData();

                        String colums = "";
                        String values = "";
                        int columnCount = meta.getColumnCount();
                        for (int i = 0; i < columnCount; i++) {
                            if (values.equals(""))
                                values += "?";
                            else
                                values += ", ?";

                            String cn = meta.getColumnName(i + 1);
                            if (colums.equals(""))
                                colums += cn;
                            else
                                colums += ", " + cn;
                            String cnLowerCase = cn.toLowerCase();
                            columns.add(cnLowerCase);
                            positions.put(cn.toLowerCase(), i + 1);
                        }
                        rs.close();
                        st.close();
                        PreparedStatement ps = connection
                                .prepareStatement("INSERT INTO " + prefix
                                        + tableName + "(" + colums
                                        + ") VALUES(" + values + ");");
                        for (int i = 1; i <= columnCount; i++)
                            if (columns.get(i - 1).equals("removed_branch_id"))
                                ps.setLong(i, Integer.MAX_VALUE);
                            else
                                ps.setObject(i, null);
                        return ps;
                    }
                });
        template.execute(new JDBCCallback() {
            @Override
            public Object execute(final Connection connection)
                    throws SQLException {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser;

                final Hashtable<String, Converter> converters = new Hashtable<String, Converter>();

                try {
                    parser = factory.newSAXParser();
                    parser.parse(stream, new DefaultHandler() {
                        StringBuilder sb = new StringBuilder();

                        private boolean initFields = true;

                        private boolean inRow = false;

                        private String id;

                        @Override
                        public void startElement(String uri, String localName,
                                                 String name, Attributes attributes)
                                throws SAXException {
                            if ("data".equals(name))
                                initFields = false;
                            if ("row".equals(name)) {
                                inRow = true;
                                for (int i = 1; i <= positions.size(); i++)
                                    try {
                                        String column = columns.get(i - 1);
                                        if (column.endsWith("_branch_id")
                                                && !column
                                                .equals("removed_branch_id"))
                                            ps.setLong(i, 0l);
                                        else if (column
                                                .equals("removed_branch_id"))
                                            ps.setObject(i, Integer.MAX_VALUE);
                                        else
                                            ps.setObject(i, null);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                            }
                            if ((inRow) && ("f".equals(name))) {
                                id = attributes.getValue("id");
                            }
                            if ((initFields) && ("field".equals(name))) {
                                Converter converter = null;
                                String type = attributes.getValue("type");

                                ids.put(attributes.getValue("id"),
                                        attributes.getValue("name"));

                                if ((type.equalsIgnoreCase("CLOB"))
                                        || (type.equalsIgnoreCase("CHAR"))
                                        || (type.equalsIgnoreCase("TEXT"))
                                        || (type.equalsIgnoreCase("bpchar"))) {
                                    converter = new StringConverter();
                                } else if ((type.equalsIgnoreCase("INTEGER"))
                                        || (type.equalsIgnoreCase("int4")))
                                    converter = new IntegerConverter();
                                else if ((type.equalsIgnoreCase("BLOB"))
                                        || (type.equalsIgnoreCase("VARBINARY"))
                                        || (type.equalsIgnoreCase("bytea"))) {
                                    converter = new ByteAConverter();
                                } else if (type.equalsIgnoreCase("TIMESTAMP")) {
                                    converter = new DateConverter();
                                } else if ((type.equalsIgnoreCase("LONG"))
                                        || (type.equalsIgnoreCase("BIGINT"))
                                        || (type.equalsIgnoreCase("int8"))) {
                                    converter = new LongConverter();
                                } else if ((type.equalsIgnoreCase("BOOL"))
                                        || (type.equalsIgnoreCase("BOOLEAN"))) {
                                    converter = new BooleanConverter();
                                } else if ((type.equalsIgnoreCase("DOUBLE"))
                                        || (type.equalsIgnoreCase("float8")))
                                    converter = new DoubleConverter();
                                else {
                                    System.err
                                            .println("ERROR: Converter not found for type "
                                                    + type);
                                }
                                converters.put(attributes.getValue("id"),
                                        converter);
                            }
                        }

                        @Override
                        public void endElement(String uri, String localName,
                                               String name) throws SAXException {
                            if ("row".equals(name)) {
                                try {
                                    inRow = false;
                                    ps.execute();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            } else if ((inRow) && ("f".equals(name))) {
                                String key = ids.get(id);
                                int i = -1;
                                try {
                                    i = positions.get(key.toLowerCase());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (i > 0) {
                                    Converter c = converters.get(id);
                                    try {
                                        c.fill(ps, i, sb.toString());
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            sb = new StringBuilder();
                        }

                        @Override
                        public void characters(char[] ch, int start, int length)
                                throws SAXException {
                            sb.append(new String(ch, start, length));
                        }

                        public void endDocument() throws SAXException {
                            try {
                                ps.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                        ;
                    });
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        stream.close();
    }
}
