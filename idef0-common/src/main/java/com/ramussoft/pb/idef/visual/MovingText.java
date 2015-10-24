/*
 * Created on 23/4/2005
 */
package com.ramussoft.pb.idef.visual;

import java.awt.Color;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.utils.Options;
import com.ramussoft.common.Metadata;

/**
 * @author ZDD
 */
public class MovingText extends MovingPanel implements VisualPanel {
    protected final int movingSpotCount = 8;

    protected boolean transparent = true;

    private String text = "";

    private Font font = Options.getFont("DEFAULT_TEXT_FONT", new Font("Arial",
            0, 10));

    private Color color = Options.getColor("DEFAULT_TEXT_COLOR", Color.black);

    protected boolean negative = false;

    private JComponent textComponent = null;

    protected boolean isNegative() {
        return negative;
    }

    protected Color fiterColor(final Color color) {
        if (isNegative())
            return negative(color);
        else
            return color;
    }

    public static Color negative(final Color color) {
        return new Color(255 - color.getRed(), 255 - color.getGreen(),
                255 - color.getBlue());
    }

    /**
     * @param negative The negative to set.
     */
    public void setNegative(final boolean negative) {
        this.negative = negative;
    }

    /**
     * @param color The color to set.
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * @return Returns the color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return Returns the font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * @param font The font to set.
     */
    public void setFont(final Font font) {
        this.font = font;
    }

    /**
     * @param transparent The transparent to set.
     */
    public void setTransparent(final boolean transparent) {
        this.transparent = transparent;
    }

    /**
     * @return Returns the transparent.
     */
    public boolean isTransparent() {
        return transparent;
    }

    protected int align = com.ramussoft.pb.print.old.Line.CENTER_ALIGN;

    // protected boolean isEditing = true;

    // protected boolean editing = false;

    // private boolean paint = true;
    // protected boolean readOnly = false;

    protected void onMouseExited(final java.awt.event.MouseEvent e) {

    }

    protected void onMouseEntered(final java.awt.event.MouseEvent e) {

    }

    /**
     * @param align The align to set.
     */
    public void setAlign(final int align) {
        this.align = align;
    }

    /**
     * @return Returns the align.
     */
    public int getAlign() {
        return align;
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }

    public String getShownText() {
        final String text = getText();
        if (text == null || "".equals(text))
            return ResourceLoader.getString("text");
        return text;

    }

    /**
     * @param text The text to set.
     */
    public void setText(final String text) {
        this.text = text;
    }

    protected Rectangle2D getResetsBounds(final FRectangle old) {
        final String text = getXText();
        movingArea.stringBounder.setFont(getFont());
        return movingArea.stringBounder.getLinesBounds(text,
                new Rectangle2D.Double(0, 0, old.getWidth(), old.getHeight()));
    }

    /**
     * Метод підгоняє розміри області відображення тексту під текст, який
     * розташований на написі.
     */

    public void resetBounds() {
        final FRectangle old = getBounds();
        final Rectangle2D r = getResetsBounds(old);
        setBounds(old.getX()/*-((r.getWidth()-old.getWidth())/2)*/,
                old.getY()/*
                         * -(r.getHeight()-old.getHeight())/2
						 */, r.getWidth(), r.getHeight());
        // myBounds.setTransformNetBoundsMax(MovingArea.NET_LENGTH);
    }

    public FRectangle getTextBounds() {
        return getBounds();
    }

    protected String getXText() {
        String text = getText();
        if (text == null || text.equals(""))
            text = ResourceLoader.getString("text");
        return text;
    }

    @Override
    public void paint(final Graphics2D g) {
        paintText(g);
        super.paint(g);
    }

    protected void paintText(final Graphics2D g) {
        if (!transparent && !movingArea.isPrinting() || isNegative()) {
            final Rectangle2D rec = movingArea.getBounds(getBounds());
            g.setColor(fiterColor(movingArea.getBackground()));
            g.fill(rec);
        }
        g.setFont(getFont());
        g.setColor(fiterColor(getColor()));

        movingArea.paintText(g, getXText(), getTextBounds(), getAlign(), 1, true);
    }

    public MovingText(final MovingArea movingArea) {
        super(movingArea);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.frames.idf.MovingPanel#getMinHeight()
     */
    @Override
    public double getMinHeight() {
        return getResetsBounds(getBounds()).getHeight();
    }

    /**
     * @param g
     * @param area
     */
    public void paint(final Graphics2D g, final MovingArea area) {
        final MovingArea movingArea = this.movingArea;
        this.movingArea = area;
        try {
            if (getTextComponent() == null) {
                paint(g);
            } else {
                Rectangle2D r = movingArea.getBounds(getBounds());
                g.translate(r.getX(), r.getY());
                getTextComponent().paint(g);
                g.translate(-r.getX(), -r.getY());
            }
        } catch (Exception e) {
            if (Metadata.DEBUG)
                e.printStackTrace();
        }
        this.movingArea = movingArea;
    }

    public String getToolTipText() {
        return null;
    }

    protected void createEditFrame() {
        final Rectangle r = movingArea.getIBounds(getBounds());
        final JScrollPane pane = new JScrollPane();
        final JTextArea textArea = new JTextArea();
        pane.setViewportView(textArea);
        processTextArea(textArea);
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    stopEdit(textArea);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    cancelEdit(textArea);
            }
        });

        textArea.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                stopEdit(textArea);
            }

            @Override
            public void focusGained(FocusEvent e) {
            }
        });
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(getText());
        movingArea.getPanel().setActionDisable(true);
        movingArea.add(pane);
        movingArea.setbImage(null);
        this.setTextComponent(pane);
        pane.setBounds(r);
        movingArea.revalidate();
        movingArea.repaint();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                textArea.requestFocus();
            }
        });
    }

    protected void cancelEdit(JTextArea textArea) {
        movingArea.remove(MovingText.this.getTextComponent());
        movingArea.revalidate();
        movingArea.repaint();
        MovingText.this.setTextComponent(null);
        movingArea.getPanel().setActionDisable(false);
    }

    private void stopEdit(JTextArea textArea) {
        if (MovingText.this.getTextComponent() == null)
            return;
        setText(textArea.getText());
        movingArea.getRefactor().setUndoPoint();
        cancelEdit(textArea);
    }

    protected void processTextArea(JTextArea textArea) {
    }

    public void edit() {
        createEditFrame();
    }

    /**
     * @param textComponent the textComponent to set
     */
    public void setTextComponent(JComponent textComponent) {
        this.textComponent = textComponent;
    }

    /**
     * @return the textComponent
     */
    public JComponent getTextComponent() {
        return textComponent;
    }

    @Override
    public Color getBackgroundA() {
        return Color.WHITE;
    }

    @Override
    public Color getForegroundA() {
        return getColor();
    }

    @Override
    public Font getFontA() {
        return getFont();
    }

    @Override
    public void setBackgroundA(Color background) {
    }

    @Override
    public void setForegroundA(Color foreground) {
        color = foreground;
    }

    @Override
    public void setFontA(Font font) {
        this.font = font;
    }

    @Override
    public FRectangle getBoundsA() {
        return getBounds();
    }

    @Override
    public void setBoundsA(FRectangle bounds) {
        setBounds(bounds);
    }

    public double getRealWidth() {
        return getBounds().getWidth();
    }

    public double getRealHeight() {
        return getBounds().getHeight();
    }
}
