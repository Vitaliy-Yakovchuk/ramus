package com.ramussoft.report.html;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

public class Style {

    /**
     * Клас, що відповідає за назву новостворених стилів, після генерації нової
     * сторінки.
     *
     * @author Яковчук В.В.
     */

    public static class Counter {

        private int c = 0;

        public String next() {
            c++;
            return "rsfc" + c;
        }

    }

    ;

    public static Counter createCounter() {
        return new Counter();
    }

    /**
     * Метод додає нові стилі з унікальними назвами (якщо в унікальності є
     * необхідність).
     *
     * @param text     Текст, з якого будуть братись нові стилі.
     * @param currents Набір поточних стилів, які будуть додані.
     * @param counter  об’єкт, який відповідає за назву новостворених стилів.
     * @return Набір правил, за якими мають замінятись всі теги та їх значення.
     */

    public static Vector<ReplaceRight> getStyles(final String text,
                                                 final Vector<Style> currents, final Counter counter) {
        int i = 0;
        final StyleParser parser = new StyleParser();
        parser.parse(text);
        final Vector<Style> add = parser.getStyles();
        final Vector<ReplaceRight> result = new Vector<ReplaceRight>();
        for (i = 0; i < add.size(); i++) {
            final Style added = add.get(i);
            added.replaceRights(result);
            Style present = null;
            for (int j = 0; j < currents.size(); j++) {
                final Style c = currents.get(j);
                if (c.equals(added) && !c.isStyled()) {
                    present = c;
                    break;
                }
            }
            if (present != null) {
                final StyleHead[] hs = added.getHeads();
                for (final StyleHead h : hs) {
                    createRight(result, h, added, counter);
                    present.addHead(h);
                }
            } else {
                final StyleHead[] hs = added.getHeads();
                for (final StyleHead h : hs) {
                    createRight(result, h, added, counter);
                }
                currents.add(added);
            }
        }
        return result;
    }

    private static void createRight(final Vector<ReplaceRight> heads,
                                    final StyleHead oldHead, final Style style, final Counter counter) {
        if (oldHead.getParent() != null)
            return;
        final ReplaceRight h = new ReplaceRight();
        heads.add(h);
        final String cl = counter.next();
        if (oldHead.isStyled()) {
            h.setNewName(oldHead.getName().substring(1));
            h.setNewType(cl);
            if (!oldHead.isDefault()) {
                h.setOldType(oldHead.getType());
            } else
                h.setDefault(true);
            oldHead.setType(cl);
            h.setStyle(style);
        } else {
            h.setTag(oldHead.getTag());
            h.setNewName(cl);
            if (!oldHead.isDefault()) {
                h.setOldName(oldHead.getName());
            } else
                h.setDefault(true);
            oldHead.setName(cl);
        }
    }

    private static class StyleParser {

        private boolean isTagComment;

        private boolean isTag;

        private int isComment;

        private final Vector<Style> styles = new Vector<Style>();

        private Style style;

        private String head;

        private boolean isHead;

        private boolean isBody;

        private boolean isName;

        private boolean isValue;

        private boolean isL;

        private boolean isL1;

        private String value;

        private String name;

        private boolean isS;

        private String bHead;

        public StyleParser() {
            super();
            isTagComment = false;
            isComment = 0;
            isTag = false;
            head = "";
            isHead = false;
            isBody = false;
            addStyle();
        }

        public void parse(final String text) {
            final int l = text.length();
            for (int i = 0; i < l; i++)
                parse(text.charAt(i));
            initStyles();
        }

        private boolean isBed(final StyleHead head) {
            final String name = head.toString();
            for (int i = 0; i < name.length(); i++) {
                final char c = name.charAt(i);
                if (c == '>' || c == '<')
                    return true;
            }
            return false;
        }

        private boolean isStyleBed(final Style style) {
            final Vector<StyleHead> v = new Vector<StyleHead>(style.heads);
            for (int i = 0; i < v.size(); i++) {
                if (isBed(v.get(i)))
                    style.heads.remove(v.get(i));
            }
            return style.heads.size() == 0;
        }

        private void addStyle() {
            if (bHead != null) {
                style.addHead(bHead);
                bHead = null;
            }
            if (style != null) {
                if (isStyleBed(style))
                    styles.remove(style);
            }
            style = new Style();
            styles.add(style);
        }

        private void parse(final char c) {
            if (!isBody && c == '<') {
                isTag = true;
                return;
            }

            if (isTag) {
                if (c == '!') {
                    isTagComment = true;
                    isTag = false;
                    return;
                }
                isTag = false;
            }

            if (isTagComment) {
                if (c == '-')
                    return;
                isTagComment = false;
            }
            if (isComment > 0) {
                if (isComment == 1) {
                    if (c == '*') {
                        isComment = 2;
                        return;
                    } else if (c == '/') {
                        isComment = 3;
                        return;
                    } else
                        isComment = 0;
                } else if (isComment == 2) {
                    if (c == '*')
                        isComment = 4;
                    return;
                } else if (isComment == 3) {
                    if (c == '\n') {
                        isComment = 0;
                    }
                    return;
                } else if (isComment == 4) {
                    if (c == '/')
                        isComment = 0;
                    return;
                }
            } else if (!isBody && c == '/') {
                isComment = 1;
                return;
            }

            if (isBody) {
                if (isName) {
                    if (isP(c))
                        return;
                    if (c != ':')
                        name += c;
                    else {
                        isValue = true;
                        value = "";
                        isL = false;
                        isL1 = false;
                        isName = false;
                    }
                } else if (isValue) {
                    if (isL) {
                        if (c == '\"')
                            isL = false;
                    } else if (isL1) {
                        if (c == '\'')
                            isL1 = false;
                    } else if (c == '\'')
                        isL1 = true;
                    else if (c == '\"')
                        isL = true;
                    else if (c == ';') {
                        addAttribute();
                        isValue = false;
                        return;
                    } else if (c == '}') {
                        addAttribute();
                        isValue = false;
                        isBody = false;
                        addStyle();
                        return;
                    }
                    value += c;
                } else {
                    if (isP(c))
                        return;
                    if (c == '}') {
                        isBody = false;
                        addStyle();
                        return;
                    }
                    isName = true;
                    name += c;
                }

                return;
            }

            if (!isHead) {
                if (isP(c)) {
                    if (c == ',')
                        isS = true;
                    return;
                }
                if (c != '{') {
                    head += c;
                    isHead = true;
                }
            } else {
                if (isP(c) || c == '{') {
                    if (c == ',')
                        isS = true;
                    isHead = false;
                    addHead();
                } else
                    head += c;
            }
            if (c == '{') {
                isBody = true;
                isName = false;
                isValue = false;
                name = "";
                value = "";
                isS = false;
            }
        }

        private void addAttribute() {
            style.addAttribute(name, value);
            name = "";
            value = "";
        }

        private void addHead() {
            if (bHead != null) {
                if (!isS) {
                    bHead += " " + head;
                    style.addHead(bHead);
                    bHead = null;
                } else {
                    style.addHead(bHead);
                    bHead = head;
                }
            } else
                bHead = head;
            head = "";
        }

        private boolean isP(final char c) {
            return c == '\n' || c == '\r' || c == '\f' || c == '\t' || c == ','
                    || c == ' ';
        }

        private void initStyles() {
            if (bHead != null) {
                style.addHead(bHead);
                bHead = null;
            }
            if (style != null) {
                if (isStyleBed(style))
                    styles.remove(style);
                style = null;
            }

            for (int i = 0; i < styles.size(); i++) {
                final Style s = styles.get(i);
                StyleHead h = null;
                for (int j = 0; j < s.heads.size(); j++) {
                    if (s.heads.get(j).isStyled()) {
                        h = s.heads.get(j);
                        break;
                    }
                }
                if (h != null && h.getPrefix() == null) {
                    for (int j = 0; j < styles.size(); j++) {
                        final Style s1 = styles.get(j);
                        StyleHead h1 = null;
                        for (int k = 0; k < s1.heads.size(); k++)
                            if (s1.heads.get(k).isStyled()) {
                                h1 = s1.heads.get(k);
                                break;
                            }

                        if (h1 != null && h1.getPrefix() != null
                                && h1.getType().equalsIgnoreCase(h.getType())) {
                            h1.setParent(h);
                        }
                    }
                }
            }
        }

        public Vector<Style> getStyles() {
            return styles;
        }
    }

    ;

    private final Vector<Attribute> attributes = new Vector<Attribute>();

    private final Vector<StyleHead> heads = new Vector<StyleHead>();

    private final Vector<Style> childs = new Vector<Style>();

    /**
     * Повертає набір елементів, для яких застосовується даний стиль.
     *
     * @return Масив елементів, для яких застосовується даний стиль.
     */

    public StyleHead[] getHeads() {
        final StyleHead[] res = new StyleHead[heads.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = heads.get(i);
        return res;
    }

    public void addAttribute(final String name, final String value) {
        attributes.add(new Attribute(name, value));
    }

    private void addHead(final String head) {
        addHead(new StyleHead(head));
    }

    private void addHead(final StyleHead head) {
        heads.add(head);
    }

    private class Attribute {
        public String name;

        public String value;

        public Attribute(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public Attribute(final Attribute a) {
            value = a.value;
            name = a.name;
        }

        public Attribute(final String string) {
            final StringTokenizer st = new StringTokenizer(string, ":");
            name = st.nextToken().trim();
            if (st.hasMoreElements()) {
                value = st.nextToken();
            } else
                value = "";
        }

        @Override
        public String toString() {
            return "\t" + name + ":" + value + ";";
        }
    }

    ;

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < heads.size(); i++) {
            final StyleHead head = heads.get(i);
            if ("".equals(res))
                res += head.toString();
            else
                res += ", " + head.toString();
        }
        res += "\n{\n";

        for (int i = 0; i < attributes.size(); i++) {
            final Attribute a = attributes.get(i);
            res += a.toString() + "\n";
        }
        res += "}\n";
        for (int i = 0; i < childs.size(); i++) {

        }
        return res;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attributes == null) ? 0 : attributes.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Style))
            return false;
        Style other = (Style) obj;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        return true;
    }

    /**
     * Метод перевіряє, чи може мати тег стиль.
     *
     * @param tag Тег, для якого буде здійснена перевірка.
     * @return <code>true</code>, якщо тег може мати стиль, <code>false</code>,
     * якщо тег не може мати свій стиль.
     */

    public static boolean isStyledTag(final String tag) {
        return true;
    }

    public void replaceRights(final Vector<ReplaceRight> rights) {
        for (int i = 0; i < rights.size(); i++) {
            final ReplaceRight right = rights.get(i);
            if (right.isStyled()) {
                Attribute atr = null;
                for (int j = 0; j < attributes.size(); j++) {
                    final Attribute a = attributes.get(j);
                    if (a.name.equalsIgnoreCase(right.getNewName())) {
                        atr = a;
                    }
                }
                if (right.isDefault()) {
                    if (atr == null) {
                        addDefAttr(right);

                    }
                } else {
                    if (atr != null) {
                        if (atr.value.equals(right.getOldType()))
                            atr.value = right.getNewType();
                    }
                }
            }
        }
    }

    private void addDefAttr(final ReplaceRight right) {
        final Style s = right.getStyle();
        for (int i = 0; i < s.attributes.size(); i++) {
            final Attribute a = s.attributes.get(i);
            boolean have = false;
            for (int j = 0; j < attributes.size(); j++) {
                final Attribute a1 = attributes.get(j);
                if (a.name.equals(a1.name)) {
                    have = true;
                    break;
                }
            }
            if (!have)
                attributes.add(new Attribute(a));
        }
    }

    private boolean isStyled() {
        for (int i = 0; i < heads.size(); i++) {
            if (heads.get(i).isStyled())
                return true;
        }
        return false;
    }

    public Style(final String attr) {
        super();
        if (attr == null)
            return;
        if ("".equals(attr))
            return;
        final StringTokenizer st = new StringTokenizer(attr, ";");
        while (st.hasMoreElements()) {
            attributes.add(new Attribute(st.nextToken()));
        }
    }

    private Style() {
        super();
    }

    public int getAttributeCount() {
        return attributes.size();
    }

    public String toAttributeValue() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < attributes.size(); i++) {
            final Attribute a = attributes.get(i);
            sb.append(a.name);
            sb.append(':');
            sb.append(a.value);
            sb.append(';');
        }
        return sb.toString();
    }

    public static void main(final String[] args) {
        try {
            final FileInputStream is = new FileInputStream("d:/test.html");
            final FileOutputStream o = new FileOutputStream("d:/res1.txt");
            final Source source = new Source(is);
            final List<StartTag> list = source
                    .getAllStartTags(HTMLElementName.STYLE);
            final Iterator<StartTag> iterator = list.iterator();
            String text = "";
            final PrintStream out = new PrintStream(o);
            while (iterator.hasNext()) {
                final StartTag tag = iterator.next();
                final Segment s = new Segment(source, tag.getEnd(), tag
                        .getElement().getEndTag().getBegin());
                text += s.toString();
                // out.println(text);
                // out.println("---------------");
            }
            is.close();
            final Vector<Style> styles = new Vector<Style>();
            getStyles(text, styles, createCounter());
            for (int i = 0; i < styles.size(); i++) {
                out.println(styles.get(i));
            }
            o.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
