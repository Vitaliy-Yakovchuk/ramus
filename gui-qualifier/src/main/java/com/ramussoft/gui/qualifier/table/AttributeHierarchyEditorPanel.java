package com.ramussoft.gui.qualifier.table;

import info.clearthought.layout.TableLayout;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JLabel;

import javax.swing.JScrollPane;

import com.ramussoft.common.Attribute;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class AttributeHierarchyEditorPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JPanel jPanel1 = null;
    private JButton jButton = null;
    private JButton jButton1 = null;
    private JPanel jPanel2 = null;
    private JPanel jPanel21 = null;
    private JButton jButton2 = null;
    private JButton jButton3 = null;
    private JPanel jPanel22 = null;
    private JList jList = null;
    private JList jList1 = null;
    private JLabel jLabel = null;
    private JLabel jLabel1 = null;
    private JPanel jPanel = null;
    private JButton jButton4 = null;
    private JPanel jPanel221 = null;
    private JButton jButton5 = null;
    private DefaultListModel leftModel;
    private DefaultListModel rightModel;
    private Attribute[] allAttributes;
    private JScrollPane jScrollPane = null;
    private JScrollPane jScrollPane1 = null;
    private GUIFramework framework;

    /**
     * This is the default constructor
     */
    public AttributeHierarchyEditorPanel(GUIFramework framework) {
        super();
        this.framework = framework;
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        double[][] size = {
                {5, TableLayout.FILL, 5, TableLayout.MINIMUM, 5,
                        TableLayout.FILL, 5, TableLayout.MINIMUM, 5},
                {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5}};
        setLayout(new TableLayout(size));
        jLabel1 = new JLabel();
        jLabel1.setText(GlobalResourcesManager
                .getString("HierarchicalAttributes"));
        jLabel = new JLabel();
        jLabel.setText(GlobalResourcesManager.getString("PresentAttributes"));
        this.add(jLabel, "1,1");
        this.add(jLabel1, "5, 1");
        this.add(getJScrollPane1(), "1,3");
        this.add(getJPanel1(), "3,3");
        this.add(getJScrollPane(), "5,3");
        this.add(getJPanel(), "7,3");
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            final GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 1;
            gridBagConstraints21.gridy = 6;
            final GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.gridx = 1;
            gridBagConstraints16.gridy = 2;
            final GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 1;
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridy = -1;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridx = -1;
            final GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 1;
            gridBagConstraints7.gridy = 3;
            final GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 1;
            gridBagConstraints6.gridy = 4;
            final GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 1;
            gridBagConstraints4.gridy = 1;
            final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 1;
            gridBagConstraints3.gridy = 5;
            jPanel1 = new JPanel();
            jPanel1.setLayout(new GridBagLayout());
            jPanel1.add(getJButton(), gridBagConstraints9);
            jPanel1.add(getJPanel2(), gridBagConstraints3);
            jPanel1.add(getJPanel21(), gridBagConstraints4);
            jPanel1.add(getJButton3(), gridBagConstraints6);
            jPanel1.add(getJButton1(), gridBagConstraints16);
            jPanel1.add(getJPanel22(), gridBagConstraints7);
            jPanel1.add(getJButton2(), gridBagConstraints21);
        }
        return jPanel1;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setMnemonic(KeyEvent.VK_UNDEFINED);
            jButton.setIcon(new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-right.png")));
            jButton.setToolTipText(GlobalResourcesManager
                    .getString("MoveAttributeRight"));
            jButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    moveRight();
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setIcon(new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-left.png")));
            jButton1.setToolTipText(GlobalResourcesManager
                    .getString("MoveAttributeLeft"));
            jButton1.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    moveLeft();
                }
            });

        }
        return jButton1;
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
            jPanel21 = new JPanel();
            jPanel21.setLayout(new FlowLayout());
        }
        return jPanel21;
    }

    /**
     * This method initializes jButton2
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setMnemonic(KeyEvent.VK_UNDEFINED);
            jButton2.setIcon(new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-all-left.png")));
            jButton2.setToolTipText(GlobalResourcesManager
                    .getString("MoveAllAttributesLeft"));
            jButton2.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    moveAllLeft();
                }
            });

        }
        return jButton2;
    }

    /**
     * This method initializes jButton3
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton();
            jButton3.setIcon(new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-all-right.png")));
            jButton3.setToolTipText(GlobalResourcesManager
                    .getString("MoveAllAttributesRight"));
            jButton3.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    moveAllRight();
                }
            });

        }
        return jButton3;
    }

    /**
     * This method initializes jPanel22
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel22() {
        if (jPanel22 == null) {
            jPanel22 = new JPanel();
            jPanel22.setLayout(new FlowLayout());
        }
        return jPanel22;
    }

    /**
     * This method initializes jList
     *
     * @return javax.swing.JList
     */
    private JList getLeftList() {
        if (jList == null) {
            jList = new JList();
        }
        return jList;
    }

    /**
     * This method initializes jList1
     *
     * @return javax.swing.JList
     */
    private JList getRightList() {
        if (jList1 == null) {
            jList1 = new JList();
        }
        return jList1;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            final GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
            gridBagConstraints17.gridx = 0;
            gridBagConstraints17.gridy = 2;
            final GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.gridy = 1;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.add(getJButton5(), new GridBagConstraints());
            jPanel.add(getJPanel221(), gridBagConstraints15);
            jPanel.add(getJButton4(), gridBagConstraints17);
        }
        return jPanel;
    }

    /**
     * This method initializes jButton4
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton();
            jButton4.setIcon(new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-down.png")));
            jButton4.setToolTipText(GlobalResourcesManager
                    .getString("MoveAttributeDown"));
            jButton4.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    moveDown();
                }

            });
        }
        return jButton4;
    }

    /**
     * This method initializes jPanel221
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel221() {
        if (jPanel221 == null) {
            jPanel221 = new JPanel();
            jPanel221.setLayout(new FlowLayout());
        }
        return jPanel221;
    }

    /**
     * This method initializes jButton5
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton5() {
        if (jButton5 == null) {
            jButton5 = new JButton();
            jButton5.setIcon(new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/move-up.png")));
            jButton5.setToolTipText(GlobalResourcesManager
                    .getString("MoveAttributeUp"));
            jButton5.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    moveUp();
                }

            });
        }
        return jButton5;
    }

    private void moveLeft(final int index) {
        final Object o = rightModel.get(index);
        rightModel.removeElementAt(index);
        int j = -1;
        for (final Attribute element : allAttributes) {
            if (o == element)
                break;
            if (leftModel.size() <= j + 1)
                break;
            if (leftModel.get(j + 1) == element) {
                j++;
            }
        }
        j++;
        leftModel.add(j, o);
    }

    private void moveRight(final int index) {
        final Object o = leftModel.get(index);
        leftModel.removeElementAt(index);
        rightModel.addElement(o);
    }

    protected void moveRight() {
        final int[] sels = getLeftList().getSelectedIndices();
        for (int i = sels.length - 1; i >= 0; i--)
            moveRight(sels[i]);
    }

    protected void moveLeft() {
        final int[] sels = getRightList().getSelectedIndices();
        for (int i = sels.length - 1; i >= 0; i--)
            moveLeft(sels[i]);
    }

    protected void moveAllRight() {
        while (leftModel.size() > 0)
            moveRight(0);
    }

    protected void moveAllLeft() {
        while (rightModel.size() > 0)
            moveLeft(0);
    }

    protected void moveDown() {
        int[] is = getRightList().getSelectedIndices();
        final ArrayList<Integer> nSel = new ArrayList<Integer>();
        for (int i = 0; i < is.length; i++) {
            final int index = is[i];
            if (index + 1 < rightModel.size()) {
                final Object obj = rightModel.get(index);
                rightModel.remove(index);
                final int j = index + 1;
                rightModel.add(j, obj);
                nSel.add(j);
            } else
                is[i] = -1;
        }
        is = new int[nSel.size()];
        for (int i = 0; i < is.length; i++)
            is[i] = nSel.get(i);
        getRightList().setSelectedIndices(is);
    }

    protected void moveUp() {
        int[] is = getRightList().getSelectedIndices();
        final ArrayList<Integer> nSel = new ArrayList<Integer>();
        for (int i = 0; i < is.length; i++) {
            final int index = is[i];
            if (index > 0) {
                final Object obj = rightModel.get(index);
                rightModel.remove(index);
                final int j = index - 1;
                rightModel.add(j, obj);
                nSel.add(j);
            } else
                is[i] = -1;
        }
        is = new int[nSel.size()];
        for (int i = 0; i < is.length; i++)
            is[i] = nSel.get(i);
        getRightList().setSelectedIndices(is);
    }

    private boolean isIn(final Attribute[] attrs, final Attribute attr) {
        for (final Attribute a : attrs)
            if (a == attr)
                return true;
        return false;
    }

    public void setAttributes(final Attribute[] allAttributes,
                              final Attribute[] right) {
        this.allAttributes = allAttributes;
        leftModel = new DefaultListModel() {

            /**
             *
             */
            private static final long serialVersionUID = -8736331096637254036L;

            @Override
            public Object getElementAt(int index) {
                Attribute attribute = (Attribute) get(index);
                String name = framework.getSystemAttributeName(attribute);
                if (name == null)
                    name = attribute.getName();
                return name;
            }
        };
        rightModel = new DefaultListModel() {

            /**
             *
             */
            private static final long serialVersionUID = -4034830803171565715L;

            @Override
            public Object getElementAt(int index) {
                Attribute attribute = (Attribute) get(index);
                String name = framework.getSystemAttributeName(attribute);
                if (name == null)
                    name = attribute.getName();
                return name;
            }
        };
        for (final Attribute a : allAttributes)
            if (!isIn(right, a))
                leftModel.addElement(a);
        for (final Attribute a : right)
            rightModel.addElement(a);
        getLeftList().setModel(leftModel);
        getRightList().setModel(rightModel);
    }

    public Attribute[] getAttributes() {
        final Attribute[] res = new Attribute[rightModel.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = (Attribute) rightModel.get(i);
        return res;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getRightList());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jScrollPane1
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane();
            jScrollPane1.setViewportView(getLeftList());
        }
        return jScrollPane1;
    }

} // @jve:decl-index=0:visual-constraint="24,16"
