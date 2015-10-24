/*
 * Created on 13/8/2005
 */
package com.ramussoft.pb.idef.frames;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.idef.elements.ReadedModel;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.IDEF0TabView;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

/**
 * @author ZDD
 */
public class ProjectOptionsPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JTabbedPane jTabbedPane = null;

    private JPanel jPanel3 = null;

    private JPanel jPanel4 = null;

    private JPanel jPanel5 = null;

    private StatusPanel statusPanel = null;

    private JScrollPane jScrollPane = null;

    private JTable jTable = null;

    private JLabel jLabel = null;

    private JTextField jTextField = null;

    private JLabel jLabel1 = null;

    private JTextField jTextField1 = null;

    private JLabel jLabel2 = null;

    private JScrollPane jScrollPane1 = null;

    private JTextArea jTextArea = null;

    private JPanel jPanel6 = null;

    private JPanel jPanel7 = null;

    private JButton jButton2 = null;

    private JButton jButton3 = null;

    private ReadedModel model = null;

    private JLabel jLabel3 = null;

    private JLabel jLabelLetterModel = null;

    private JTextField jTextField2 = null;

    private JTextField letterField = null;

    private Function function;

    private final ProjectOptions project;

    private AbstractTableModel dataModel;

    private JComboBox sizesBox = new JComboBox();

    private JComboBox horizontalPageCountBox = new JComboBox();

    private final JDialog dialog;

    private DataPlugin dataPlugin;

    private void addRow() {
        model.addReaded();
        dataModel.fireTableRowsInserted(model.getRowCount() - 1,
                model.getRowCount() - 1);
    }

    private void removeRow() {
        final int sel = getJTable().getSelectedRow();
        if (sel >= 0) {
            model.removeReaded(sel);
            dataModel.fireTableRowsDeleted(sel, sel);
        }
    }

    /**
     * This is the default constructor
     */
    public ProjectOptionsPanel(Function function, DataPlugin dataPlugin,
                               JDialog dialog) {
        super(new BorderLayout());
        this.dialog = dialog;
        this.dataPlugin = dataPlugin;
        project = function.getProjectOptions();
        this.function = function;
        this.add(getJContentPane(), BorderLayout.CENTER);
        ResourceLoader.setJComponentsText(this);
        getJTextField().setText(project.getProjectName());
        getJTextField1().setText(project.getProjectAutor());
        getJTextField2().setText(project.getUsedAt());
        getJTextArea().setText(project.getDefinition());
        getStatusPanel().setStatus(function.getStatus());
        letterField.setText(project.getDeligate().getModelLetter());
        model = project.getReadedModel();
        dataModel = new AbstractTableModel() {

            @Override
            public int getColumnCount() {
                return model.getColumnCount();
            }

            @Override
            public int getRowCount() {
                return model.getRowCount();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return model.getValueAt(rowIndex, columnIndex);
            }

            @Override
            public String getColumnName(int column) {
                return model.getColumnName(column);
            }

            @Override
            public void setValueAt(Object value, int rowIndex, int columnIndex) {
                model.setValueAt(value, rowIndex, columnIndex);
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
        };
        getJTable().setModel(dataModel);

        String size = project.getDeligate().getDiagramSize();
        if (size != null) {
            int i = size.indexOf('x');
            if (i < 0)
                sizesBox.setSelectedItem(size);
            else {
                horizontalPageCountBox.setSelectedItem(size.substring(i + 1));
                sizesBox.setSelectedItem(size.substring(0, i));
            }
        }

        setVisible(true);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
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
            jPanel.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
        }
        return jPanel;
    }

    public void save(final GUIFramework framework) {
        project.setDefinition(getJTextArea().getText());
        project.setProjectAutor(getJTextField1().getText());
        project.setProjectName(getJTextField().getText());
        project.getDeligate().setModelLetter(letterField.getText());
        function.setStatus(statusPanel.getStatus());
        project.setUsedAt(getJTextField2().getText());
        String newSize = String.valueOf(sizesBox.getSelectedItem());
        String oldSize = project.getDeligate().getDiagramSize();

        if (horizontalPageCountBox.getSelectedIndex() != 0)
            newSize += "x" + horizontalPageCountBox.getSelectedItem();

        double percent = 1d;

        if ("A3".equals(newSize) && (oldSize == null || "A4".equals(oldSize)))
            percent = 2d;
        else if ("A4".equals(newSize) && ("A3".equals(oldSize)))
            percent = 0.5d;

        project.getDeligate().setDiagramSize(newSize);

        if (!equals(oldSize, newSize))
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    framework.propertyChanged(IDEF0TabView.UPDATE_SIZES);
                }
            });

        function.setProjectOptions(project);

        if (percent != 1.0d) {
            SizeChangedDialog changedDialog = new SizeChangedDialog(dialog);
            changedDialog.setLocationRelativeTo(null);
            changedDialog.pack();
            changedDialog.setMinimumSize(changedDialog.getSize());
            com.ramussoft.gui.common.prefrence.Options
                    .loadOptions(changedDialog);
            changedDialog.setVisible(true);
            com.ramussoft.gui.common.prefrence.Options
                    .saveOptions(changedDialog);
            boolean updateFonts = changedDialog.isUpdateFonts();
            boolean updateZoom = changedDialog.isUpdateZoom();

            update(dataPlugin.getBaseFunction(), updateFonts, updateZoom,
                    percent, framework);
        }
    }

    private boolean equals(String oldSize, String newSize) {
        if (oldSize == null)
            return newSize == null;
        if (newSize == null)
            return false;
        return oldSize.equals(newSize);
    }

    private void update(Function function, boolean updateFonts,
                        boolean updateZoom, double percent, GUIFramework framework) {
        if (!updateFonts && !updateZoom)
            return;

        long id = -1l;
        if (!dataPlugin.getBaseFunction().equals(function)) {
            id = function.getElement().getId();
        }

        framework.propertyChanged(IDEF0TabView.CLOSE, id);

        SectorRefactor sectorRefactor = new SectorRefactor(new MovingArea(
                dataPlugin, function));
        sectorRefactor.updatePageSize(updateFonts, updateZoom, percent,
                function);
        Vector<Row> v = dataPlugin.getChilds(function, true);
        for (Row row : v) {
            Function function2 = (Function) row;
            update(function2, updateFonts, updateZoom, percent, framework);
        }
    }

    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = getTabPanel();
            jTabbedPane.insertTab(ResourceLoader.getString("general"), null,
                    getJPanel3(), null, 0);
            jTabbedPane.insertTab(ResourceLoader.getString("status"), null,
                    getJPanel4(), null, 1);
            jTabbedPane.insertTab(ResourceLoader.getString("readers"), null,
                    getJPanel5(), null, 2);

        }
        return jTabbedPane;
    }

    protected JTabbedPane getTabPanel() {
        return new JTabbedPane();
    }

    /**
     * This method initializes jPanel3
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            double[][] size = {
                    {5, TableLayout.FILL, 5},
                    {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                            TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                            TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                            TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                            TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                            TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                            TableLayout.MINIMUM, 5, TableLayout.FILL, 5}};
            jLabel3 = new JLabel();
            jLabel2 = new JLabel();
            jLabel1 = new JLabel();
            jLabel = new JLabel();
            jPanel3 = new JPanel(new TableLayout(size));
            jLabel.setText("project_name:");
            jLabel1.setText("autor:");
            jLabel2.setText("definition:");
            jLabel3.setText("used_at:");
            jLabelLetterModel = new JLabel();
            jLabelLetterModel.setText("letter_model:");
            jPanel3.add(jLabel, "1,1");
            jPanel3.add(getJTextField(), "1,3");
            jPanel3.add(jLabel1, "1,5");
            jPanel3.add(getJTextField1(), "1,7");
            jPanel3.add(jLabel3, "1,9");
            jPanel3.add(getJTextField2(), "1,11");

            jPanel3.add(new JLabel("page_size:"), "1,13");
            jPanel3.add(sizesBox, "1,15");

            jPanel3.add(new JLabel("horizontal_page_count:"), "1,17");
            jPanel3.add(horizontalPageCountBox, "1,19");

            sizesBox.addItem("A4");
            sizesBox.addItem("A3");

            horizontalPageCountBox.addItem("1");
            horizontalPageCountBox.addItem("2");
            horizontalPageCountBox.addItem("3");
            horizontalPageCountBox.addItem("4");
            horizontalPageCountBox.addItem("5");
            horizontalPageCountBox.addItem("6");
            horizontalPageCountBox.addItem("7");
            horizontalPageCountBox.addItem("8");
            horizontalPageCountBox.addItem("9");
            horizontalPageCountBox.addItem("10");
            // sizesBox.addItem("A3x2");

            jPanel3.add(jLabelLetterModel, "1,21");
            letterField = new JTextField();

            jPanel3.add(letterField, "1,23");
            jPanel3.add(jLabel2, "1,25");
            jPanel3.add(getJScrollPane1(), "1,27");
        }
        return jPanel3;
    }

    /**
     * This method initializes jPanel4
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            jPanel4 = new JPanel();
            jPanel4.setLayout(new BorderLayout());
            jPanel4.add(getStatusPanel(), java.awt.BorderLayout.NORTH);
        }
        return jPanel4;
    }

    /**
     * This method initializes jPanel5
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel5() {
        if (jPanel5 == null) {
            jPanel5 = new JPanel();
            jPanel5.setLayout(new BorderLayout());
            jPanel5.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
            jPanel5.add(getJPanel6(), java.awt.BorderLayout.EAST);
        }
        return jPanel5;
    }

    /**
     * This method initializes statusPanel
     *
     * @return com.jason.clasificators.frames.idf.StatusPanel
     */
    private StatusPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new StatusPanel();
        }
        return statusPanel;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTable
     *
     * @return javax.swing.JTable
     */
    private JTable getJTable() {
        if (jTable == null) {
            jTable = new JTable();
        }
        return jTable;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
            jTextField.setBounds(24, 54, 379, 20);
        }
        return jTextField;
    }

    /**
     * This method initializes jTextField1
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField1() {
        if (jTextField1 == null) {
            jTextField1 = new JTextField();
            jTextField1.setBounds(24, 118, 379, 20);
        }
        return jTextField1;
    }

    /**
     * This method initializes jScrollPane1
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane1() {
        if (jScrollPane1 == null) {
            jScrollPane1 = new JScrollPane(getJTextArea());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes jTextArea
     *
     * @return javax.swing.JTextArea
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
            jTextArea.setWrapStyleWord(true);
            jTextArea.setLineWrap(true);
        }
        return jTextArea;
    }

    /**
     * This method initializes jPanel6
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel6() {
        if (jPanel6 == null) {
            final FlowLayout flowLayout4 = new FlowLayout();
            jPanel6 = new JPanel();
            jPanel6.setLayout(flowLayout4);
            flowLayout4.setAlignment(java.awt.FlowLayout.CENTER);
            jPanel6.add(getJPanel7(), null);
        }
        return jPanel6;
    }

    /**
     * This method initializes jPanel7
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel7() {
        if (jPanel7 == null) {
            final GridLayout gridLayout3 = new GridLayout();
            jPanel7 = new JPanel();
            jPanel7.setLayout(gridLayout3);
            gridLayout3.setRows(2);
            gridLayout3.setHgap(5);
            gridLayout3.setVgap(5);
            jPanel7.add(getJButton2(), null);
            jPanel7.add(getJButton3(), null);
        }
        return jPanel7;
    }

    /**
     * This method initializes jButton2
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setText("add");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    addRow();
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
            jButton3.setText("remove");
            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    removeRow();
                }
            });
        }
        return jButton3;
    }

    /**
     * This method initializes jTextField2
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField2() {
        if (jTextField2 == null) {
            jTextField2 = new JTextField();
            jTextField2.setBounds(24, 180, 379, 20);
        }
        return jTextField2;
    }

    public ProjectOptions getProject() {
        return project;
    }
} // @jve:decl-index=0:visual-constraint="10,10"
