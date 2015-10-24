/*
 * Created on 13/8/2005
 */
package com.ramussoft.pb.frames.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.frames.components.NamedColorEditor;

/**
 * @author ZDD
 */
public class ViewOptionsDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JTabbedPane jTabbedPane = null;

    private JPanel jPanel4 = null;

    private NamedColorEditor namedColorEditor = null;

    private NamedColorEditor namedColorEditor1 = null;

    private JPanel jPanel5 = null;

    private NamedColorEditor namedColorEditor3 = null;

    private JPanel jPanel9 = null;

    private JPanel jPanel10 = null;

    private NamedColorEditor namedColorEditor4 = null;

    private NamedColorEditor namedColorEditor5 = null;

    private NamedColorEditor namedColorEditor6 = null;

    private JCheckBox jCheckBox = null;

    private ViewIDEF0PropertiosPanel viewIDFPropertiosPanel = null;

    private RunServerOptionsPanel runServerOptionsPanel = null;

    public void showModal() {
        Options.loadOptions("view_options_dialog", this);
        runServerOptionsPanel.loadOptions();
        super.setVisible(true);
        Options.saveOptions("view_options_dialog", this);
        Main.getMainFrame().repaint();
    }

    /**
     * This is the default constructor
     */
    public ViewOptionsDialog() {
        super();
        initialize();
        setLocationRelativeTo(null);
    }

    public ViewOptionsDialog(final JFrame frame) {
        super(frame);
        initialize();
        setLocationRelativeTo(frame);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setModal(true);
        setTitle("program_options");
        setMainPane(getJContentPane());
        ResourceLoader.setJComponentsText(this);
        pack();
        setMinimumSize(getSize());
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

    /**
     *
     */
    protected void saveOptions() {
        getNamedColorEditor().apply();
        getNamedColorEditor1().apply();
        getNamedColorEditor3().apply();
        getNamedColorEditor4().apply();
        getNamedColorEditor5().apply();
        getNamedColorEditor6().apply();
        Options.setBoolean("SHOW_COLORS_ON_TABLES", jCheckBox.isSelected());
        getViewIDFPropertiosPanel().saveOptions();
        runServerOptionsPanel.saveOptions();
    }

    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.addTab(ResourceLoader.getString("matrix_projection"),
                    null, getJPanel4(), null);
            jTabbedPane.addTab(ResourceLoader.getString("clasificator_table"),
                    null, getJPanel9(), null);
            jTabbedPane.addTab(
                    ResourceLoader.getString("view_idf0_properties"), null,
                    getViewIDFPropertiosPanel(), null);
            jTabbedPane.addTab(ResourceLoader.getString("server_options"),
                    null, getRunServerOptionsPanel(), null);
        }
        return jTabbedPane;
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
            jPanel4.add(getJPanel5(), java.awt.BorderLayout.NORTH);
        }
        return jPanel4;
    }

    /**
     * This method initializes namedColorEditor
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor() {
        if (namedColorEditor == null) {
            namedColorEditor = new NamedColorEditor();
            namedColorEditor.setColorName("CONNECTION_MATRIX_COLOR",
                    Color.black);
        }
        return namedColorEditor;
    }

    /**
     * This method initializes namedColorEditor1
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor1() {
        if (namedColorEditor1 == null) {
            namedColorEditor1 = new NamedColorEditor();
            namedColorEditor1.setColorName("PARENT_CONNECTION_MATRIX_COLOR",
                    Color.green);
        }
        return namedColorEditor1;
    }

    /**
     * This method initializes jPanel5
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel5() {
        if (jPanel5 == null) {
            final GridLayout gridLayout1 = new GridLayout();
            jPanel5 = new JPanel();
            jPanel5.setLayout(gridLayout1);
            gridLayout1.setRows(3);
            gridLayout1.setColumns(1);
            jPanel5.add(getNamedColorEditor(), null);
            jPanel5.add(getNamedColorEditor1(), null);
            jPanel5.add(getNamedColorEditor3(), null);
        }
        return jPanel5;
    }

    /**
     * This method initializes namedColorEditor3
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor3() {
        if (namedColorEditor3 == null) {
            namedColorEditor3 = new NamedColorEditor();
            namedColorEditor3.setColorName("MATRIX_MARK_ROW_COLOR", Color.red);
        }
        return namedColorEditor3;
    }

    /**
     * This method initializes jPanel9
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel9() {
        if (jPanel9 == null) {
            jPanel9 = new JPanel();
            jPanel9.setLayout(new BorderLayout());
            jPanel9.add(getJPanel10(), java.awt.BorderLayout.NORTH);
        }
        return jPanel9;
    }

    /**
     * This method initializes jPanel10
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel10() {
        if (jPanel10 == null) {
            final GridLayout gridLayout5 = new GridLayout();
            jPanel10 = new JPanel();
            jPanel10.setLayout(gridLayout5);
            gridLayout5.setRows(4);
            jPanel10.add(getNamedColorEditor4(), null);
            jPanel10.add(getNamedColorEditor5(), null);
            jPanel10.add(getNamedColorEditor6(), null);
            jPanel10.add(getJCheckBox(), null);
        }
        return jPanel10;
    }

    /**
     * This method initializes namedColorEditor4
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor4() {
        if (namedColorEditor4 == null) {
            namedColorEditor4 = new NamedColorEditor();
            namedColorEditor4.setColorName("ClasificatorTable.textColor",
                    Color.black);
        }
        return namedColorEditor4;
    }

    /**
     * This method initializes namedColorEditor5
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor5() {
        if (namedColorEditor5 == null) {
            namedColorEditor5 = new NamedColorEditor();
            namedColorEditor5.setColorName("ClasificatorTable.bgColor1",
                    Color.white);
        }
        return namedColorEditor5;
    }

    /**
     * This method initializes namedColorEditor6
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor6() {
        if (namedColorEditor6 == null) {
            namedColorEditor6 = new NamedColorEditor();
            namedColorEditor6.setColorName("ClasificatorTable.bgColor2",
                    new Color(220, 220, 220));
        }
        return namedColorEditor6;
    }

    /**
     * This method initializes jCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox() {
        if (jCheckBox == null) {
            jCheckBox = new JCheckBox();
            jCheckBox.setText("show_colors_on_grid");
            jCheckBox.setSelected(Options.getBoolean("SHOW_COLORS_ON_TABLES",
                    false));
        }
        return jCheckBox;
    }

    /**
     * This method initializes viewIDFPropertiosPanel
     *
     * @return com.jason.clasificators.frames.setup.ViewIDFPropertiosPanel
     */
    private ViewIDEF0PropertiosPanel getViewIDFPropertiosPanel() {
        if (viewIDFPropertiosPanel == null) {
            viewIDFPropertiosPanel = new ViewIDEF0PropertiosPanel();
        }
        return viewIDFPropertiosPanel;
    }

    /**
     * This method initializes runServerOptionsPanel
     *
     * @return com.jason.clasificators.frames.setup.RunServerOptionsPanel
     */
    private RunServerOptionsPanel getRunServerOptionsPanel() {
        if (runServerOptionsPanel == null) {
            runServerOptionsPanel = new RunServerOptionsPanel();
        }
        return runServerOptionsPanel;
    }

    @Override
    protected void onOk() {
        saveOptions();
        super.onOk();
    }

} // @jve:decl-index=0:visual-constraint="35,20"
