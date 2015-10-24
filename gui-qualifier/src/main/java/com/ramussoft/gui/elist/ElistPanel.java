package com.ramussoft.gui.elist;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;
import com.ramussoft.gui.qualifier.table.TableViewProperties;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

public abstract class ElistPanel extends SelectableTableView implements
        SelectionListener {

    private Attribute elementList;

    private JLabel qLabel;

    private JPanel panel = new JPanel(new BorderLayout());

    public static class ElementInfo {

        public ElementInfo(double y, boolean collapsed) {
            this.collapsed = collapsed;
            this.y = y;
        }

        double y;

        boolean collapsed;
    }

    ;

    private QualifierAdapter qualifierAdapter = new QualifierAdapter() {
        @Override
        public void qualifierUpdated(QualifierEvent event) {
            if (qLabel != null) {
                if (event.getNewQualifier().equals(qualifier)) {
                    qLabel.setText(event.getNewQualifier().getName());
                    ElistPanel.this.qualifier = event.getNewQualifier();
                }
            }
        }
    };

    public ElistPanel(GUIFramework framework, Engine engine,
                      AccessRules accessor, Qualifier qualifier, Attribute elementList) {
        super(framework, engine, accessor, qualifier);
        this.elementList = elementList;
        engine.addQualifierListener(qualifierAdapter);
    }

    @Override
    public JComponent createComponent() {
        panel.removeAll();
        JComponent createComponent = super.createComponent();
        component.getModel().addSelectionListener(this);
        setSelectType(SelectType.RADIO);
        JPanel top = new JPanel(new BorderLayout());
        qLabel = new JLabel(qualifier.getName());
        top.add(qLabel, BorderLayout.WEST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(createComponent, BorderLayout.CENTER);
        panel.revalidate();
        return panel;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public TableViewProperties getProperties() {
        TableViewProperties properties = super.getProperties();
        if (properties.getHideAttributesString() == null) {
            properties.setHideAttributes(new long[]{elementList.getId()});
        }
        return properties;
    }

    @Override
    public void close() {
        super.close();
        engine.removeQualifierListener(qualifierAdapter);
    }

    public ElementInfo getElementInfo(long elementId) {
        int c = table.getRowCount();
        for (int row = 0; row < c; row++) {
            TreeTableNode node = (TreeTableNode) table.getPathForRow(row)
                    .getLastPathComponent();
            Row r = node.getRow();
            if ((r != null) && (r.getElementId() == elementId)) {
                return new ElementInfo(getRowY(row), false);
            }
            if (table.isCollapsed(row)) {
                if (isPresent(node, elementId)) {
                    return new ElementInfo(getRowY(row), true);
                }
            }
        }
        return null;
    }

    private boolean isPresent(TreeTableNode node, long elementId) {
        for (TreeTableNode child : node.getChildren()) {
            Row row = child.getRow();
            if ((row != null) && (row.getElementId() == elementId)) {
                return true;
            }
            if (isPresent(child, elementId))
                return true;
        }
        return false;
    }

    private double getRowY(int row) {
        Rectangle rect = table.getCellRect(row, 0, false);
        rect.y += rect.height / 2;
        rect.y += qLabel.getHeight();
        rect.y -= getComponent().getPane().getVerticalScrollBar().getValue()
                - getComponent().getPane().getColumnHeader().getHeight();
        return rect.y;
    }

}
