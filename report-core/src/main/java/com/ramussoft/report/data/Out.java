package com.ramussoft.report.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import com.ramussoft.core.attribute.simple.HTMLPage;
import com.ramussoft.localefix.DecimalFormatWithFix;
import com.ramussoft.report.html.ReplaceRight;
import com.ramussoft.report.html.Style;
import com.ramussoft.report.html.Style.Counter;

public class Out extends PrintStream {

    private Vector<Style> styles = new Vector<Style>();

    private Counter counter = Style.createCounter();

    private OutputStream outputStream;

    private NumberFormat numberFormat = new DecimalFormatWithFix();

    private DateFormat dateFormat = DateFormat.getDateInstance();

    public Out(OutputStream out) throws UnsupportedEncodingException {
        super(new ByteArrayOutputStream(), true, "UTF-8");
        this.outputStream = out;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void print(Object object) {
        if (object == null)
            return;

        if (object instanceof Number) {
            print(numberFormat.format(object));
            return;
        }

        if (object instanceof Date) {
            print(dateFormat.format(object));
            return;
        }

        if (object instanceof List) {
            List list = (List) object;
            boolean first = true;
            for (Object obj : list) {
                if (first)
                    first = false;
                else
                    print("; ");
                print(obj);
            }
            return;
        }

        if (object instanceof HTMLPage) {
            try {
                printHTMLPage((HTMLPage) object);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (object instanceof Source) {
            try {
                printHTMLPage((Source) object);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (object instanceof Object[]) {
            Object[] objects = (Object[]) object;
            boolean first = true;
            for (Object object2 : objects) {
                if (first)
                    first = false;
                else
                    print("; ");
                print(object2);
            }
        } else
            super.print(object);
    }

    public void printHTMLPage(HTMLPage page) throws IOException {
        byte[] data = page.getData();
        if (data == null)
            return;
        Source source = new Source(new ByteArrayInputStream(data));
        source.fullSequentialParse();
        printHTMLPage(source);
    }

    public void printHTMLPage(Source source)
            throws UnsupportedEncodingException, IOException {
        List<StartTag> list = source.getAllStartTags(HTMLElementName.STYLE);
        Iterator<StartTag> iterator = list.iterator();
        String text = "";
        while (iterator.hasNext()) {
            final StartTag tag = iterator.next();
            final Segment s = new Segment(source, tag.getEnd(), tag
                    .getElement().getEndTag().getBegin());
            text += s.toString();
        }
        Vector<ReplaceRight> rights = Style.getStyles(text, styles, counter);

        // генерація сторінки з оновленими стилями.

        list = source.getAllStartTags();
        iterator = list.iterator();
        StartTag startTag = null;
        while (iterator.hasNext()) {
            final StartTag st = iterator.next();
            if (HTMLElementName.BODY.equals(st.getName())) {
                startTag = st;
                break;
            }
        }
        if (startTag == null)
            return;

        final StartTag body = startTag;
        final OutputDocument document = new OutputDocument(source);
        while (iterator.hasNext()) {
            startTag = iterator.next();
            replaceAttrs(startTag, document, rights);
        }

        OutputStreamWriter writer = new OutputStreamWriter(this.out, "UTF-8");
        document.writeTo(writer, body.getEnd(), body.getElement().getEndTag()
                .getBegin());
        writer.flush();
    }

    private static void replaceAttrs(final StartTag startTag,
                                     final OutputDocument document, final Vector<ReplaceRight> rights) {
        final Attributes as = startTag.getAttributes();
        Attribute cl = null;
        Attribute style = null;
        if (as != null) {
            cl = as.get("class");
            style = as.get("style");
        }
        Style s;
        String c = null;
        if (cl != null) {
            c = cl.toString();
        }
        if (style == null)
            s = new Style(null);
        else
            s = new Style(style.getValue());

        for (int i = 0; i < rights.size(); i++) {
            final ReplaceRight right = rights.get(i);
            if (!right.isStyled() && startTag.getName().equals(right.getTag())) {
                if (right.isDefault() && cl == null) {
                    c = "class=\"" + right.getNewName() + "\"";
                } else if (!right.isDefault() && cl != null) {
                    if (right.getOldName().equalsIgnoreCase(cl.getValue())) {
                        c = "class=\"" + right.getNewName() + "\"";
                    }
                } else
                    s.replaceRights(rights);
            }
        }
        final StringBuffer tag = new StringBuffer("<" + startTag.getName());
        if (c != null) {
            tag.append(' ');
            tag.append(c);
        }
        if (s.getAttributeCount() > 0) {
            tag.append(" style =\'");
            tag.append(s.toAttributeValue());
            tag.append('\'');
        }
        if (as != null) {
            final Iterator<Attribute> i = as.iterator();
            while (i.hasNext()) {
                final Attribute a = i.next();
                if (!a.getName().equalsIgnoreCase("style")
                        && !a.getName().equalsIgnoreCase("class")) {
                    tag.append(' ');
                    tag.append(a.toString());
                }
            }
        }
        tag.append('>');
        document.replace(startTag, tag);
    }

    public void realWrite() throws IOException {
        realWrite(true);
    }

    public void realWrite(boolean printHeadBodyTags) throws IOException {
        flush();
        ByteArrayOutputStream stream = (ByteArrayOutputStream) this.out;
        this.out = outputStream;
        if (!printHeadBodyTags) {
            stream.writeTo(outputStream);
            return;
        }
        println("<html>");
        println("<head>");
        println("<style>");
        for (Style style : styles)
            println(style);
        println("</style>");
        println("</head>");

        String htmlText = new String(stream.toByteArray(), "UTF-8");
        Source source = new Source(htmlText);
        source.fullSequentialParse();

        List<StartTag> startTags = source.getAllStartTags("body");
        if (startTags.size() == 0) {
            println("<body>");
            println(htmlText);
            println("</body>");
        } else {
            println(new StringBuffer(startTags.get(0).getElement()));
        }

        println("</html>");
    }

    @Override
    public void println(Object object) {
        print(object);
        println();
    }

    public Vector<Style> getStyles() {
        return styles;
    }

    public void realWriteWithHTMLUpdate() throws IOException {
        flush();
        ByteArrayOutputStream out = (ByteArrayOutputStream) this.out;
        Source source = new Source(new String(out.toByteArray(), "UTF-8"));
        source.fullSequentialParse();
        List<StartTag> list = source.getAllStartTags("html");
        if (list.size() == 0) {
            realWrite();
            return;
        }

        this.out = outputStream;

        OutputStreamWriter writer = new OutputStreamWriter(this.out, "UTF-8");
        OutputDocument document = new OutputDocument(source);

        StringBuffer style = new StringBuffer();
        if (this.styles.size() > 0) {
            for (Style style2 : this.styles)
                style.append(style2.toString());
        }

        List<StartTag> h = source.getAllStartTags("style");
        if (h.size() > 0) {
            document.insert(h.get(0).getElement().getEndTag().getBegin(), style);
        } else {

            style.insert(0, "\n<style>\n");
            style.append("</style>\n");

            h = source.getAllStartTags("head");
            if (h.size() > 0) {
                document.insert(h.get(0).getElement().getEndTag().getBegin(),
                        style);
            } else {
                style.insert(0, "\n<head>\n");
                style.append("</head>\n");
                document.insert(h.get(0).getElement().getEndTag().getBegin(),
                        style);
            }
        }
        document.writeTo(writer);
        writer.flush();

    }
}
