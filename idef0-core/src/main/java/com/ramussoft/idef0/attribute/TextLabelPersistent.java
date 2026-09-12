package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Double;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;
import com.ramussoft.common.persistent.Text;

/**
 * Вільний текстовий підпис на діаграмі.
 * <p>
 * Раніше підписи лежали всередині двійкового поля {@code F_VISUAL_DATA} разом
 * зі своїми шрифтом, кольором і межами. Це був останній блоб у моделі: у
 * текстовому форматі він перетворювався на рядок base64 і робив половину файлу
 * нечитабельною. Тепер це звичайна таблиця з іменованими полями.
 */
@Table(name = "text_labels", type = TableType.ONE_TO_MANY)
public class TextLabelPersistent extends AbstractPersistent implements
        Comparable<TextLabelPersistent> {

    private static final long serialVersionUID = 4460521145539228781L;

    private int position;

    private String text = "";

    private double x;

    private double y;

    private double width;

    private double height;

    private String fontName;

    private int fontStyle;

    private int fontSize;

    /**
     * Колір у форматі ARGB. Зберігається числом, бо саме так його віддає
     * {@link java.awt.Color#getRGB()}; {@code null} означає «типовий».
     */
    private java.lang.Integer color;

    /**
     * Порядок підпису на діаграмі.
     * <p>
     * Входить у первинний ключ. Для таблиці «один до багатьох» ключа
     * «елемент + атрибут» недостатньо: усі підписи однієї діаграми мали б
     * однаковий ключ, і при збереженні лишався б тільки останній.
     * Заразом задає сталу послідовність — без нього порядок визначала б СУБД.
     */
    @Integer(id = 2, primary = true)
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Text(id = 3)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    @Double(id = 4)
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Double(id = 5)
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Double(id = 6)
    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    @Double(id = 7)
    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Text(id = 8)
    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    @Integer(id = 9)
    public int getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(int fontStyle) {
        this.fontStyle = fontStyle;
    }

    @Integer(id = 10)
    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    @Integer(id = 11)
    public java.lang.Integer getColor() {
        return color;
    }

    public void setColor(java.lang.Integer color) {
        this.color = color;
    }

    @Override
    public int compareTo(TextLabelPersistent other) {
        return java.lang.Integer.compare(position, other.position);
    }
}
