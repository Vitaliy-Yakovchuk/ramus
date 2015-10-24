package com.ramussoft.pb.frames.docking;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.ramussoft.pb.frames.MainFrame;

/**
 * Панель призначена для розташування на вікні, що перетягується.
 *
 * @author Яковчук В. В.
 */

public abstract class ViewPanel extends JPanel {

    public static final String CLASIFICATORS = "clasificators";

    public static final String ELEMENTS_TABLE = "elementsTable";

    public static final String ATTRIBUTE_EDIT = "AttributeEdit";

    public static final String DESCRIBE = "describe";

    public static final String NOTE = "note";

    public static final String FUNCTION_TREE_PANEL = "functionTreePanel";

    public static final String ACTIVATE_FUNCTION_OBJECT = "functionObjectTreePanel";

    public static final String IDEF0_EDITOR = "idef0Model";

    public static final String MATRIXES_LIST = "matrixList";

    public static final String MATRIX_LIST = "matrixListEditor";

    public static final String MATRIX_TABLE = "matrixTableEditor";

    public static final String REPORT_LIST = "roportList";

    public static final String ROW_REPORT_CHOOSER = "rowReportChooser";

    public static final String WORKSPACE_GROUP = "workspaceGroup";

    public static final String ROW_ATTRUBUTE_DATA = "rowAttributeData";

    public static final String ATTRIBUTES_LIST = "AttributesList";

    public static final String REPORT_EDIT_AREA = "ReportEditArea";

    public static final String REPORT_COMPONENTS_LIST = "ReportComponentsList";

    public static final String ACTIVE_ROW = "activeRow";

    public static final String ATTRIBUTE_DATA = "attributeData";

    public static final String ACTIVE_ATTRIBUTE_DATA = "activeAttributeData";

    protected MainFrame frame;

    public abstract String[] getEnableActions();

    public abstract String getTitleKey();

    public abstract void actionPerformed(ActionEvent e);

    public ViewPanel(final MainFrame frame) {
        super();
        this.frame = frame;
    }

    public MainFrame getFrame() {
        return frame;
    }
}
