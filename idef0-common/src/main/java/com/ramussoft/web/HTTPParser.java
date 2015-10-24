package com.ramussoft.web;

import java.awt.Dimension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JFrame;

import net.htmlparser.jericho.Source;

import com.dsoft.html.Base64;
import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.types.FRectangle;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.core.attribute.simple.FilePersistent;
import com.ramussoft.core.attribute.simple.FilePlugin;
import com.ramussoft.core.attribute.simple.VariantPropertyPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.core.impl.TableToXML;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.MatrixProjection;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.AbstractDataPlugin;
import com.ramussoft.pb.data.RowFactory;
import com.ramussoft.pb.data.negine.NRow;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.elements.PaintSector.Pin;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingLabel;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.pb.print.PIDEF0painter;
import com.ramussoft.pb.print.web.AttributeViewerFactory;
import com.ramussoft.pb.types.GlobalId;
import com.ramussoft.report.Code;
import com.ramussoft.report.Query;
import com.ramussoft.report.ReportPlugin;
import com.ramussoft.report.ReportQuery;
import com.ramussoft.report.ReportQueryImpl;
import com.ramussoft.report.ReportResourceManager;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.MessageFormatter;
import com.ramussoft.report.data.Out;
import com.ramussoft.report.html.Style;

/**
 * Клас призначений для обробки запитів браузера.
 *
 * @author ZDD
 */

public class HTTPParser extends Servlet {

    public static final String ENCODING = "UTF-8";

    private static final int IMAGE_WIDTH = 1024;

    private static final int IMAGE_HEIGHT = 768;

    private static final Collator collator = Collator.getInstance();

    public static class PrintStream extends Out {

        public PrintStream(OutputStream out)
                throws UnsupportedEncodingException {
            super(out);
        }

    }

    ;

    public static final ResourceBundle RES = ResourceBundle.getBundle(
            "com.dsoft.pb.resources.htmls", ResourceLoader.getLocale());

    private static ByteArrayOutputStream favicon;
    ;

    private static int imageFormat = PIDEF0painter.PNG_FORMAT;

    private static String GRAY = "#DDDDDD";

    static {
        favicon = new ByteArrayOutputStream();
        try {
            copyFromResource("/resources/icon.ico", favicon);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private OutputStream stream;

    protected PrintStream htmlStream;

    private String htmlTitle = null;

    private Hashtable<String, String> params;

    private String fromLink;

    private boolean startPage;

    private String location;

    protected boolean printVersion;

    private DataPlugin dataPlugin;

    private GUIFramework framework;

    private AttributeViewerFactory factory = AttributeViewerFactory
            .getAttributeViewverFactory();

    private Request request;

    private Response response;

    public HTTPParser(DataPlugin dataPlugin, GUIFramework framework) {
        this.dataPlugin = dataPlugin;
        this.framework = framework;
    }

    public void printStartATeg(final String href) throws IOException {
        printStartATeg(href, false);
    }

    protected void printStartATeg(final String href, final boolean newWindow)
            throws IOException {
        printStartATeg(href, newWindow, true);
    }

    private void printStartATeg(final String href, final boolean newWindow,
                                final boolean printFrom) throws IOException {
        htmlStream.print(getStartATeg(href, newWindow, printFrom));
    }

    private String getStartATeg(final String href, final boolean newWindow,
                                final boolean printFrom) {
        if (printVersion)
            return "";
        if (href == null)
            return "";
        String fromLink;
        if (printFrom)
            fromLink = this.fromLink;
        else
            fromLink = "";
        final String h = params.get("h");
        final String w = params.get("w");
        String r = "";
        if (h != null && w != null) {
            boolean b = false;
            for (int i = 0; i < href.length(); i++) {
                if (href.charAt(i) == '?') {
                    b = true;
                    break;
                }
            }
            if (b)
                r = "&h=" + h + "&w=" + w;
            else
                r = "?h=" + h + "&w=" + w;
        }
        if (newWindow)
            return "<a target=\"_blank\" href=\"" + fromLink + href + r + "\">";
        else
            return "<a href=\"" + fromLink + href + r + "\">";
    }

    public void printEndATeg() throws IOException {
        htmlStream.println(getEndATeg());
    }

    private String getEndATeg() {
        if (printVersion)
            return "";
        return "</a>";
    }

    private static void copyFromResource(final String resource,
                                         final OutputStream stream) throws IOException {
        final URL url = resource.getClass().getResource(resource);
        InputStream is;
        if (url != null) {
            is = url.openStream();
            int i;
            while ((i = is.read()) >= 0)
                stream.write(i);
            is.close();
        }
    }

    private void printStart() throws IOException {
        response.writeHead();
        java.io.PrintStream htmlStream = new java.io.PrintStream(stream, false,
                ENCODING);
        htmlStream.println("<html>");
        htmlStream.println("<head>");
        htmlStream.print("<title>");
        if (htmlTitle != null)
            htmlStream.print(htmlTitle + " ");
        if (printVersion)
            htmlStream.print("(" + RES.getString("printVersion") + ")");
        else {
        }

        if (framework != null) {
            JFrame mainFrame = framework.getMainFrame();
            htmlStream.print((mainFrame == null) ? Main.getProgramName()
                    : mainFrame.getTitle() + " ");
        } else {
            htmlStream.print(Metadata.getApplicationName() + " ");
        }

        htmlStream.println("</title>");
        htmlStream
                .println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset="
                        + ENCODING + "\">");
        if (!printVersion)
            htmlStream
                    .println("<link rel=\"stylesheet\" type=\"text/css\" href=\"default.css\">");
        htmlStream.println("<style>");
        for (Style style : this.htmlStream.getStyles())
            htmlStream.println(style);
        htmlStream.println("</style>");
        htmlStream.println("</head>");
        htmlStream.println("<body>");
        htmlStream.flush();
    }

    private void printEnd() throws IOException {
        java.io.PrintStream htmlStream = new java.io.PrintStream(stream);
        htmlStream.println("</body>");
        htmlStream.println("</html>");
        htmlStream.flush();
    }

    /**
     * Друкує список звітів з посиланнями на них.
     *
     * @throws IOException
     */

    protected void printReportsList() throws IOException {
        Engine engine = dataPlugin.getEngine();
        Qualifier qualifier = ReportPlugin.getReportsQualifier(engine);

        Attribute name = ReportPlugin.getReportNameAttribute(engine);

        RowSet rowSet = new RowSet(engine, qualifier, new Attribute[]{name},
                null, true);

        List<Element> reports = ((ReportQuery) engine).getHTMLReports();
        if (reports.size() == 0)
            return;
        printMainTableTitle(RES.getString("reportsTitle"));
        for (com.ramussoft.database.common.Row element : rowSet.getAllRows()) {
            htmlStream.println("<tr>");
            htmlStream.println("<td colspan=2>");
            printStartATeg("reportsq/index.html?num=" + element.getElementId());
            htmlStream.println(element.getCode());
            printEndATeg();
            printStartATeg("reportsq/index.html?num=" + element.getElementId());
            htmlStream.print(element.getName());
            printEndATeg();
            htmlStream.println("</td>");
            htmlStream.println("</tr>");
        }
    }

    private void printRowKod(final Row row, final boolean printLeft)
            throws IOException {
        printRowKod(row, printLeft, false);
    }

    private void printRowKod(final Row row, final boolean printLeft,
                             boolean function) throws IOException {
        String sb = "";
        String eb = "";
        String left = "";
        final int l = dataPlugin.getElementLevel(row);
        if (printLeft)
            for (int i = 0; i < l; i++)
                left += "&nbsp;&nbsp;&nbsp;";
        if (row.getChildCount() > 0) {
            sb = "<b>";
            eb = "</b>";
        }
        htmlStream.print(sb + left
                + ((function) ? getIDEF0Kod(row) : row.getKod()) + eb);
    }

    private void printRowName(final Row row, final boolean printLeft)
            throws IOException {
        String sb = "";
        String eb = "";
        String left = "";
        final int l = dataPlugin.getElementLevel(row);
        if (printLeft)
            for (int i = 0; i < l; i++)
                left += "&nbsp;&nbsp;&nbsp;";
        if (row.getChildCount() > 0) {
            sb = "<b>";
            eb = "</b>";
        }
        htmlStream.print(sb + left + row.getName() + eb);
    }

    /**
     * Друкує назву елемента класифікатора.
     *
     * @throws IOException
     *
     */

	/*
     * private void printRow(Row row, boolean gray) throws IOException { String
	 * g = ""; if (gray) g = " bgcolor=" + GRAY; htmlStream.print("<td" + g +
	 * ">"); printRowKod(row, true); htmlStream.print("</td>");
	 * htmlStream.print("<td" + g + ">"); printRowName(row, true);
	 * htmlStream.print("</td>"); }
	 */

    /**
     * Друкує сторінку запиту для генерування звіту.
     *
     * @throws IOException
     */

    private void printReportQuary(final Qualifier clasificator, final String num)
            throws IOException {
        final String selectAll = params.get("all");
        String value = "";
        if (selectAll != null && "true".equals(selectAll))
            value = " checked=\"true\"";

        Row sel = null;
        final String c = params.get("check");
        if (c != null) {
            try {
                sel = dataPlugin.findRowByGlobalId(GlobalId.convert(c));
            } catch (final Exception e) {
            }
        }

        // htmlStream.println("<caption>");
        printSmallTitle(htmlTitle + ". " + RES.getString("baseClasificator")
                + ": " + clasificator.getName());
        htmlStream.print("<br><div align=right>");
        printStartATeg("reportsq/index.html?num=" + num + "&all=true");
        htmlStream.print(RES.getString("selectAll"));
        printEndATeg();
        printStartATeg("reportsq/index.html?num=" + num);
        htmlStream.print(RES.getString("deselectAll"));
        printEndATeg();
        htmlStream.print("</div>");

        htmlStream.println("<FORM ACTION=\"" + fromLink + "report_" + num
                + "/index.html\" METHOD=GET ID=\"ReportForm\">");
        htmlStream.println("<table border=1 width=100%>");

        // htmlStream.println("</caption>");
        htmlStream.println("<tr>");

        htmlStream.print("<td><b>");
        htmlStream.print(RES.getString("kod"));
        htmlStream.println("</b></td>");
        htmlStream.print("<td><b>");
        htmlStream.print(RES.getString("clasificatorElementName"));
        htmlStream.println("</b></td>");
        htmlStream.print("<td><b>");
        htmlStream.print(RES.getString("isPrinting") + "<br>");

        htmlStream.println("</b></td>");
        htmlStream.println("</tr>");
        final Vector v = dataPlugin.getRecChildren(clasificator);
        for (int i = 0; i < v.size(); i++) {
            final Row row = (Row) v.get(i);
            htmlStream.println("<tr>");
            final boolean gray = i % 2 == 0;

            String g = "";
            if (gray)
                g = " bgcolor=" + GRAY;
            htmlStream.print("<td" + g + ">");
            printRowKod(row, true, IDEF0Plugin.isFunction(clasificator));
            htmlStream.print("</td>");
            htmlStream.print("<td" + g + ">");
            final int l = dataPlugin.getElementLevel(row);
            String left = "";
            for (int j = 0; j < l; j++)
                left += "&nbsp;&nbsp;&nbsp;";
            htmlStream.print(left);
            printStartATeg("report_" + num + "/index.html?base="
                    + row.getElement().getId());
            printRowName(row, false);
            printEndATeg();
            htmlStream.print(" ");
            printStartATeg("reportsq/index.html?num=" + num + "&check="
                    + row.getElement().getId());
            htmlStream.print(" <img border=0 src=" + fromLink
                    + "check_all.gif>");
            printEndATeg();

            htmlStream.print("</td>");
            if (gray)
                htmlStream.print("<td bgcolor=" + GRAY + "><center>");
            else
                htmlStream.print("<td><center>");
            final String t = value;

            if (sel != null
                    && (dataPlugin.isParent(row, sel) || sel.equals(row)))
                value = " checked=\"true\"";

            htmlStream.print("<input type=checkbox name="
                    + row.getGlobalId().toString() + value + ">");

            value = t;

            htmlStream.println("</center></td>");
            htmlStream.println("</tr>");
        }
        htmlStream.println("<tr><td colspan=3></td></tr>");
        htmlStream.println("</table>");
        htmlStream.print("<p align=right><INPUT TYPE=SUBMIT value=\""
                + RES.getString("report") + "\"></p>");
        htmlStream.println("</FORM>");
    }

    private void printReportsQ() throws IOException {
        final String sNum = params.get("num");
        if (sNum == null)
            printError(RES.getString("reportEror"));
        final long num = Long.parseLong(sNum);
        Element report = null;
        if ((report = dataPlugin.getEngine().getElement(num)) == null) {
            printError(RES.getString("reportEror"));
            return;
        }
        htmlTitle = RES.getString("reportQuaryFor") + ": " + report.getName();
        printStartD();
        Qualifier qualifier = ((ReportQuery) dataPlugin.getEngine())
                .getHTMLReportQuery(report);
        if (qualifier == null) {
            printReport(report, null);
        } else
            printReportQuary(qualifier, Long.toString(report.getId()));
        printEndD();
    }

    private Row[] getSelRows(Qualifier clasificator) {
        final Enumeration e = params.keys();
        Vector v = new Vector();
        final String base = params.get("base");
        if (base == null) {
            while (e.hasMoreElements()) {
                final Object key = e.nextElement();
                final GlobalId id = GlobalId.convert(key.toString());
                if (id != null) {
                    if (!params.get(key).equals("")) {
                        final Row r = dataPlugin.findRowByGlobalId(id);
                        if (r != null)
                            v.add(r);
                    }
                }
            }
            final Vector rec = dataPlugin.getRecChildren(clasificator);
            final Vector res = new Vector();
            Object tmp;
            for (int i = 0; i < rec.size(); i++)
                if (v.indexOf(tmp = rec.get(i)) >= 0)
                    res.add(tmp);
            v = res;
        } else {
            try {
                final Row row = dataPlugin.findRowByGlobalId(GlobalId
                        .convert(base));
                v = dataPlugin.getRecChilds(row, true);
                v.insertElementAt(row, 0);
            } catch (final Exception ex) {

            }
        }
        final Row[] rows = new Row[v.size()];
        for (int i = 0; i < rows.length; i++)
            rows[i] = (Row) v.get(i);
        return rows;
    }

    private void printReport(final String string) throws IOException {

        final long num = Long.parseLong(string);
        Element report = null;
        report = dataPlugin.getEngine().getElement(num);

        if (report == null) {
            printError(RES.getString("reportEror"));
            return;
        }
        htmlTitle = report.getName();
        printStartD();
        Query query = null;
        Qualifier qualifier = ((ReportQuery) dataPlugin.getEngine())
                .getHTMLReportQuery(report);
        if (qualifier != null) {
            query = new Query(null);
            List<Element> elements = new ArrayList<Element>();
            for (Row row : getSelRows(qualifier)) {
                elements.add(row.getElement());
            }
            query.setElements(elements);
        }

        printReport(report, query);
        printEndD();
    }

    private void printReport(Element report, Query query) throws IOException {
        htmlTitle = report.getName();
        getReportHTMLText(dataPlugin.getEngine(), report, query);
    }

    protected Source getReportHTMLText(Engine engine, Element report,
                                       Query query) {
        String page = null;
        try {
            HashMap<String, Object> map = new HashMap<String, Object>();

            ReportQuery impl;
            if (dataPlugin.getEngine().getDeligate() instanceof IEngineImpl)
                impl = new ReportQueryImpl(engine) {
                    @Override
                    protected Out createOut(OutputStream stream) {
                        try {
                            return new Out(stream) {
                                @Override
                                public void print(Object object) {
                                    if (!printVersion) {
                                        if (object instanceof Qualifier) {
                                            Engine engine = dataPlugin
                                                    .getEngine();
                                            Element element = StandardAttributesPlugin
                                                    .getElement(
                                                            engine,
                                                            ((Qualifier) object)
                                                                    .getId());
                                            if (element == null) {
                                                print(object.toString());
                                            } else {
                                                String href = "rows/index.html?id="
                                                        + element

                                                        .getId();
                                                print(getStartATeg(href, false,
                                                        true));
                                                print(object.toString());
                                                print(getEndATeg());
                                            }
                                        } else if (object instanceof Code) {
                                            String href = "rows/index.html?id="
                                                    + ((Code) object)
                                                    .getElement()
                                                    .getId();
                                            print(getStartATeg(href, false,
                                                    true));
                                            print(object.toString());
                                            print(getEndATeg());
                                        } else if (object instanceof com.ramussoft.database.common.Row) {
                                            String href = "rows/index.html?id="
                                                    + ((com.ramussoft.database.common.Row) object)
                                                    .getElementId();
                                            print(getStartATeg(href, false,
                                                    true));
                                            print(object.toString());
                                            print(getEndATeg());

                                        } else
                                            super.print(object);
                                    } else
                                        super.print(object);
                                }
                            };
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                };
            else
                impl = (ReportQuery) dataPlugin.getEngine();

            if (query != null)
                map.put("query", query);
            page = impl.getHTMLReport(report, map);
        } catch (Exception e1) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            java.io.PrintStream s = null;
            try {
                s = new java.io.PrintStream(stream, true, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (e1 instanceof DataException)
                s.println(((DataException) e1)
                        .getMessage(new MessageFormatter() {

                            @Override
                            public String getString(String key,
                                                    Object[] arguments) {
                                return MessageFormat.format(
                                        ReportResourceManager.getString(key),
                                        arguments);
                            }
                        }));
            else {
                s.println("<pre>");
                e1.printStackTrace(s);
                s.println("</pre>");
            }

            s.flush();

            try {
                page = new String(stream.toByteArray(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            htmlStream.println(page);
            return null;
        }

        if (!printVersion) {
            htmlStream.println("<H4>" + report.getName() + "</H4>");
        }
        Source source = new Source(page);
        source.fullSequentialParse();
        htmlStream.println(source);
        return source;
    }

    private void printRowBase(final Row row) throws IOException {
        htmlStream.print("<canter><h2 class=\"Ramus\">");
        if (row instanceof Function)
            htmlStream.print(MovingFunction
                    .getIDEF0Kod((com.ramussoft.database.common.Row) row)
                    + " "
                    + row.getName());
        else
            htmlStream.print(row.getKod() + ". " + row.getName());
        if (row.isElement()) {
            Element element = StandardAttributesPlugin.getElement(
                    dataPlugin.getEngine(), row.getElement().getQualifierId());
            if (element != null) {
                htmlStream.print(" [");
                printStartATeg("rows/index.html?id=" + element.getId());
                htmlStream.print(element.getName());
                printEndATeg();
                htmlStream.print("]");
            }
        }
        htmlStream.println("</h2></canter>");
    }

    private class OtherElementMetadata implements
            Comparable<OtherElementMetadata> {
        Element element;
        Qualifier qualifier;
        Attribute attribute;

        @Override
        public int compareTo(OtherElementMetadata o) {
            if (this.qualifier.equals(o.qualifier)) {
                if (this.attribute.equals(o.attribute)) {
                    return collator.compare(this.element.getName(),
                            o.element.getName());
                } else {
                    return collator.compare(this.attribute.getName(),
                            o.attribute.getName());
                }
            } else {
                return collator.compare(this.qualifier.getName(),
                        o.qualifier.getName());
            }
        }

        public boolean isNotSystem() {
            return !isSystem();
        }

        private boolean isSystem() {
            return qualifier.isSystem();
        }
    }

    ;

    private void printRowAttributes(final Row row) throws IOException {
        Element element = row.getElement();
        Qualifier qualifier = dataPlugin.getEngine().getQualifier(
                element.getQualifierId());
        List<Attribute> attributes = qualifier.getAttributes();
        for (Attribute attr : attributes) {
            if (attr.getId() != qualifier.getAttributeForName()) {
                factory.printAttribute(htmlStream, dataPlugin, element, this,
                        attr);
            }
        }

        IEngine deligate = dataPlugin.getEngine().getDeligate();
        if (deligate instanceof IEngineImpl) {
            final IEngineImpl impl = (IEngineImpl) deligate;
            String prefix = impl.getPrefix();
            JDBCTemplate template = impl.getTemplate();

            List<OtherElementMetadata> list = template
                    .query("SELECT * FROM "
                                    + prefix
                                    + "attribute_other_elements a WHERE other_element=? AND value_branch_id IN (SELECT branch_id FROM "
                                    + prefix
                                    + "attributes_data_metadata WHERE attribute_id=a.attribute_id AND element_id=a.element_id)",
                            new RowMapper() {

                                private Hashtable<Long, Attribute> attrs = new Hashtable<Long, Attribute>();

                                private Hashtable<Long, Qualifier> qualifiers = new Hashtable<Long, Qualifier>();

                                @Override
                                public Object mapRow(ResultSet rs, int rowNum)
                                        throws SQLException {

                                    OtherElementMetadata metadata = new OtherElementMetadata();

                                    Element element = impl.getElement(rs
                                            .getLong("element_id"));
                                    if (element == null)
                                        return null;

                                    metadata.element = element;

                                    metadata.qualifier = getQualifier(element
                                            .getQualifierId());

                                    metadata.attribute = getAttribute(rs
                                            .getLong("attribute_id"));
                                    return metadata;

                                }

                                private Attribute getAttribute(Long id) {
                                    Attribute attribute = attrs.get(id);
                                    if (attribute == null) {
                                        attribute = dataPlugin.getEngine()
                                                .getAttribute(id);
                                        attrs.put(id, attribute);
                                    }
                                    return attribute;
                                }

                                private Qualifier getQualifier(Long id) {
                                    Qualifier qualifier = qualifiers.get(id);
                                    if (qualifier == null) {
                                        qualifier = dataPlugin.getEngine()
                                                .getQualifier(id);
                                        qualifiers.put(id, qualifier);
                                    }
                                    return qualifier;
                                }

                            }, new Object[]{row.getElement().getId()}, true);

            boolean print = false;
            for (OtherElementMetadata data : list)
                if (data.isNotSystem()) {
                    print = true;
                    break;
                }

            if (print) {
                Collections.sort(list);

                htmlStream.println("<table border=1>");
                htmlStream.println("<tr>");
                htmlStream.print("<td><b>");
                htmlStream.print(RES.getString("clasificator"));
                htmlStream.print("</b></td>");
                htmlStream.print("<td><b>");
                htmlStream.print(RES.getString("rowAttribute"));
                htmlStream.print("</b></td>");
                htmlStream.print("<td><b>");
                htmlStream.print(RES.getString("element"));
                htmlStream.print("</b></td>");
                htmlStream.println("</tr>");

                Hashtable<Qualifier, Element> hash = new Hashtable<Qualifier, Element>();

                for (OtherElementMetadata data : list)
                    if (data.isNotSystem()) {

                        Element element2 = hash.get(data.qualifier);
                        if (element2 == null) {
                            element2 = StandardAttributesPlugin.getElement(
                                    dataPlugin.getEngine(),
                                    data.qualifier.getId());
                            hash.put(data.qualifier, element2);
                        }

                        htmlStream.println("<tr>");

                        htmlStream.print("<td>");
                        printStartATeg("rows/index.html?id=" + element2.getId());
                        htmlStream.print(data.qualifier.getName());
                        printEndATeg();
                        htmlStream.print("</td>");
                        htmlStream.print("<td>");
                        htmlStream.print(data.attribute.getName());
                        htmlStream.print("</td>");
                        htmlStream.print("<td>");
                        printStartATeg("rows/index.html?id="
                                + data.element.getId());
                        htmlStream.print(data.element.getName());
                        printEndATeg();
                        htmlStream.print("</td>");

                        htmlStream.println("</tr>");
                    }

                htmlStream.println("</table><br>");

            }
        }
    }

    private void printClasificator() throws IOException {
        final Row row = loadRowById();
        if (row == null) {
            printError(RES.getString("cantLoatClasificator"));
            return;
        }
        if (row instanceof Function) {
            htmlTitle = MovingFunction
                    .getIDEF0Kod((com.ramussoft.database.common.Row) row)
                    + " "
                    + row.getName();
        } else
            htmlTitle = row.getKod() + ". " + row.getName();
        printStartD();
        printRowBase(row);
        if (row instanceof Function) {
            boolean b = true;
            if (!row.isLeaf()) {
                b = false;
                htmlStream.println("<br>");
                printStartATeg("idef0/index.html?id="
                        + row.getGlobalId().toString());
                htmlStream.print(RES.getString("idef0Model"));
                printEndATeg();
            }
            if (row.isElement()) {
                if (b)
                    htmlStream.println("<br>");
                else
                    htmlStream.print(" ");
                printStartATeg("idef0/index.html?id="
                        + row.getParentRow().getGlobalId().toString());
                htmlStream.print(RES.getString("ParentIDEF0Model"));
                printEndATeg();
            }
            htmlStream.println("<br>");
        } else if (row instanceof Stream
                && !dataPlugin.getBaseStream().equals(row)) {
            printStreamsRows((Stream) row);
        }

        boolean haveTable = false;

        Vector<Row> v = dataPlugin.getChilds(row, row.isElement());
        if (v.size() > 0) {
            if (haveTable)
                htmlStream.println("<br>");
            printElements(RES.getString("childElements"), v.toArray(),
                    row.isElement() ? "rows" : "clasificators");
            haveTable = true;
        }

        if (!row.isElement()) {
            if (haveTable)
                htmlStream.println("<br>");
            v = dataPlugin.getRecChilds(row, true);
            htmlStream.println("<hr>");
            String var = params.get("var");
            String attr = params.get("attr");
            if (var != null) {
                Engine engine = dataPlugin.getEngine();
                long attrId = Long.parseLong(attr);
                long varId = Long.parseLong(var);
                Attribute attribute = engine.getAttribute(attrId);
                List<VariantPropertyPersistent> list = (List<VariantPropertyPersistent>) engine
                        .getAttribute(null, attribute);
                VariantPropertyPersistent vpp = null;
                for (VariantPropertyPersistent vp : list)
                    if (vp.getVariantId() == varId) {
                        vpp = vp;
                        break;
                    }
                if ((vpp != null) && (vpp.getValue() != null)) {
                    String value = vpp.getValue();
                    ArrayList list1 = new ArrayList();
                    for (Row r : v) {
                        if (value.equals(((NRow) r).getAttribute(attribute)))
                            list1.add(r);
                    }
                    htmlStream.println("(" + value + ")");
                    htmlStream.println("<br>");
                    printElements(RES.getString("childElements"),
                            list1.toArray(), row.isElement() ? "rows"
                                    : "clasificators");
                }
            } else
                printElements(RES.getString("clasificatorElements"),
                        v.toArray());
        } else
            printRowAttributes(row);

        if (row instanceof Function && (row.isElement())) {
            printFunctionsArrows((Function) row);
        } else if (!(row instanceof Stream)) {
            printArrowFunctionss(row);
        }

        printEndD();
    }

    private class UF {
        public UF(final Function function, final int type) {
            super();
            this.function = function;
            this.type = type;
        }

        public Function function;

        public int type;

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result
                    + ((function == null) ? 0 : function.hashCode());
            result = prime * result + type;
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof UF))
                return false;
            UF other = (UF) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (function == null) {
                if (other.function != null)
                    return false;
            } else if (!function.equals(other.function))
                return false;
            if (type != other.type)
                return false;
            return true;
        }

        private HTTPParser getOuterType() {
            return HTTPParser.this;
        }
    }

    private void printArrowFunctionss(final Row row) throws IOException {
        final Vector funcs = new Vector<Row>();
        List<Qualifier> list = dataPlugin.getEngine().getQualifiers();
        for (Qualifier q : list) {
            if (IDEF0Plugin.isFunction(q)) {
                DataPlugin dp = NDataPluginFactory.getDataPlugin(q,
                        dataPlugin.getEngine(), dataPlugin.getAccessRules());
                funcs.addAll(dp.getRecChilds(dp.getBaseFunction(), true));
            }
        }

        final Vector<UF> res = new Vector<UF>();
        for (int i = 0; i < funcs.size(); i++) {
            addFunction(row, (Function) funcs.get(i), res);
        }
        if (res.size() > 0) {
            htmlStream.println("<p><table width=100% border=1>");
            printMainTableTitle(RES.getString("UseInFunctions"), 3);
            htmlStream.print("<tr>");
            htmlStream.print("<td><b>");
            htmlStream.print(RES.getString("kod"));
            htmlStream.print("</b></td>");

            htmlStream.print("<td width=100%><b>");
            htmlStream.print(RES.getString("FunctionName"));
            htmlStream.print("</b></td>");

            htmlStream.print("<td><b>");
            htmlStream.print(RES.getString("Type"));
            htmlStream.print("</b></td>");

            htmlStream.println("</tr>");

            for (int i = 0; i < res.size(); i++) {
                final UF uf = res.get(i);
                htmlStream.println("<tr>");
                htmlStream.print("<td>");
                printStartATeg("rows/index.html?id="
                        + uf.function.getGlobalId().toString());
                htmlStream
                        .print(MovingFunction
                                .getIDEF0Kod((com.ramussoft.database.common.Row) uf.function));
                htmlStream.print("</td>");
                htmlStream.print("<td width=100%>");
                printStartATeg("rows/index.html?id="
                        + uf.function.getGlobalId().toString());
                htmlStream.print(uf.function.getName());
                htmlStream.print("</td>");

                htmlStream.print("<td>");
                switch (uf.type) {
                    case MovingPanel.RIGHT:
                        htmlStream.print(RES.getString("RightElements"));
                        break;
                    case MovingPanel.LEFT:
                        htmlStream.print(RES.getString("LeftElements"));
                        break;
                    case MovingPanel.TOP:
                        htmlStream.print(RES.getString("TopElements"));
                        break;
                    case MovingPanel.BOTTOM:
                        htmlStream.print(RES.getString("BottomElements"));
                        break;
                }
                htmlStream.print("</td>");

                htmlStream.println("</tr>");
            }

            htmlStream.println("</table></p>");
        }
    }

    private void addFunction(final Row row, final Function function,
                             final Vector<UF> res) {
        if (!function.isHaveChilds()) {
            final Function par = (Function) function.getParent();
            boolean dfd = par.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFD;
            final Vector<Sector> sectors = par.getSectors();
            for (int i = 0; i < sectors.size(); i++) {
                final Sector s = sectors.get(i);
                final Stream stream = s.getStream();
                if (stream != null) {
                    if (function.equals(s.getStart().getFunction())) {
                        if (RowFactory.isPresent(stream.getAdded(), row)) {
                            final UF uf = new UF(function, s.getStart()
                                    .getFunctionType());
                            if (res.indexOf(uf) < 0) {
                                res.add(uf);
                                if (dfd)
                                    uf.type = MovingPanel.RIGHT;
                            }
                        }
                    } else if (function.equals(s.getEnd().getFunction())) {
                        if (RowFactory.isPresent(stream.getAdded(), row)) {
                            final UF uf = new UF(function, s.getEnd()
                                    .getFunctionType());
                            if (res.indexOf(uf) < 0) {
                                res.add(uf);
                                if (dfd)
                                    uf.type = MovingPanel.LEFT;
                            }
                        }
                    }
                }
            }
        }
    }

    private void printFunctionsArrows(final Function function)
            throws IOException {
        MatrixProjection projection = dataPlugin.getFastMatrixProjectionIDEF0(
                MovingPanel.TOP, function);
        Vector left = getStreams(function, projection);
        if (left.size() > 0)
            printFunctionArrows(left, RES.getString("TopElements"));
        projection = dataPlugin.getFastMatrixProjectionIDEF0(MovingPanel.LEFT,
                function);
        left = getStreams(function, projection);
        if (left.size() > 0)
            printFunctionArrows(left, RES.getString("LeftElements"));
        projection = dataPlugin.getFastMatrixProjectionIDEF0(MovingPanel.RIGHT,
                function);
        left = getStreams(function, projection);
        if (left.size() > 0)
            printFunctionArrows(left, RES.getString("RightElements"));
        projection = dataPlugin.getFastMatrixProjectionIDEF0(
                MovingPanel.BOTTOM, function);
        left = getStreams(function, projection);
        if (left.size() > 0)
            printFunctionArrows(left, RES.getString("BottomElements"));
    }

    private Vector getStreams(final Function function,
                              final MatrixProjection projection) {
        Vector left = projection.getLeft(function);
        final Vector x = new Vector();
        for (int i = 0; i < left.size(); i++) {
            final Sector s = (Sector) left.get(i);
            if (s.getStream() != null)
                x.add(s.getStream());
        }
        left = x;
        return left;
    }

    private void printFunctionArrows(final Vector left, final String string)
            throws IOException {
        final Vector<Row> v = new Vector<Row>();
        for (int i = 0; i < left.size(); i++) {
            final Stream stream = (Stream) left.get(i);
            final Row[] rs = stream.getAdded();
            for (final Row row : rs) {
                if (v.indexOf(row) < 0)
                    v.add(row);
            }
        }
        final Row[] rows = new Row[v.size()];
        for (int i = 0; i < rows.length; i++)
            rows[i] = v.get(i);

        if (rows.length == 0)
            return;

        RowFactory.sortByName(rows);

        htmlStream.println("<p>");
        htmlStream.println("<table border=1 width=100%>");
        printMainTableTitle(string, 3);
        htmlStream.print("<tr>");
        htmlStream.print("<td><b>");
        htmlStream.print(RES.getString("kod"));
        htmlStream.print("</b></td>");
        htmlStream.print("<td><b>");
        htmlStream.print(RES.getString("element"));
        htmlStream.print("</b></td>");
        htmlStream.print("<td><b>");
        htmlStream.print(RES.getString("clasificator"));
        htmlStream.print("</b></td>");
        htmlStream.println("</tr>");

        for (final Row row : rows) {
            htmlStream.print("<tr>");
            htmlStream.print("<td>");
            printStartATeg("rows/index.html?id=" + row.getGlobalId().toString());
            htmlStream.print(row.getKod());
            printEndATeg();
            htmlStream.print("</td>");
            htmlStream.print("<td width=100%>");
            printStartATeg("rows/index.html?id=" + row.getGlobalId().toString());
            htmlStream.print(row.getName());
            printEndATeg();
            htmlStream.print("</td>");
            Qualifier q = dataPlugin.getEngine().getQualifier(
                    row.getElement().getQualifierId());
            Element element = StandardAttributesPlugin.getElement(
                    dataPlugin.getEngine(), q.getId());
            htmlStream.print("<td>");
            String name = q.getName();
            if (element != null) {
                printStartATeg("rows/index.html?id=" + element.getId());
            } else {
                if (name.equals(StandardAttributesPlugin.QUALIFIERS_QUALIFIER))
                    name = RES.getString("clasificator");
            }
            htmlStream.print(name);
            if (element != null) {
                printEndATeg();
            }
            htmlStream.print("</td>");
            htmlStream.println("</tr>");
        }

        htmlStream.println("</table>");
        htmlStream.println("</p>");
    }

    private String getImagesFormatName() {
        switch (imageFormat) {
            case PIDEF0painter.PNG_FORMAT:
                return "png";
            case PIDEF0painter.BMP_FORMAT:
                return "bmp";
            case PIDEF0painter.JPEG_FORMAT:
                return "jpg";
        }
        return "png";
    }

    private Row loadRowById() {
        final String sId = (String) params.get("id");
        if (sId == null) {
            return null;
        }

        final GlobalId id = GlobalId.convert(sId);
        if (id == null) {
            return null;
        }
        return dataPlugin.findRowByGlobalId(id);
    }

    private String getAreaCoords(final FRectangle bounds, final MovingArea area) {
        return area.getIntOrdinate(bounds.getLocation().getX())
                + ","
                + (area.getIntOrdinate(bounds.getLocation().getY()) + area
                .getIntOrdinate(area.TOP_PART_A))
                + ","
                + area.getIntOrdinate(bounds.getRight())
                + ","
                + (area.getIntOrdinate(bounds.getBottom()) + area
                .getIntOrdinate(area.TOP_PART_A));
    }

    private void printIDEF0Model() throws IOException {
        int imageWidth = IMAGE_WIDTH;
        String s = (String) params.get("w");
        if (s != null) {
            try {
                imageWidth = new Integer(s).intValue();
            } catch (final Exception e) {
            }
        }
        int imageHeight = IMAGE_HEIGHT;
        s = (String) params.get("h");
        if (s != null) {
            try {
                imageHeight = new Integer(s).intValue();
            } catch (final Exception e) {
            }
        }
        Row row = loadRowById();

        Row old = row;

        if (row == null || !(row instanceof Function)) {
            printIDEF0Error();
            return;
        }
        row = replaceIDEF0Row(row);
        final Function function = (Function) row;
        String functionType;
        String name = function.getName();
        int iFunctionType = function.getType();
        Row ouner = null;
        if (function.getParent() == null) {
            final Enumeration e = function.children();
            while (e.hasMoreElements()) {
                final Function f = (Function) e.nextElement();
                ouner = f.getOwner();
                iFunctionType = f.getType();
                name = f.getName();
                if (ouner != null)
                    break;
            }
        } else
            ouner = function.getOwner();

        switch (iFunctionType) {
            case Function.TYPE_PROCESS_KOMPLEX:
                functionType = RES.getString("functionProcessKomplex");
                break;
            case Function.TYPE_PROCESS:
                functionType = RES.getString("functionProcess");
                break;
            case Function.TYPE_PROCESS_PART:
                functionType = RES.getString("functionProcessPart");
                break;
            case Function.TYPE_ACTION:
                functionType = RES.getString("functionAction");
                break;
            case Function.TYPE_OPERATION:
                functionType = RES.getString("functionOperation");
                break;
            default:
                functionType = "";
                break;
        }
        htmlTitle = functionType + " " + name;
        printStartD();
        if (printVersion) {

        } else {
            if (imageWidth != 800) {
                htmlStream.print("<a href=\"" + fromLink
                        + "idef0/index.html?id="
                        + function.getGlobalId().toString()
                        + "&w=800&h=600\" style=\"color:rgb(0,0,0);\">");
                htmlStream.print("800x600");
                htmlStream.println("</a>");
            }
            if (imageWidth != 905) {
                htmlStream.print("<a href=\"" + fromLink
                        + "idef0/index.html?id="
                        + function.getGlobalId().toString()
                        + "&w=905&h=700\" style=\"color:rgb(0,0,0);\">");
                htmlStream.print("905x700");
                htmlStream.println("</a>");
            }

            if (imageWidth != 1024) {
                htmlStream.print("<a href=\"" + fromLink
                        + "idef0/index.html?id="
                        + function.getGlobalId().toString()
                        + "&w=1024&h=768\" style=\"color:rgb(0,0,0);\">");
                htmlStream.print("1024x768");
                htmlStream.println("</a>");
            }

            if (imageWidth != 1152) {
                htmlStream.print("<a href=\"" + fromLink
                        + "idef0/index.html?id="
                        + function.getGlobalId().toString()
                        + "&w=1152&h=864\" style=\"color:rgb(0,0,0);\">");
                htmlStream.print("1152x864");
                htmlStream.println("</a>");
            }

            if (imageWidth != 1300) {
                htmlStream.print("<a href=\"" + fromLink
                        + "idef0/index.html?id="
                        + function.getGlobalId().toString()
                        + "&w=1300&h=1000\" style=\"color:rgb(0,0,0);\">");
                htmlStream.print("1300x1000");
                htmlStream.println("</a>");
            }

            if (imageWidth != 1600) {
                htmlStream.print("<a href=\"" + fromLink
                        + "idef0/index.html?id="
                        + function.getGlobalId().toString()
                        + "&w=1600&h=1200\" style=\"color:rgb(0,0,0);\">");
                htmlStream.print("1600x1200");
                htmlStream.println("</a>");
            }

            final Row parent = function.getParentRow();
            htmlStream.println(RES.getString("functionType") + ": <b>"
                    + functionType + "</b>");

            if (parent != null) {
                printStartATeg("idef0/index.html?id="
                        + parent.getGlobalId().toString());
                htmlStream.println(RES.getString("oneLevelTop"));
                printEndATeg();
                // printStartATeg("idef0/index.html?id="
                // + dataPlugin.getBaseFunction().getGlobalId().toString());
                htmlStream.println(RES.getString("contents"));
                printEndATeg();
            }

            if (ouner != null) {
                printStartATeg("rows/index.html?id="
                        + ouner.getGlobalId().toString());
                htmlStream.println(RES.getString("ouner") + ": "
                        + ouner.getKod() + ". " + ouner.getName());
                printEndATeg();
            }

            if (old == row) {
                printStartATeg("rows/index.html?id="
                        + function.getGlobalId().toString());
                htmlStream.println(RES.getString("element"));
                printEndATeg();
            }
            Row top = row;
            while (top.getParentRow() != null) {
                top = top.getParentRow();
            }
            printStartATeg("fullmodel/index.html?id="
                    + top.getElement().getId());
            htmlStream.println(RES.getString("ExpandedModel"));
            printEndATeg();
        }

        htmlStream.print("<br>");
        htmlStream.println("<img border=0 src=\"" + fromLink + "idef0/"
                + "model." + getImagesFormatName() + "?id="
                + function.getGlobalId().toString() + "&w=" + imageWidth
                + "&h=" + imageHeight + "\" useMap=#M"
                + function.getGlobalId().toString() + ">");

        htmlStream.println("<map name=M" + function.getGlobalId().toString()
                + ">");
        final Vector childs = dataPlugin.getChilds(function, true);
        final MovingArea area = PIDEF0painter.createMovingArea(new Dimension(
                imageWidth, imageHeight), dataPlugin, function);
        final SectorRefactor refactor = area.getRefactor();
        for (int i = 0; i < childs.size(); i++) {
            final Function fun = (Function) childs.get(i);
            Row row2 = dataPlugin.findRowByGlobalId(fun.getLink());
            String where = "rows";
            if ((row2 == null)
                    && (fun.getType() < Function.TYPE_EXTERNAL_REFERENCE)) {
                row2 = fun;
                if (!fun.isLeaf())
                    where = "idef0";
            }

            if (row2 != null) {

                htmlStream.print("<area shape=RECT coords="
                        + getAreaCoords(fun.getBounds(), area) + " href=\""
                        + fromLink + where + "/index.html?id="
                        + row2.getElement().getId() + "&w=" + imageWidth
                        + "&h=" + imageHeight
                        + (printVersion ? "&printVersion=true" : "") + "\"");
                htmlStream.println(">");
            }
        }

        refactor.loadFromFunction(function, false);
        final int sc = refactor.getSectorsCount();

        for (int i = 0; i < sc; i++) {
            final PaintSector sector = refactor.getSector(i);
            final Stream stream = sector.getStream();

            final MovingLabel text = refactor.getSector(i).getText();
            if (text != null && stream != null) {
                htmlStream.print("<area shape=RECT coords="
                        + getAreaCoords(text.getBounds(), area) + " href=\""
                        + fromLink + "rows/index.html?id="
                        + stream.getGlobalId().toString() + "&sectorId="
                        + sector.getSector().getGlobalId() + "&w=" + imageWidth
                        + "&h=" + imageHeight + "\"");
                htmlStream.println(">");
            }
            final int l = sector.getPinCount();
            for (int j = 0; j < l; j++)
                if (stream != null) {
                    final Pin pin = sector.getPin(j);
                    htmlStream.print("<area shape=RECT coords="
                            + getPinCoords(pin, area) + " href=\"" + fromLink
                            + "rows/index.html?id="
                            + stream.getGlobalId().toString() + "&sectorId="
                            + sector.getSector().getGlobalId() + "&w="
                            + imageWidth + "&h=" + imageHeight + "\"");
                    htmlStream.println(">");
                }
        }

        htmlStream.println("<map>");
        printEndD();
    }

    private Row replaceIDEF0Row(Row row) {
        if (IDEF0Plugin.getBaseFunctions(dataPlugin.getEngine()).getId() == row
                .getElement().getQualifierId()) {
            Qualifier base = IDEF0Plugin.getBaseQualifier(
                    dataPlugin.getEngine(), row.getElement());
            row = NDataPluginFactory.getDataPlugin(base,
                    dataPlugin.getEngine(), dataPlugin.getAccessRules())
                    .getBaseFunction();
        }
        return row;
    }

    private String getPinCoords(final Pin pin, final MovingArea area) {
        final double d = 2;
        final FRectangle bounds = new FRectangle();
        if (pin.getType() == PaintSector.PIN_TYPE_X) {
            double x1 = pin.getStart().getX();
            double x2 = pin.getEnd().getX();
            final double y = pin.getEnd().getY();
            if (x2 < x1) {
                final double x = x1;
                x1 = x2;
                x2 = x;
            }
            bounds.setX(x1);
            bounds.setWidth(x2 - x1);
            bounds.setY(y - d);
            bounds.setHeight(d * 2);
        } else {
            double y1 = pin.getStart().getY();
            double y2 = pin.getEnd().getY();
            final double x = pin.getEnd().getX();
            if (y2 < y1) {
                final double y = y1;
                y1 = y2;
                y2 = y;
            }
            bounds.setY(y1);
            bounds.setHeight(y2 - y1);
            bounds.setX(x - d);
            bounds.setWidth(d * 2);
        }
        return getAreaCoords(bounds, area);
    }

    private void printMatrixProjection() throws IOException {
        final String sId = (String) params.get("matrixid");
        if (sId == null) {
            printMatrixError();
            return;
        }
        final Long id = Long.parseLong(sId);
        if (id == null) {
            printMatrixError();
            return;
        }
        Engine engine = dataPlugin.getEngine();
        Attribute mp = engine.getAttribute(id);
        if (mp == null) {
            printMatrixError();
            return;
        }

        htmlTitle = RES.getString("matrixProjection") + " " + mp.getName();
        printStartD();
        boolean rotate = "true".equals(params.get("rotate"));
        htmlStream.print("<center><h2 class=\"Ramus\">"
                + RES.getString("matrixProjection") + ": " + mp.getName()
                + "</h2></center>");
        htmlStream.println("<table width=100%>");
        htmlStream.println("<tr>");
        htmlStream.println("<td  valign=top align=center width=50%>");

        ElementListPropertyPersistent pp = (ElementListPropertyPersistent) engine
                .getAttribute(null, mp);

        Qualifier qualifier1 = engine.getQualifier(pp.getQualifier1());
        Qualifier qualifier2 = engine.getQualifier(pp.getQualifier2());

        if (rotate) {
            Qualifier tmp = qualifier1;
            qualifier1 = qualifier2;
            qualifier2 = tmp;
        }

        htmlStream.println("<table border=1 width=100%>");

        List<Row> rows = (List) dataPlugin.getRowSet(qualifier1.getId())
                .getAllRows();

        printMainTableTitle(qualifier1.getName());
        final Row leftRow = loadRowById();
        for (int i = 0; i < rows.size(); i++) {
            final Row row = (Row) rows.get(i);
            final boolean gray = i % 2 == 0;
            String sGray = "";
            String bs = "";
            String be = "";
            if (row.equals(leftRow)) {
                bs = "<i>";
                be = "</i>";
            }

            if (gray)
                sGray = " bgcolor=" + GRAY;
            String href = "matrixprojections/index.html?id="
                    + row.getGlobalId().toString() + "&matrixid=" + sId
                    + (rotate ? "&rotate=true" : "");
            htmlStream.print("<tr>");
            htmlStream.print("<td" + sGray + ">");
            printStartATeg("rows/index.html?id=" + row.getElement().getId());
            htmlStream.print("^");
            printEndATeg();

            printStartATeg(href);
            htmlStream.print(bs);
            printRowKod(row, false, IDEF0Plugin.isFunction(qualifier1));
            htmlStream.print(be);
            if (href != null)
                printEndATeg();
            htmlStream.print("</td>");
            htmlStream.print("<td" + sGray + ">");
            printStartATeg(href);
            htmlStream.print(bs);
            printRowName(row, false);
            htmlStream.print(be);
            if (href != null)
                printEndATeg();
            htmlStream.print("</td>");
            htmlStream.println("</tr>");
        }

        htmlStream.println("</table>");
        htmlStream.println("</td>");
        htmlStream.println("<td valign=top align=center width=50%>");
        final String row2Name = qualifier2.getName()
                + " ("
                + RES.getString("addedElements")
                + (leftRow == null ? "" : " " + leftRow.getKod() + ". "
                + leftRow.getName()) + ")";
        if (leftRow != null) {
            List<ElementListPersistent> left = (List<ElementListPersistent>) engine
                    .getAttribute(leftRow.getElement(), mp);

            final Vector v = new Vector();

            for (ElementListPersistent p : left) {
                if (rotate) {
                    v.add(dataPlugin.findRowByGlobalId(p.getElement1Id()));
                } else {
                    v.add(dataPlugin.findRowByGlobalId(p.getElement2Id()));
                }
            }

            printElements(row2Name, v.toArray(), null);
        } else {

            htmlStream.println("<table width=100%>");
            htmlStream.println("<caption>");
            printSmallTitle(row2Name);
            htmlStream.println("</caption>");
            htmlStream.println("</table>");
        }
        htmlStream.println("</td>");
        htmlStream.println("</tr>");
        htmlStream.println("</table>");
        String href = "matrixprojections/index.html?matrixid=" + sId;
        if (!rotate)
            href += "&rotate=" + "true";
        htmlStream.println("<p align=right>");
        printStartATeg(href);
        htmlStream.println(RES.getString("rotate"));
        printEndATeg();
        htmlStream.println("</p>");

        printEndD();
    }

    private void printMatrixError() throws IOException {
        printError(RES.getString("matrixProjectionError"));
    }

    private void printIDEF0Error() throws IOException {
        printError(RES.getString("errorToLoadFunction"));
    }

    private void printStreamsRows(final Stream stream) throws IOException {
        final Row[] rows = stream.getAdded();
        if (rows == null || rows.length == 0)
            return;
        htmlStream.println("<hr>");
        String sectorId = params.get("sectorId");
        if (sectorId == null)
            sectorId = "";
        else
            sectorId = "&sectorId=" + sectorId;
        printElements(RES.getString("streamRows"), rows, "rows", sectorId,
                "<table border=1 width=100%>");

    }

    private void printElements(final String elementsName, final Object[] rows)
            throws IOException {
        printElements(elementsName, rows, "rows");
    }

    private void printElements(final String elementsName, final Object[] rows,
                               final String startLink) throws IOException {
        printElements(elementsName, rows, startLink, "",
                "<table border=1 width=100%>");
    }

    private void printElements(final String elementsName, final Object[] rows,
                               final String startLink, final String moreParams,
                               final String tableTag) throws IOException {
        if (rows.length == 0)
            return;
        htmlStream.println(tableTag);
        printMainTableTitle(elementsName);
        for (int i = 0; i < rows.length; i++) {
            final Row row = (Row) rows[i];
            final boolean gray = i % 2 == 0;
            String sGray = "";
            if (gray)
                sGray = " bgcolor=" + GRAY;
            String href;
            if (startLink == null)
                href = null;
            else
                href = startLink + "/index.html?id="
                        + row.getGlobalId().toString() + moreParams;
            htmlStream.print("<tr>");
            htmlStream.print("<td" + sGray + ">");
            printStartATeg(href);
            printRowKod(row, false);
            if (startLink != null)
                printEndATeg();
            htmlStream.print("</td>");
            htmlStream.print("<td" + sGray + ">");
            printStartATeg(href);
            printRowName(row, false);
            if (startLink != null)
                printEndATeg();
            htmlStream.print("</td>");
            htmlStream.println("</tr>");
        }
        htmlStream.println("</table>");
    }

    private void printError(final String string) throws IOException {
        htmlTitle = string;
        printStartD();
        htmlStream.print("<font size=+3 color=red>");
        htmlStream.print(string);
        htmlStream.println("</font>");
        printEndD();
    }

    private void printEmpty() throws IOException {
        htmlTitle = RES.getString("emptyTitle");
        printStartD();
        htmlStream.flush();
        copyFromResource(
                "/com/dsoft/pb/resources/" + RES.getString("startText"),
                htmlStream);
        printEndD();
    }

    protected void printEndD() throws IOException {
        if (!printVersion) {
            if (!startPage) {
                htmlStream.println("<hr><center>");
                printStartATeg("index.html", false, true);
                htmlStream.print(RES.getString("toStart"));
                printEndATeg();
                htmlStream.println("</center>");
            }
            htmlStream.println("</td>");
            htmlStream.println("</tr>");
            htmlStream.println("</table>");
        }
    }

    protected String getCurrentPageQuary(final Hashtable moreParams) {
        String res = getLocationForCurrentPage();
        boolean first = true;
        Enumeration e = params.keys();
        String p;
        while (e.hasMoreElements()) {
            final Object key = e.nextElement();
            if (first) {
                p = "?" + key.toString() + "=" + params.get(key).toString();
                first = false;
            } else
                p = "&" + key.toString() + "=" + params.get(key).toString();
            res += p;
        }
        e = moreParams.keys();
        while (e.hasMoreElements()) {
            final Object key = e.nextElement();
            if (first) {
                p = "?" + key.toString() + "=" + moreParams.get(key).toString();
                first = false;
            } else
                p = "&" + key.toString() + "=" + moreParams.get(key).toString();
            res += p;
        }
        return res;
    }

    protected String getLocationForCurrentPage() {
        return location;
    }

    protected synchronized void printStartD() throws IOException {
        if (printVersion)
            return;
        printTop();
        htmlStream.println("<table width=100%>");
        htmlStream.println("<tr>");
        htmlStream.println("<td align=center colspan=2>");
        htmlStream.println("<table width=100% bgcolor=" + GRAY + " width=100%"
                + " border=1 cellpadding=2 cellspacing=0 >");
        htmlStream.println("<tr>");
        htmlStream.println("<td align=center colspan=2 width=100%>");
        htmlStream.println(MessageFormat.format(
                RES.getString("pBuilderWebGenerator"), Main.getProgramName()
                        + " " + Main.getVersion()));
        /*
		 * printStartATeg("history/index.html");
		 * htmlStream.print(RES.getString("pbuilderHistory")); printEndATeg();
		 */
        final Hashtable h = new Hashtable();
        h.put("printVersion", "true");
        printStartATeg(getCurrentPageQuary(h), true);
        htmlStream.print(RES.getString("printVersion"));
        printEndATeg();
        htmlStream.print(" ");
        printOpenInNewWindow();
        htmlStream.println("</td>");
        htmlStream.println("</tr>");
        htmlStream.println("</table>");
        htmlStream.println("</td>");
        htmlStream.println("</tr>");
        htmlStream.println("<tr>");
        htmlStream.println("<td valign=top bgcolor=#EEEEFF width=20%>");
        htmlStream.println("<table>");
        printClasificatorsList();
        printModelsList();
        printMatrixProjectionsList();
        printReportsList();
        htmlStream.println("</table>");
        htmlStream.println("</td>");
        htmlStream.println("<td valign=top align=left>");
    }

    protected void printOpenInNewWindow() throws IOException {
        printStartATeg(getCurrentPageQuary(new Hashtable()), true);
        htmlStream.print(RES.getString("OpenInNewWindow"));
        printEndATeg();
    }

    protected void printTop() throws IOException {
        htmlStream.println("<div id=TopBar> </div>");
    }

    protected void printModelsList() throws IOException {
        printMainTableTitle(GlobalResourcesManager.getString("ModelsView"));
        for (Qualifier q : IDEF0Plugin
                .getBaseQualifiers(dataPlugin.getEngine())) {
            com.ramussoft.database.common.Row row = dataPlugin.getRowSet(
                    q.getId()).getRoot();
            final String href = "idef0/index.html?id="
                    + row.getElement().getId();
            htmlStream.print("<tr><td valign=\"top\" colspan=\"2\">");
            printStartATeg(href);
            htmlStream.print(q.getName());
            printEndATeg();
            htmlStream.println("</td></tr>");
        }

    }

    private void printSmallTitle(final String name) throws IOException {
        htmlStream.print("<b><font size=+1>");
        htmlStream.print(name);
        htmlStream.println("</font></b>");
    }

    private void printMainTableTitle(final String name, final int colspan)
            throws IOException {
        htmlStream.print("<tr><td colspan=" + colspan + ">");
        printSmallTitle(name);
        htmlStream.println("</td></tr>");
    }

    private void printMainTableTitle(final String name) throws IOException {
        printMainTableTitle(name, 2);
    }

    protected void printMatrixProjectionsList() throws IOException {
        final Attribute[] attrs = StandardAttributesPlugin
                .getElementLists(dataPlugin.getEngine());
        if (attrs.length <= 0)
            return;

        printMainTableTitle(RES.getString("matrixProjectionsTitle"));

        for (Attribute attr : attrs) {
            String name = attr.getName();
            htmlStream.println("<tr><td colspan=2>");
            printStartATeg("matrixprojections/index.html?matrixid="
                    + attr.getId());
            htmlStream.print(name);
            printEndATeg();
            htmlStream.println("</td></tr>");
        }

    }

    protected void printClasificatorsList() throws IOException {
        printMainTableTitle(RES.getString("clasificatorsTitle"));
        final Vector v = dataPlugin.getRecChilds(null, false);
        for (int i = 0; i < v.size(); i++) {
            final Row row = (Row) v.get(i);
            final String href = "clasificators/index.html?id="
                    + row.getGlobalId();
            htmlStream.print("<tr><td valign=\"top\">");
            printStartATeg(href);
            printRowKod(row, false);
            printEndATeg();
            htmlStream.print("</td><td>");
            printStartATeg(href);
            printRowName(row, false);
            printEndATeg();
            htmlStream.println("</td></tr>");
        }
    }

    private void outFunctionModel(final int format) throws IOException {
        Row row = loadRowById();
        if (row == null || !(row instanceof Function))
            return;

        row = replaceIDEF0Row(row);

        final Function f = (Function) row;
        int imageWidth = IMAGE_WIDTH;
        String s = (String) params.get("w");
        if (s != null) {
            try {
                imageWidth = new Integer(s).intValue();
                if (imageWidth > 2000)
                    imageWidth = 2000;
            } catch (final Exception e) {
            }
        }
        int imageHeight = IMAGE_HEIGHT;
        s = (String) params.get("h");
        if (s != null) {
            try {
                imageHeight = new Integer(s).intValue();
                if (imageHeight > 1600)
                    imageHeight = 1600;
            } catch (final Exception e) {
            }
        }
        final PIDEF0painter painter = new PIDEF0painter(f, new Dimension(
                imageWidth, imageHeight), dataPlugin);
        painter.writeToStream(stream, format);
    }

    @Override
    protected void doGet(final Request request, final Response response)
            throws IOException {
        this.request = request;
        doGet(request.getLocation(), request.getParams(), response);
    }

    public void doGet(final String location,
                      final Hashtable<String, String> params, final Response response)
            throws IOException {
        if (location == null)
            return;
        this.location = location;
        this.params = params;
        this.response = response;
        stream = response.getStream();
        htmlStream = new PrintStream(stream);
        startPage = false;
        printVersion = "true".equals(params.get("printVersion"));

        boolean printImage = false;

        try {

			/*
			 * System.out.println("location: " + location); Enumeration e =
			 * params.keys(); while (e.hasMoreElements()) { Object next =
			 * e.nextElement(); System.out.println(next + " = " +
			 * params.get(next)); }
			 */
            fromLink = "../";

            if (location.equals("") || location.equals("index.html")) {
                fromLink = "";
                startPage = true;
                printEmpty();
            } else if (location.equals("reportsq/index.html")) {
                printReportsQ();
            } else if (location.endsWith("default.css")) {
                response.setContentType("text/css");
                final InputStream is = getClass().getResourceAsStream(
                        "/com/dsoft/pb/resources/default.css");
                AbstractDataPlugin.copyStream(is, stream);
                printImage = true;
            } else if (location.endsWith("ramus.png")) {
                response.setContentType("image/png");
                printImage = true;
                final InputStream is = getClass().getResourceAsStream(
                        "/com/dsoft/pb/resources/ramus.png");
                AbstractDataPlugin.copyStream(is, stream);
            } else if (location.length() > "report_".length()
                    && location.substring(0, "report_".length()).equals(
                    "report_")) {
                final int len = location.length() - 1 - "index.html".length();
                if (len > 0) {
                    final String sNum = location.substring("report_".length(),
                            len);
                    try {
                        printReport(sNum);
                    } catch (Exception e) {
                        printError(e.getLocalizedMessage());
                        e.printStackTrace(htmlStream);
                    } finally {

                    }
                }
            } else if (location.equals("clasificators/index.html")
                    || location.equals("rows/index.html")) {
                printClasificator();
            } else if (location.equals("idef0/index.html")) {
                printIDEF0Model();
            } else if (location.equals("fullmodel/index.html")) {
                printFullIDEF0Model();
            } else if (location.equals("idef0/model.png")) {
                response.setContentType("image/png");
                response.writeHead();
                printImage = true;
                outFunctionModel(PIDEF0painter.PNG_FORMAT);
            } else if (location.equals("idef0/model.jpg")) {
                response.setContentType("image/jpeg");
                response.writeHead();
                outFunctionModel(PIDEF0painter.JPEG_FORMAT);
                printImage = true;
            } else if (location.equals("idef0/model.bmp")) {
                response.setContentType("image/bmp");
                response.writeHead();
                outFunctionModel(PIDEF0painter.BMP_FORMAT);
                printImage = true;
            } else if (location.equals("idef0/model.emf")) {
                response.setContentType("image/emf");
                response.writeHead();
                outFunctionModel(PIDEF0painter.EMF_FORMAT);
                printImage = true;
            } else if (location.equals("idef0/model.svg")) {
                response.setContentType("image/svg+xml");
                response.writeHead();
                outFunctionModel(PIDEF0painter.SVG_FORMAT);
                printImage = true;
            } else if (location.equals("matrixprojections/index.html")) {
                printMatrixProjection();
            } else if (location.equals("favicon.ico")) {
                favicon.writeTo(stream);
                printImage = true;
            } else if (location.equals("check_all.gif")) {
                response.setContentType("image/gif");
                final InputStream is = getClass().getResourceAsStream(
                        "/images/check_all.gif");
                AbstractDataPlugin.copyStream(is, stream);
                printImage = true;
            } else if (location.endsWith("file_getter.html")) {
                printFile();
                printImage = true;
            }

            try {
                if (printImage) {
                    // stream = response.getStream();
                    // out.writeTo(stream);
                } else {
                    response.setContentType("text/html; charset=" + ENCODING);
                    // stream = response.getStream();
                    printStart();
                    // out.re
                    htmlStream.realWrite(false);
                    printEnd();
                }
            } catch (final IOException e1) {

            }
            if (stream != null)
                stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void printFullIDEF0Model() throws IOException {
        int imageWidth = IMAGE_WIDTH;
        String s = (String) params.get("w");
        if (s != null) {
            try {
                imageWidth = new Integer(s).intValue();
            } catch (final Exception e) {
            }
        }
        int imageHeight = IMAGE_HEIGHT;
        s = (String) params.get("h");
        if (s != null) {
            try {
                imageHeight = new Integer(s).intValue();
            } catch (final Exception e) {
            }
        }
        Row row = loadRowById();
        htmlTitle = replaceIDEF0Row(row).getName();
        printStartD();
        printIDEF0Image(row, imageWidth, imageHeight);
        printEndD();
    }

    private void printIDEF0Image(Row aRow, int imageWidth, int imageHeight)
            throws IOException {
        Row row = replaceIDEF0Row(aRow);
        if (row.getChildCount() > 0) {
            printStartATeg("idef0/index.html?id=" + aRow.getElement().getId());
            htmlStream.print("<img border=0 src=\"" + fromLink + "idef0/"
                    + "model." + getImagesFormatName() + "?id="
                    + row.getElement().getId() + "&w=" + imageWidth + "&h="
                    + imageHeight + "\" alt=\""
                    + row.getName().replaceAll("\"", "\'") + "\">");
            printEndATeg();
            htmlStream.println("<br><br><br><br>");
            for (Row r : dataPlugin.getChilds(row, true)) {
                printIDEF0Image((Row) r, imageWidth, imageHeight);
            }
        }

    }

    private void printFile() throws IOException {
        long elementId = Long.parseLong(params.get("id"));
        long attributeId = Long.parseLong(params.get("attr"));
        Engine engine = dataPlugin.getEngine();
        FilePlugin plugin = (FilePlugin) engine.getPluginProperty("Core",
                FilePlugin.PLUGIN_NAME);
        String string = plugin.getFilePath(elementId, attributeId);

        List<Persistent>[] lists = engine.getBinaryAttribute(elementId,
                attributeId);
        if (lists[0].size() > 0) {
            FilePersistent fp = (FilePersistent) lists[0].get(0);

            String userAgent = request.getUserAgent();

            String encodedFileName = null;

            if ((userAgent != null)
                    && (userAgent.contains("MSIE") || userAgent
                    .contains("Opera"))) {

                encodedFileName = URLEncoder.encode(fp.getName(), "UTF-8");

            } else {

                byte[] bytes = fp.getName().getBytes("UTF-8");
                try {
                    encodedFileName = "=?UTF-8?B?"
                            + new String(Base64.encode(bytes), "UTF-8") + "?=";
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (params.get("prev") == null) {
                response.setContentDisposition("attachment; filename="
                        + encodedFileName);
                response.setRamusContentDisposition(toUtf8(fp.getName()));
            }
        }

        byte[] bytes = engine.getStream(string);

        response.writeHead();

        if (bytes != null)
            stream.write(bytes);

    }

    private String toUtf8(String name) throws UnsupportedEncodingException {
        TableToXML.ByteAConverter converter = new TableToXML.ByteAConverter();
        return converter.toHexString(name.getBytes("UTF-8"));
    }

    public OutputStream getStream() {
        return stream;
    }

    public String getFromLink() {
        return fromLink;
    }

    private static String getRecIDEF0Kod(final Row function) {
        final Row f = function.getParentRow();
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

    public static String getIDEF0Kod(final Row function) {
        final Row f = function.getParentRow();
        if (f == null)
            return "A-0";
        if (f.getParent() == null)
            return "A0";
        return "A" + getRecIDEF0Kod(function);
    }
}
