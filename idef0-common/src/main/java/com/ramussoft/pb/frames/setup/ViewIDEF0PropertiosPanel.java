/*
 * Created on 29/10/2004
 */
package com.ramussoft.pb.frames.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.frames.components.NamedColorEditor;
import com.ramussoft.pb.frames.components.NamedFontEditor;
import com.ramussoft.pb.idef.visual.MovingArea;

/**
 * @author ZDD
 */
public class ViewIDEF0PropertiosPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private NamedColorEditor namedColorEditor = null;

    private JPanel jPanel2 = null;

    private NamedFontEditor namedFontEditor = null;

    private NamedFontEditor namedFontEditor11 = null;

    private NamedFontEditor namedFontEditor12 = null;

    private NamedFontEditor dataStoreNamedFont = null;

    private NamedFontEditor externalReferenceNamedFont = null;

    private NamedColorEditor namedColorEditor1 = null;

    private NamedColorEditor namedColorEditor2 = null;

    private NamedColorEditor namedColorEditor3 = null;

    private NamedColorEditor namedColorEditor4 = null;

    private NamedColorEditor namedColorExt = null;

    private NamedColorEditor namedColorTextExt = null;

    private NamedColorEditor namedColorDS = null;

    private NamedColorEditor namedColorTextDS = null;

    private JComboBox actionType = new JComboBox();

    private JCheckBox enableRenderingHints = new JCheckBox();

    private NamedFontEditor dfdsRoleNamedFont;

    private NamedColorEditor dfdsRoleNamedColorEditor;

    private NamedColorEditor dfdsRoleNamedTextColorEditor;

    private JSpinner secondPartMinus = new JSpinner(new SpinnerNumberModel(
            Options.getInteger("SECOND_PART_MINUS", 2), 1, 20, 1));

    /**
     *
     */
    public void showModal() {
        Options.loadOptions("view_i_properties", this);
        setVisible(true);
        Options.saveOptions("view_i_properties", this);
    }

    /**
     * This is the default constructor
     */
    public ViewIDEF0PropertiosPanel() {
        super();
        initialize();
        ResourceLoader.setJComponentsText(this);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {

        namedColorExt = new NamedColorEditor();

        namedColorTextExt = new NamedColorEditor();

        namedColorDS = new NamedColorEditor();
        namedColorDS.setColorName("DEFAULT_DATA_STORE_COLOR", Color.white);

        namedColorTextDS = new NamedColorEditor();
        namedColorTextDS.setColorName("DEFAULT_DATA_STORE_TEXT_COLOR",
                Color.black);

        namedColorTextExt.setColorName("DEFAULT_EXTERNAL_TEXT_COLOR",
                Color.black);
        namedColorExt.setColorName("DEFAULT_EXTERNAL_COLOR", Color.white);
        enableRenderingHints.setSelected(!Options.getBoolean(
                "DISABLE_RENDERING_HINTS", false));
        enableRenderingHints.setText("EnableRenderingHints");

        setLayout(new BorderLayout());
        this.add(getJContentPane(), BorderLayout.CENTER);
        actionType.addItem(ResourceLoader.getString("EDIT"));
        actionType.addItem(ResourceLoader.getString("RENAME"));
        if (Options.getString("ON_DOUBLE_CLICK", "EDIT").equals("EDIT"))
            actionType.setSelectedIndex(0);
        else
            actionType.setSelectedIndex(1);
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
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getJPanel2(), java.awt.BorderLayout.CENTER);
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
            final FlowLayout flowLayout2 = new FlowLayout();
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout2);
            flowLayout2.setAlignment(java.awt.FlowLayout.RIGHT);
            jPanel.add(getJPanel1(), null);
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
            final GridLayout gridLayout1 = new GridLayout();
            jPanel1 = new JPanel();
            jPanel1.setLayout(gridLayout1);
            gridLayout1.setRows(1);
            gridLayout1.setHgap(5);
        }
        return jPanel1;
    }

    public void saveOptions() {
        namedColorEditor.apply();
        namedColorEditor1.apply();
        namedColorEditor2.apply();
        namedColorEditor3.apply();
        namedColorEditor4.apply();
        dfdsRoleNamedColorEditor.apply();
        dfdsRoleNamedTextColorEditor.apply();

        namedColorExt.apply();
        namedColorTextExt.apply();
        namedColorDS.apply();
        namedColorTextDS.apply();

        dataStoreNamedFont.saveFont();
        externalReferenceNamedFont.saveFont();

        namedFontEditor.saveFont();
        namedFontEditor11.saveFont();
        namedFontEditor12.saveFont();

        dfdsRoleNamedFont.saveFont();
        Options.setInteger("SECOND_PART_MINUS",
                ((Number) secondPartMinus.getValue()).intValue());
        Options.setBoolean("DISABLE_RENDERING_HINTS",
                !enableRenderingHints.isSelected());
        MovingArea.DISABLE_RENDERING_HINTS = !enableRenderingHints.isSelected();
        if (actionType.getSelectedIndex() == 0)
            Options.setString("ON_DOUBLE_CLICK", "EDIT");
        else
            Options.setString("ON_DOUBLE_CLICK", "RENAME");
    }

    /**
     * This method initializes namedColorEditor
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor() {
        if (namedColorEditor == null) {
            namedColorEditor = new NamedColorEditor();
            namedColorEditor.setColorName("DEFAULT_BACKGROUND", new Color(249,
                    249, 249));
        }
        return namedColorEditor;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            BoxLayout mgr = new BoxLayout(jPanel2, BoxLayout.Y_AXIS);
            jPanel2.setLayout(mgr);
            jPanel2.add(getNamedColorEditor(), null);
            jPanel2.add(getNamedColorEditor1(), null);
            jPanel2.add(getNamedColorEditor4(), null);

            jPanel2.add(namedColorDS, null);
            jPanel2.add(namedColorTextDS, null);
            jPanel2.add(namedColorExt, null);
            jPanel2.add(namedColorTextExt, null);

            jPanel2.add(getNamedColorEditor2(), null);
            jPanel2.add(getNamedColorEditor3(), null);
            dfdsRoleNamedColorEditor = new NamedColorEditor();
            dfdsRoleNamedColorEditor.setColorName("DEFAULT_DFDS_ROLE_COLOR",
                    Color.white);
            dfdsRoleNamedTextColorEditor = new NamedColorEditor();
            dfdsRoleNamedTextColorEditor.setColorName(
                    "DEFAULT_DFDS_ROLE_TEXT_COLOR", Color.black);

            jPanel2.add(dfdsRoleNamedColorEditor, null);
            jPanel2.add(dfdsRoleNamedTextColorEditor, null);

            jPanel2.add(new JPanel(new FlowLayout()), null);
            jPanel2.add(getNamedFontEditor(), null);
            jPanel2.add(getNamedFontEditor11(), null);
            jPanel2.add(getNamedFontEditor12(), null);
            dataStoreNamedFont = new NamedFontEditor();
            dataStoreNamedFont.setSelFont("DEFAULT_DATA_STORE_FONT", new Font(
                    "Dialog", Font.BOLD, 8));
            externalReferenceNamedFont = new NamedFontEditor();
            externalReferenceNamedFont.setSelFont(
                    "DEFAULT_EXTERNAL_REFERENCE_FONT", new Font("Dialog",
                            Font.BOLD, 8));

            dfdsRoleNamedFont = new NamedFontEditor();
            dfdsRoleNamedFont.setSelFont("DEFAULT_DFDS_ROLE_FONT", new Font(
                    "Dialog", Font.BOLD, 8));

            jPanel2.add(dataStoreNamedFont, null);
            jPanel2.add(externalReferenceNamedFont, null);
            jPanel2.add(dfdsRoleNamedFont, null);
            JPanel spPanel = new JPanel(new BorderLayout());
            spPanel.add(secondPartMinus, BorderLayout.EAST);
            spPanel.add(new JLabel("DFDS Details font minus"), BorderLayout.WEST);
            jPanel2.add(spPanel, null);
            jPanel2.add(new JPanel(new FlowLayout()), null);
            jPanel2.add(getDefaultDoubleClickActionPanel(), null);
            jContentPane.add(enableRenderingHints, BorderLayout.SOUTH);

        }
        return jPanel2;
    }

    private JPanel getDefaultDoubleClickActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JLabel("ON_DOUBLE_CLICK"), BorderLayout.WEST);
        panel.add(actionType, BorderLayout.EAST);

        return panel;
    }

    /**
     * This method initializes namedFontEditor
     *
     * @return com.jason.clasificators.frames.NamedFontEditor
     */
    private NamedFontEditor getNamedFontEditor() {
        if (namedFontEditor == null) {
            namedFontEditor = new NamedFontEditor();
            namedFontEditor.setSelFont("DEFAULT_ARROW_FONT", new Font("Dialog",
                    0, 10));
        }
        return namedFontEditor;
    }

    /**
     * This method initializes namedFontEditor11
     *
     * @return com.jason.clasificators.frames.NamedFontEditor
     */
    private NamedFontEditor getNamedFontEditor11() {
        if (namedFontEditor11 == null) {
            namedFontEditor11 = new NamedFontEditor();
            namedFontEditor11.setSelFont("DEFAULT_FUNCTIONAL_BLOCK_FONT");
        }
        return namedFontEditor11;
    }

    /**
     * This method initializes namedFontEditor12
     *
     * @return com.jason.clasificators.frames.NamedFontEditor
     */
    private NamedFontEditor getNamedFontEditor12() {
        if (namedFontEditor12 == null) {
            namedFontEditor12 = new NamedFontEditor();
            namedFontEditor12.setSelFont("DEFAULT_TEXT_FONT", new Font("Arial",
                    0, 10));
        }
        return namedFontEditor12;
    }

    /**
     * This method initializes namedColorEditor1
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor1() {
        if (namedColorEditor1 == null) {
            namedColorEditor1 = new NamedColorEditor();
            namedColorEditor1.setColorName("DEFAULD_FUNCTIONAL_BLOCK_COLOR",
                    Color.white);
        }
        return namedColorEditor1;
    }

    /**
     * This method initializes namedColorEditor2
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor2() {
        if (namedColorEditor2 == null) {
            namedColorEditor2 = new NamedColorEditor();
            namedColorEditor2.setColorName("DEFAULT_ARROW_COLOR", Color.black);
        }
        return namedColorEditor2;
    }

    /**
     * This method initializes namedColorEditor3
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor3() {
        if (namedColorEditor3 == null) {
            namedColorEditor3 = new NamedColorEditor();
            namedColorEditor3.setColorName("DEFAULT_TEXT_COLOR", Color.black);
        }
        return namedColorEditor3;
    }

    /**
     * This method initializes namedColorEditor4
     *
     * @return com.jason.clasificators.frames.NamedColorEditor
     */
    private NamedColorEditor getNamedColorEditor4() {
        if (namedColorEditor4 == null) {
            namedColorEditor4 = new NamedColorEditor();
            namedColorEditor4.setColorName(
                    "DEFAULD_FUNCTIONAL_BLOCK_TEXT_COLOR", Color.black);
        }
        return namedColorEditor4;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
