package com.ramussoft.report.html;

/**
 * Клас призначений для зберігання інформації про стиль, який необхідно замінити
 * на інший стиль.
 *
 * @author Яковчук В. В.
 */

public class ReplaceRight {

    private String tag;

    private String oldName;

    private String newName;

    private String newType;

    private String oldType;

    private boolean def;

    private Style style;

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public void setOldName(final String oldName) {
        this.oldName = oldName;
    }

    public void setNewName(final String newName) {
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewType() {
        return newType;
    }

    public void setNewType(final String newType) {
        this.newType = newType;
    }

    public String getOldType() {
        return oldType;
    }

    public void setOldType(final String oldType) {
        this.oldType = oldType;
    }

    public boolean isStyled() {
        return tag == null;
    }

    public boolean isDefault() {
        return def;
    }

    public void setDefault(final boolean def) {
        this.def = def;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(final Style style) {
        this.style = style;
    }
}
