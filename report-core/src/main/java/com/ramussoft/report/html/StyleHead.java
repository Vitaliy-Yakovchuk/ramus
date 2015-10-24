package com.ramussoft.report.html;

import java.util.StringTokenizer;

/**
 * Клас призначений для визначення набору елементів, для яких слід застосовувати
 * стиль.
 *
 * @author Яковчук В.В.
 */

public class StyleHead {

    private boolean def;

    private String tag;

    private String name;

    private boolean styled;

    private String type;

    private String prefix;

    private StyleHead parent;

    public StyleHead(final String head) {
        super();
        if (head.charAt(0) == '@') {
            styled = true;
            final StringTokenizer st = new StringTokenizer(head, " ");
            name = st.nextToken();
            if (st.hasMoreElements()) {
                def = false;
                final StringTokenizer st1 = new StringTokenizer(st.nextToken(), ":");
                type = st1.nextToken();
                if (st1.hasMoreElements())
                    prefix = st1.nextToken();
            } else
                def = true;
        } else {
            styled = false;
            final StringTokenizer st = new StringTokenizer(head, ".");
            tag = st.nextToken();
            if (st.hasMoreTokens()) {
                name = st.nextToken();
                def = false;
            } else
                def = true;
        }
    }

    /**
     * Перевіряє, чи являється стиль типом стиля.
     *
     * @return <code>true</code> - являється,<br>
     * <code>false</code> - інакше.
     */

    public boolean isStyled() {
        return styled;
    }

    /**
     * Метод перевіряє, чи являється стиль стилем по-замовчуванню.
     *
     * @return <code>true</code> - стиль по-замовчуванню (типу
     * @font-face і т.д.), <code>false</code> - інакше.
     */

    public boolean isDefault() {
        return def;
    }

    @Override
    public String toString() {
        if (parent != null) {
            return parent.name + ' ' + parent.type + ":" + prefix;
        }
        if (isStyled()) {
            if (type == null)
                return name;
            if (prefix == null)
                return name + " " + type;
            return name + " " + type + ':' + prefix;
        }
        if (name == null)
            return tag;
        return tag + "." + name;
    }

    /**
     * Повертає тег, для якого призначений стиль.
     *
     * @return Тег, для якого призначений стиль. Якщо стиль не призначений для
     * стиля виникає помилка {@link NullPointerException}.
     */

    public String getTag() {
        return tag.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setParent(final StyleHead parent) {
        this.parent = parent;
    }

    public StyleHead getParent() {
        return parent;
    }
}
