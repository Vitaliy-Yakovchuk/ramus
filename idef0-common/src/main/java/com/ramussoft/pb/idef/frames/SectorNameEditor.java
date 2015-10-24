package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.dfds.visual.DFDSRole;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;

public abstract class SectorNameEditor extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JPanel jPanel3 = null;

    private JLabel jLabel = null;

    JTextField jTextField = null;

    private JButton jButton = null;

    private Stream stream = null;

    private AlternativeArrowDialog arrowRowsDialog = null;

    private JDialog dialog = null;

    private JPanel jPanel4 = null;

    private DataPlugin dataPlugin;

    private GUIFramework framework;

    private JPanel jPanel6 = null;

    private JLabel jLabel1 = null;

    private JPanel jPanel123 = null;

    private JTextField alternativeTextField;

    public JCheckBox box;

    private boolean changed;

    private JRadioButton all;

    private JRadioButton children;

    private JRadioButton safe;

    private JComboBox aligments;

    public String getArrowName() {
        return jTextField.getText();
    }

    private AlternativeArrowDialog getArrowRowsDialog() {
        if (arrowRowsDialog == null) {
            if (dialog == null)
                arrowRowsDialog = new AlternativeArrowDialog(dataPlugin,
                        framework);
            else
                arrowRowsDialog = new AlternativeArrowDialog(dialog,
                        dataPlugin, framework);
        }
        return arrowRowsDialog;
    }

    public void setSector(Sector sector) {
        setStream(sector.getStream());
        if (sector.getStream() == null) {
            jTextField.setText("");
        } else {
            if (sector.getStream().isEmptyName())
                jTextField.setText("");
            else
                jTextField.setText(sector.getStream().getName());
        }
        box.setSelected(sector.isShowText());
        alternativeTextField.setText(sector.getAlternativeText());
        alternativeTextField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        setChanged(true);
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        setChanged(true);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        setChanged(true);
                    }
                });

        box.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setChanged(true);
            }
        });
        aligments.setSelectedIndex(sector.getTextAligment());
        repaint();
        changed = false;
    }

    public void setDFDSRole(DFDSRole role) {
        setStream(role.getStream());
        if (role.getStream() == null) {
            jTextField.setText("");
        } else {
            if (role.getStream().isEmptyName())
                jTextField.setText("");
            else
                jTextField.setText(role.getStream().getName());
        }
        alternativeTextField.setText(role.getAlternativeText());
        alternativeTextField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        setChanged(true);
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        setChanged(true);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        setChanged(true);
                    }
                });

        box.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setChanged(true);
            }
        });

        repaint();
        changed = false;
    }

    /**
     * This is the default constructor
     */
    public SectorNameEditor(DataPlugin dataPlugin, GUIFramework framework,
                            AccessRules accessRules) {
        super();
        this.dataPlugin = dataPlugin;
        this.framework = framework;
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.setSize(new Dimension(441, 231));
        JPanel jPanel = new JPanel(new BorderLayout());
        this.add(jPanel, java.awt.BorderLayout.NORTH);
        jPanel.add(getJPanel(), java.awt.BorderLayout.NORTH);
        createReplacementPanel(jPanel);
        jPanel.add(getJPanel4(), java.awt.BorderLayout.SOUTH);
    }

    protected void createReplacementPanel(JPanel panel) {
        panel.add(getReplacemetPanel(), java.awt.BorderLayout.CENTER);
        JPanel aligmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aligmentPanel.add(new JLabel(ResourceLoader.getString("Aligment")));
        aligments = new JComboBox();
        aligments.addItem(ResourceLoader.getString("Aligment.Left"));
        aligments.addItem(ResourceLoader.getString("Aligment.Center"));
        aligments.addItem(ResourceLoader.getString("Aligment.Right"));
        aligmentPanel.add(aligments);
        this.add(aligmentPanel, java.awt.BorderLayout.CENTER);
    }

    public void setDialog(final JDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jPanel.add(getJPanel1(), java.awt.BorderLayout.NORTH);
            jPanel.add(getJPanel2(), java.awt.BorderLayout.CENTER);
        }
        return jPanel;
    }

    private Component getReplacemetPanel() {
        JPanel group = new JPanel(new GridLayout(0, 1));
        group.setBorder(BorderFactory.createTitledBorder(ResourceLoader
                .getString("ArrowReplacementType.name")));
        group.add(safe = new JRadioButton("ArrowReplacementType.safe"));
        group.add(children = new JRadioButton("ArrowReplacementType.branching"));
        group.add(all = new JRadioButton("ArrowReplacementType.everywhere"));
        safe.setSelected(true);

        ButtonGroup g = new ButtonGroup();
        g.add(children);
        g.add(all);
        g.add(safe);

        setReplaceEnable(false);

        return group;
    }

    public void setReplaceEnable(boolean b) {
        if (all == null)
            return;
        all.setEnabled(b);
        children.setEnabled(b);
        safe.setEnabled(b);
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jLabel = new JLabel();
            jLabel.setText("arrow_name:");
            jPanel1 = new JPanel();
            jPanel1.setLayout(new BorderLayout());
            jPanel1.add(getJPanel3(), java.awt.BorderLayout.NORTH);
            jPanel1.add(jLabel, java.awt.BorderLayout.CENTER);
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
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.weightx = 1.0;
            final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 1;
            gridBagConstraints3.gridy = 1;
            jPanel2 = new JPanel();
            jPanel2.setLayout(new GridBagLayout());
            jPanel2.add(getJButton(), gridBagConstraints3);
            jPanel2.add(getJTextField(), gridBagConstraints1);
            jPanel2.add(getJPanel5(), gridBagConstraints);
        }
        return jPanel2;
    }

    /**
     * This method initializes jPanel3
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            jPanel3 = new JPanel();
        }
        return jPanel3;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
            jTextField.setName("jTextField");
            jTextField.getDocument().addDocumentListener(
                    new DocumentListener() {

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            ch();
                        }

                        private void ch() {
                            setChanged(true);
                            String oldName = "";
                            if ((lowstream != null)
                                    && (!lowstream.isEmptyName()))
                                oldName = lowstream.getName();

                            boolean c;
                            if (all == null)
                                c = false;
                            else
                                c = all.isEnabled();
                            String text = jTextField.getText();
                            if (text == null)
                                text = "";
                            boolean s = text.length() > 0
                                    && !text.equals(oldName);
                            if (s != c)
                                setReplaceEnable(s);

                        }

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            ch();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            ch();
                        }
                    });
        }
        return jTextField;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("...");
            jButton.setPreferredSize(new Dimension(40, 20));
            jButton.setToolTipText("alternative_stream");
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    selectRows();
                    setChanged(true);
                }
            });
        }
        return jButton;
    }

    protected boolean selectRows() {
        final Stream tmp = getArrowRowsDialog().showModal();
        if (tmp == getStream() || tmp == null)
            return false;
        Stream ls = lowstream;
        setStream(tmp);
        lowstream = ls;
        jTextField.setText(tmp.getName());
        return true;
    }

    public abstract Stream getStream();

    public Stream findStreamByName() {
        return findStreamByName(jTextField.getText());
    }

    public Stream findStreamByName(final String name) {
        if ("".equals(name))
            return null;
        final Vector<Row> streams = dataPlugin.getRecChilds(
                dataPlugin.getBaseStream(), true);
        final int l = streams.size();
        for (int i = 0; i < l; i++) {
            final Stream s = (Stream) streams.get(i);
            if (name.equals(s.getName()) && !s.isEmptyName())
                return s;
        }
        return null;
    }

    public Stream getNullStream() {
        return stream;
    }

    private JPanel jPanel5 = null;

    private Stream lowstream;

    /**
     * This method initializes jPanel4
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            jPanel4 = new JPanel();
            jPanel4.setLayout(new BorderLayout());
            jPanel4.add(getJPanel6(), java.awt.BorderLayout.SOUTH);
        }
        return jPanel4;
    }

    public void beforeShow() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jTextField.requestFocus();
            }
        });
        setReplaceEnable(false);
        safe.setSelected(true);
    }

    public ReplaceStreamType getReplaceStreamType() {
        ReplaceStreamType type = null;
        if (all.isSelected())
            type = ReplaceStreamType.ALL;
        else if (children.isSelected())
            type = ReplaceStreamType.CHILDREN;
        else if (safe.isSelected())
            type = ReplaceStreamType.SIMPLE;
        return type;
    }

    /**
     * This method initializes jPanel5
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel5() {
        if (jPanel5 == null) {
            jPanel5 = new JPanel();
            jPanel5.setLayout(new FlowLayout());
        }
        return jPanel5;
    }

    private JPanel getJPanel6() {
        if (jPanel6 == null) {
            final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridwidth = 4;
            gridBagConstraints1.anchor = GridBagConstraints.WEST;
            gridBagConstraints1.gridy = 3;
            final GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 2;
            gridBagConstraints7.gridy = 5;
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridx = 2;
            final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 2;
            gridBagConstraints3.gridy = 0;
            gridBagConstraints3.gridwidth = 1;
            gridBagConstraints3.anchor = GridBagConstraints.WEST;
            jLabel1 = new JLabel();
            jLabel1.setText("SectorLabel");
            jLabel1.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
            jPanel6 = new JPanel();
            jPanel6.setLayout(new GridBagLayout());
            jPanel6.add(jLabel1, gridBagConstraints3);
            jPanel6.add(getAlternativeTextField(), gridBagConstraints);
            jPanel6.add(getJPanel123(), gridBagConstraints7);
            jPanel6.add(getBox(), gridBagConstraints1);
        }
        return jPanel6;
    }

    /**
     * This method initializes jPanel123
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel123() {
        if (jPanel123 == null) {
            jPanel123 = new JPanel();
            jPanel123.setLayout(new FlowLayout());
        }
        return jPanel123;
    }

    public JTextField getAlternativeTextField() {
        if (alternativeTextField == null) {
            alternativeTextField = new TextField();
        }
        return alternativeTextField;
    }

    public JCheckBox getBox() {
        if (box == null) {
            box = new JCheckBox();
            box.setText("ShowText");
            box.setMnemonic(KeyEvent.VK_UNDEFINED);
            box.setHorizontalAlignment(SwingConstants.LEADING);
            box.setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        return box;
    }

    public void setStream(final Stream stream) {
        this.stream = stream;
        this.lowstream = stream;
    }

    /**
     * @param changed the changed to set
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    /**
     * @return the changed
     */
    public boolean isChanged() {
        return changed;
    }

    public int getTextAligment() {
        int i = aligments.getSelectedIndex();
        if (i < 0)
            i = 0;
        return i;
    }

}
