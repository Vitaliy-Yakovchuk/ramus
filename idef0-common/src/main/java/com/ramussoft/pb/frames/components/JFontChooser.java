/*
 * Created on 6/8/2005
 */
package com.ramussoft.pb.frames.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataListener;

import com.dsoft.pb.idef.ResourceLoader;

/**
 * @author ZDD
 */
public class JFontChooser extends JPanel {

    private JList jList = null;

    private JList jList2 = null;

    private JList jList1 = null;

    private String fns[];

    private final int[] styles = {0, Font.BOLD, Font.ITALIC,
            Font.BOLD | Font.ITALIC};

    public Font getSelFont() {
        int size = 10;
        try {
            size = Integer.parseInt(jLabel11.getText());
        } catch (Exception e) {

        }

        return new Font(jLabel7.getText(),
                styles[jList1.getSelectedIndex() > 0 ? jList1
                        .getSelectedIndex() : 0], size);
    }

    private static String vals[];

    private JPanel jPanel = null; // @jve:decl-index=0:visual-constraint="476,71"

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JPanel jPanel21 = null;

    private JLabel jLabel7 = null;

    private JPanel jPanel22 = null;

    private JPanel jPanel23 = null;

    private JScrollPane jScrollPane = null;

    private JPanel jPanel24 = null;

    private JPanel jPanel25 = null;

    private JPanel jPanel26 = null;

    private JPanel jPanel28 = null;

    private JPanel jPanel29 = null;

    private JLabel jLabel9 = null;

    private JLabel jLabel10 = null;

    private JPanel jPanel210 = null;

    private JScrollPane jScrollPane1 = null;

    private JScrollPane jScrollPane5 = null;

    private JLabel jLabel11 = null;

    private JLabel jLabel12 = null;

    private JPanel jPanel211 = null;

    private JPanel jPanel212 = null;

    private JPanel jPanel213 = null;

    private JPanel jPanel27 = null;

    private JLabel jLabel13 = null;

    private JLabel jLabel6 = null;

    /**
     * This method initializes jList2
     *
     * @return javax.swing.JList
     */
    private JList getJList2() {
        if (jList2 == null) {
            jList2 = new JList();
            vals = new String[72 - 1 + 1];
            for (int i = 1; i <= 72; i++)
                vals[i - 1] = Integer.toString(i);
            jList2.setModel(new ListModel() {

                public int getSize() {
                    return vals.length;
                }

                public Object getElementAt(final int arg0) {
                    return vals[arg0];
                }

                public void addListDataListener(final ListDataListener arg0) {

                }

                public void removeListDataListener(final ListDataListener arg0) {

                }

            });
            jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(
                        final javax.swing.event.ListSelectionEvent e) {
                    jLabel11.setText((String) jList2.getSelectedValue());
                    getJPanel25().repaint();
                    fireFontUpdated();
                }
            });

        }
        return jList2;
    }

    /**
     * This method initializes jList1
     *
     * @return javax.swing.JList
     */
    private JList getJList1() {
        if (jList1 == null) {
            jList1 = new JList();
            jList1.setModel(new ListModel() {

                private final String texts[] = {"regular", "bold", "italic",
                        "bold_italic"};

                public int getSize() {
                    return texts.length;
                }

                public Object getElementAt(final int arg0) {
                    return ResourceLoader.getString(texts[arg0]);
                }

                public void addListDataListener(final ListDataListener arg0) {

                }

                public void removeListDataListener(final ListDataListener arg0) {

                }

            });
            jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(
                        final javax.swing.event.ListSelectionEvent e) {
                    jLabel10.setText((String) jList1.getSelectedValue());
                    getJPanel25().repaint();
                    fireFontUpdated();
                }
            });
            jList1.setSelectedValue("Dialog", true);
        }
        return jList1;
    }

    private JList getJList() {
        if (jList == null) {
            jList = new JList();
            fns = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames();

            jList.setModel(new ListModel() {

                public int getSize() {
                    return fns.length;
                }

                public Object getElementAt(final int arg0) {
                    return fns[arg0];
                }

                public void addListDataListener(final ListDataListener arg0) {

                }

                public void removeListDataListener(final ListDataListener arg0) {

                }

            });
            jList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(
                        final javax.swing.event.ListSelectionEvent e) {
                    jLabel7.setText((String) jList.getSelectedValue());
                    getJPanel25().repaint();
                    fireFontUpdated();
                }
            });

        }
        return jList;
    }

    /**
     * This is the default constructor
     */
    public JFontChooser() {
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
        this.setSize(411, 426);
        this.add(getJPanel(), java.awt.BorderLayout.CENTER);
    }

    /**
     * @param font
     */
    public void setSelFont(final Font font) {
        jList.setSelectedValue(font.getName(), true);
        int stylei = 0;
        for (int i = 0; i < styles.length; i++)
            if (styles[i] == font.getStyle())
                stylei = i;
        jList1.setSelectedIndex(stylei);
        jList2.setSelectedValue(Integer.toString(font.getSize()), true);

    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            final GridBagConstraints gridBagConstraints181 = new GridBagConstraints();
            gridBagConstraints181.gridx = 1;
            gridBagConstraints181.gridy = 2;
            final GridBagConstraints gridBagConstraints171 = new GridBagConstraints();
            gridBagConstraints171.gridx = 1;
            gridBagConstraints171.gridy = 0;
            final GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
            gridBagConstraints91.gridx = 0;
            gridBagConstraints91.gridy = 0;
            jLabel12 = new JLabel();
            jLabel12.setText("");
            final GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 3;
            gridBagConstraints11.fill = GridBagConstraints.BOTH;
            gridBagConstraints11.anchor = GridBagConstraints.NORTH;
            gridBagConstraints11.weightx = 1.0D;
            gridBagConstraints11.weighty = 1.0D;
            gridBagConstraints11.gridy = 1;
            final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 1;
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.CENTER;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridheight = 1;
            gridBagConstraints.weightx = 1.0D;
            gridBagConstraints.gridy = 1;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setSize(new Dimension(347, 396));
            jPanel.add(getJPanel1(), gridBagConstraints);
            jPanel.add(getJPanel2(), gridBagConstraints1);
            jPanel.add(getJPanel21(), gridBagConstraints11);
            jPanel.add(jLabel12, gridBagConstraints91);
            jPanel.add(getJPanel213(), gridBagConstraints171);
            jPanel.add(getJPanel211(), gridBagConstraints181);
        }
        return jPanel;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            final GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
            gridBagConstraints19.gridx = 2;
            gridBagConstraints19.anchor = GridBagConstraints.WEST;
            gridBagConstraints19.gridy = 2;
            final GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
            gridBagConstraints23.gridx = 0;
            gridBagConstraints23.gridy = 6;
            final GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = GridBagConstraints.BOTH;
            gridBagConstraints6.gridy = 7;
            gridBagConstraints6.weightx = 1.0;
            gridBagConstraints6.weighty = 1.0;
            gridBagConstraints6.gridx = 2;
            final GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 2;
            gridBagConstraints5.gridy = 6;
            final GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 2;
            gridBagConstraints4.anchor = GridBagConstraints.WEST;
            gridBagConstraints4.gridy = 3;
            final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 2;
            gridBagConstraints3.fill = GridBagConstraints.NONE;
            gridBagConstraints3.anchor = GridBagConstraints.WEST;
            gridBagConstraints3.gridy = 5;
            jLabel7 = new JLabel();
            jLabel7.setText("");
            jPanel1 = new JPanel();
            jPanel1.setLayout(new GridBagLayout());
            jPanel1.add(jLabel7, gridBagConstraints3);
            jPanel1.add(getJPanel22(), gridBagConstraints4);
            jPanel1.add(getJPanel23(), gridBagConstraints5);
            jPanel1.add(getJScrollPane(), gridBagConstraints6);
            jPanel1.add(getJPanel212(), gridBagConstraints23);
            jPanel1.add(jLabel13, gridBagConstraints19);
        }
        return jPanel1;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setLayout(new FlowLayout());
        }
        return jPanel2;
    }

    /**
     * This method initializes jPanel21
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel21() {
        if (jPanel21 == null) {
            final GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
            gridBagConstraints22.gridx = 2;
            gridBagConstraints22.gridy = 1;
            final GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 1;
            gridBagConstraints9.gridy = 1;
            final GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 1;
            gridBagConstraints8.fill = GridBagConstraints.BOTH;
            gridBagConstraints8.weightx = 1.0D;
            gridBagConstraints8.weighty = 1.0D;
            gridBagConstraints8.gridy = 4;
            final GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.insets = new Insets(5, 5, 5, 5);
            gridBagConstraints7.gridy = 0;
            gridBagConstraints7.fill = GridBagConstraints.BOTH;
            gridBagConstraints7.weighty = 1.0D;
            gridBagConstraints7.gridx = 1;
            jPanel21 = new JPanel();
            jPanel21.setLayout(new GridBagLayout());
            jPanel21.add(getJPanel24(), gridBagConstraints7);
            jPanel21.add(getJPanel25(), gridBagConstraints8);
            jPanel21.add(getJPanel26(), gridBagConstraints9);
            jPanel21.add(getJPanel27(), gridBagConstraints22);
        }
        return jPanel21;
    }

    /**
     * This method initializes jPanel22
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel22() {
        if (jPanel22 == null) {
            jLabel13 = new JLabel();
            jLabel13.setText("font:");
            jPanel22 = new JPanel();
            jPanel22.setLayout(new FlowLayout());
        }
        return jPanel22;
    }

    /**
     * This method initializes jPanel23
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel23() {
        if (jPanel23 == null) {
            jPanel23 = new JPanel();
            jPanel23.setLayout(new FlowLayout());
        }
        return jPanel23;
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
     * This method initializes jPanel24
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel24() {
        if (jPanel24 == null) {
            final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.anchor = GridBagConstraints.WEST;
            gridBagConstraints2.gridy = 1;
            jLabel6 = new JLabel();
            jLabel6.setText("style:");
            final GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 2;
            gridBagConstraints21.fill = GridBagConstraints.NONE;
            gridBagConstraints21.anchor = GridBagConstraints.WEST;
            gridBagConstraints21.gridy = 4;
            jLabel11 = new JLabel();
            jLabel11.setText("");
            final GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
            gridBagConstraints18.fill = GridBagConstraints.BOTH;
            gridBagConstraints18.gridy = 6;
            gridBagConstraints18.weightx = 1.0;
            gridBagConstraints18.weighty = 1.0;
            gridBagConstraints18.gridx = 2;
            final GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
            gridBagConstraints17.fill = GridBagConstraints.BOTH;
            gridBagConstraints17.gridy = 6;
            gridBagConstraints17.weightx = 1.0;
            gridBagConstraints17.weighty = 1.0;
            gridBagConstraints17.gridx = 0;
            final GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.gridx = 0;
            gridBagConstraints16.gridy = 5;
            final GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.fill = GridBagConstraints.NONE;
            gridBagConstraints15.anchor = GridBagConstraints.WEST;
            gridBagConstraints15.gridy = 4;
            jLabel10 = new JLabel();
            jLabel10.setText("");
            final GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            gridBagConstraints14.gridx = 2;
            gridBagConstraints14.fill = GridBagConstraints.NONE;
            gridBagConstraints14.anchor = GridBagConstraints.WEST;
            gridBagConstraints14.gridy = 1;
            jLabel9 = new JLabel();
            jLabel9.setText("size:");
            final GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.gridx = 0;
            gridBagConstraints13.gridy = 2;
            final GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 1;
            gridBagConstraints12.gridy = 1;
            jPanel24 = new JPanel();
            jPanel24.setLayout(new GridBagLayout());
            jPanel24.add(getJPanel28(), gridBagConstraints12);
            jPanel24.add(getJPanel29(), gridBagConstraints13);
            jPanel24.add(jLabel9, gridBagConstraints14);
            jPanel24.add(jLabel10, gridBagConstraints15);
            jPanel24.add(getJPanel210(), gridBagConstraints16);
            jPanel24.add(getJScrollPane1(), gridBagConstraints17);
            jPanel24.add(getJScrollPane5(), gridBagConstraints18);
            jPanel24.add(jLabel11, gridBagConstraints21);
            jPanel24.add(jLabel6, gridBagConstraints2);
        }
        return jPanel24;
    }

    /**
     * This method initializes jPanel25
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel25() {
        if (jPanel25 == null) {
            final GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
            gridBagConstraints20.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints20.gridy = 0;
            gridBagConstraints20.weightx = 1.0;
            gridBagConstraints20.gridx = 0;
            jPanel25 = new JPanel() {
                @Override
                public void paint(final Graphics arg0) {
                    super.paint(arg0);
                    final Graphics2D g = (Graphics2D) arg0;
                    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g.setFont(getSelFont());
                    final String text = ResourceLoader.getString("example");
                    final int w = getBounds().width;
                    final int h = getBounds().height;
                    final int tw = (int) g.getFont()
                            .getStringBounds(text, g.getFontRenderContext())
                            .getWidth();
                    final int th = (int) g.getFont()
                            .getStringBounds(text, g.getFontRenderContext())
                            .getHeight();
                    g.drawString(text, (w - tw) / 2, h - (h - th) / 2);
                }
            };
            jPanel25.setLayout(null);
            jPanel25.setBorder(BorderFactory.createTitledBorder(null,
                    ResourceLoader.getString("sample"),
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, new Font("Dialog",
                            Font.BOLD, 12), new Color(51, 51, 51)));
        }
        return jPanel25;
    }

    /**
     * This method initializes jPanel26
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel26() {
        if (jPanel26 == null) {
            jPanel26 = new JPanel();
            jPanel26.setLayout(new FlowLayout());
        }
        return jPanel26;
    }

    /**
     * This method initializes jPanel28
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel28() {
        if (jPanel28 == null) {
            jPanel28 = new JPanel();
            jPanel28.setLayout(new FlowLayout());
        }
        return jPanel28;
    }

    /**
     * This method initializes jPanel29
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel29() {
        if (jPanel29 == null) {
            jPanel29 = new JPanel();
            jPanel29.setLayout(new FlowLayout());
        }
        return jPanel29;
    }

    /**
     * This method initializes jPanel210
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel210() {
        if (jPanel210 == null) {
            jPanel210 = new JPanel();
            jPanel210.setLayout(new FlowLayout());
        }
        return jPanel210;
    }

    /**
     * This method initializes jScrollPane1
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setViewportView(getJList1());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jScrollPane5
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane5() {
        if (jScrollPane5 == null) {
            jScrollPane5 = new JScrollPane();
            jScrollPane5.setViewportView(getJList2());
        }
        return jScrollPane5;
    }

    /**
     * This method initializes jPanel211
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel211() {
        if (jPanel211 == null) {
            jPanel211 = new JPanel();
            jPanel211.setLayout(new FlowLayout());
        }
        return jPanel211;
    }

    /**
     * This method initializes jPanel212
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel212() {
        if (jPanel212 == null) {
            jPanel212 = new JPanel();
            jPanel212.setLayout(new FlowLayout());
        }
        return jPanel212;
    }

    /**
     * This method initializes jPanel213
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel213() {
        if (jPanel213 == null) {
            jPanel213 = new JPanel();
            jPanel213.setLayout(new FlowLayout());
        }
        return jPanel213;
    }

    /**
     * This method initializes jPanel27
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel27() {
        if (jPanel27 == null) {
            jPanel27 = new JPanel();
            jPanel27.setLayout(new FlowLayout());
        }
        return jPanel27;
    }

    protected void fireFontUpdated() {

    }
}
