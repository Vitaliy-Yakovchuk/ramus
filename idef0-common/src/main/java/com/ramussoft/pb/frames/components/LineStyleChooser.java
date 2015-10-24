/*
 * Created on 15/10/2004
 */
package com.ramussoft.pb.frames.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ramussoft.pb.idef.elements.ArrowPainter;
import com.ramussoft.pb.idef.visual.ArrowedStroke;
import com.ramussoft.pb.idef.visual.WayStroke;

import javax.swing.JCheckBox;

/**
 * @author ZDD
 */
public class LineStyleChooser extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JScrollPane jScrollPane = null;

    private JList jList = null;

    private JCheckBox isDefaultArrowStyleCheckBox;

    private static Stroke strokes[] = new Stroke[21];

    static {
        int a, k = 0;

        for (a = 0; a < 5; a++) {
            strokes[k] = new BasicStroke(a + 0.5f) {
                @Override
                public String toString() {
                    return " ";
                }
            };
            k++;

            float f[] = {5 * (a + 1), 5 * (a + 1)};
            strokes[k] = new BasicStroke(a + 0.5f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 1, f, 0) {
                @Override
                public String toString() {
                    return " ";
                }
            };
            k++;
            f = new float[4];
            f[0] = 5 * (a + 1);
            f[1] = 5 * (a + 1);
            f[2] = a + 1;
            f[3] = 5 * (a + 1);

            strokes[k] = new BasicStroke(a + 0.5f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 1, f, 0) {
                @Override
                public String toString() {
                    return " ";
                }
            };
            k++;
        }
        final float arrowWidth = (float) ArrowPainter.ARROW_WIDTH;
        final float arrowHeight = (float) ArrowPainter.ARROW_HEIGHT;
        ArrowedStroke as1 = new ArrowedStroke(arrowWidth * 1.7f,
                arrowHeight * 1.7f) {
            @Override
            public String toString() {
                return " ";
            }
        };
        as1.setType(0);
        strokes[15] = as1;
        ArrowedStroke as2 = new ArrowedStroke(arrowWidth * 1.7f / 2f,
                arrowHeight * 1.7f / 2f) {
            @Override
            public String toString() {
                return " ";
            }
        };
        as2.setType(1);
        strokes[16] = as2;
        ArrowedStroke as3 = new ArrowedStroke(arrowWidth * 1.7f / 3f,
                arrowHeight * 1.7f / 3f) {
            @Override
            public String toString() {
                return " ";
            }
        };
        as3.setType(2);
        strokes[17] = as3;
        WayStroke ws1 = new WayStroke(arrowWidth * 1.7f) {
            @Override
            public String toString() {
                return " ";
            }
        };
        ws1.setType(0);
        strokes[18] = ws1;
        WayStroke ws2 = new WayStroke(arrowWidth * 1.7f / 2f) {
            @Override
            public String toString() {
                return " ";
            }
        };
        ws2.setType(1);
        strokes[19] = ws2;
        WayStroke ws3 = new WayStroke(arrowWidth * 1.7f / 3f) {
            @Override
            public String toString() {
                return " ";
            }
        };
        ws3.setType(2);
        strokes[20] = ws3;
    }

    public static Stroke[] getStrokes() {
        return strokes;
    }

    /**
     * This is the default constructor
     */
    public LineStyleChooser() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);

        isDefaultArrowStyleCheckBox = new JCheckBox("DEFAULT_ARROW_STROKE");
        add(isDefaultArrowStyleCheckBox, BorderLayout.SOUTH);
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jList
     *
     * @return javax.swing.JList
     */
    private JList getJList() {
        if (jList == null) {
            jList = new JList();
            jList.setCellRenderer(new DefaultListCellRenderer() {

                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                Stroke stroke;

                @Override
                public void paint(final Graphics gr) {
                    super.paint(gr);
                    final Graphics2D g = (Graphics2D) gr;

                    g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                            RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                            RenderingHints.VALUE_STROKE_NORMALIZE);
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

					/*
                     * g.setColor(getBackground()); g.fillRect(0, 0, getWidth(),
					 * getHeight());
					 */
                    g.setColor(getForeground());
                    g.setStroke(stroke);
                    g.draw(new Line2D.Double(0, (double) getHeight() / 2,
                            getWidth(), (double) getHeight() / 2));
                }

                @Override
                public Component getListCellRendererComponent(final JList list,
                                                              final Object value, final int index,
                                                              final boolean isSelected, final boolean cellHasFocus) {
                    stroke = (Stroke) value;
                    return super.getListCellRendererComponent(list, value,
                            index, isSelected, cellHasFocus);
                }
            });

            jList.setModel(new AbstractListModel() {

                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                public int getSize() {
                    return strokes.length;
                }

                public Object getElementAt(final int index) {
                    return strokes[index];
                }

            });
            jList.setSelectedIndex(0);
        }
        return jList;
    }

    public Stroke getStroke() {
        return (Stroke) jList.getSelectedValue();
    }

    public void setStroke(final Stroke stroke) {
        jList.setSelectedValue(stroke, true);
        isDefaultArrowStyleCheckBox.setSelected(false);
    }

    public boolean isDefaultArrowStyle() {
        return isDefaultArrowStyleCheckBox.isSelected();
    }
}
