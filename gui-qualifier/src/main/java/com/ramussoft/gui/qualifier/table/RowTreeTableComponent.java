package com.ramussoft.gui.qualifier.table;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.DropMode;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.ScrollPanePreview;

public class RowTreeTableComponent extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 8614772131250564957L;

    private RowTreeTable table;

    private RowSet rowSet;

    protected RowTreeTableModel model;

    private TableRowHeader header;

    private JScrollPane pane = new JScrollPane();

    private FindPanel findPanel = new FindPanel() {
        /**
         *
         */
        private static final long serialVersionUID = -4667439439794542091L;

        @Override
        public boolean find(String text, boolean wordsOrder) {
            return table.find(text, wordsOrder);
        }

        @Override
        public boolean findNext(String text, boolean wordsOrder) {
            return table.findNext(text, wordsOrder);
        }
    };

    public boolean isShowFindPanel() {
        return findPanel.isVisible();
    }

    public void setShowFindPanel(boolean b) {
        findPanel.setVisible(b);
    }

    public RowTreeTableComponent(Engine engine, Qualifier qualifier,
                                 AccessRules accessRules, RootCreater rootCreater,
                                 Attribute[] attributes, GUIFramework framework) {
        super(new BorderLayout());

        AttributePlugin[] plugins = new AttributePlugin[attributes.length];

        for (int i = 0; i < attributes.length; i++) {
            AttributePlugin plugin = framework
                    .findAttributePlugin(attributes[i]);
            if (plugin == null) {
                System.err.println("WARNING: GUI Plugin for attribute type: "
                        + attributes[i].getAttributeType() + " not found");
            } else {
                plugins[i] = plugin;
            }
        }

        model = createRowTreeTableModel(engine, qualifier, accessRules,
                rootCreater, attributes, framework);

        if (qualifier.equals(StandardAttributesPlugin
                .getAttributesQualifier(engine))) {
            model.setAttributeLocalizer(
                    new RowTreeTableModel.Localizer() {
                        @Override
                        public Object getValue(Object key) {
                            return GlobalResourcesManager
                                    .getString("AttributeType." + key);
                        }
                    }, (Attribute) engine.getPluginProperty("Core",
                            StandardAttributesPlugin.ATTRIBUTE_TYPE_NAME));

        }

        table = createTable(accessRules, framework, plugins);
        model.setTable(table);
        this.rowSet = model.getRowSet();
        table.setDropMode(DropMode.ON_OR_INSERT_ROWS);
        table.setTransferHandler(new RowTransferHandle());

        header = new TableRowHeader(table, model);

        boolean dragEnable = true;
        // Fixing dragging of IDEF0 and DFD functions
        for (Attribute attr : qualifier.getSystemAttributes()) {
            if (attr.getAttributeType().getTypeName().equals("VisualData"))
                dragEnable = false;
        }

        header.setDragEnabled(dragEnable);
        header.setTransferHandler(new RowTransferHandle());
        header.setDropMode(DropMode.INSERT);

        pane.setViewportView(table);
        pane.setRowHeaderView(header);
        this.add(pane, BorderLayout.CENTER);
        findPanel.setVisible(false);
        this.add(findPanel, BorderLayout.SOUTH);

        table.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK),
                "showFindPanel");

        table.getActionMap().put("showFindPanel", new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(final ActionEvent e) {
                if (!findPanel.isVisible()) {
                    findPanel.setVisible(true);
                }
                findPanel.getJTextField().requestFocus();
            }

        });

        ScrollPanePreview.install(pane);
    }

    protected RowTreeTable createTable(AccessRules accessRules,
                                       GUIFramework framework, AttributePlugin[] plugins) {
        return new RowTreeTable(accessRules, model.getRowSet(), plugins,
                framework, model);
    }

    protected RowTreeTableModel createRowTreeTableModel(Engine engine,
                                                        Qualifier qualifier, AccessRules accessRules,
                                                        RootCreater rootCreater, Attribute[] attributes,
                                                        GUIFramework framework) {
        return new RowTreeTableModel(engine, qualifier, attributes,
                accessRules, rootCreater, framework);
    }

    public RowTreeTable getTable() {
        return table;
    }

    public RowSet getRowSet() {
        return rowSet;
    }

    public RowTreeTableModel getModel() {
        return model;
    }

    public void setSelectType(SelectType b) {
        this.header.setSelectType(b);
    }

    public JScrollPane getPane() {
        return pane;
    }

    public void updateTableHeaderWidth() {
        header.updateWidth();
    }

    public TableRowHeader getHeader() {
        return header;
    }
}
