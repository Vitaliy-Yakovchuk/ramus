package com.ramussoft.gui.elist;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeAdapter;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;

public class ElistTableTabView extends AbstractView implements TabView {

    public static final int CELL_BORDER = 20;

    private static final int PART_Q = 4;

    private JTable table;

    private Qualifier q1;

    private Qualifier q2;

    private Attribute attribute;

    private ElistTableModel model;

    private JScrollPane pane;

    private ElistTablePanel left;

    private ElistTablePanel top;

    private TopTablePanel tableHeader;

    private AbstractAction revertAction = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = -3460703953840412148L;

        {
            putValue(ACTION_COMMAND_KEY, "Action.ElementList.Revert");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/revert.png")));
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            ElistTablePanel tmp = left;
            left = top;
            top = tmp;

            model.close();
            model = createModel();
            if ((Boolean) getValue(SELECTED_KEY)) {
                model.setRevert(true);
            } else {
                model.setRevert(false);
            }

            table.setModel(model);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            tableHeader = new TopTablePanel(top, table);
            table.setTableHeader(tableHeader);
            table.setRowHeight(CELL_BORDER);

            tableHeader.revalidate();
            tableHeader.repaint();

            TableColumnModel columnModel = table.getColumnModel();
            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                TableColumn column = columnModel.getColumn(i);
                column.setMaxWidth(CELL_BORDER);
                column.setMinWidth(CELL_BORDER);
            }

            pane.setRowHeaderView(left);
            updateHeadders();
            pane.revalidate();
            pane.repaint();
            pane.setViewportView(table);
        }

    };

    private ElistTableModel createModel() {
        return new ElistTableModel(framework.getEngine(),
                framework.getAccessRules(), attribute, left, top);
    }

    private AttributeListener listener = new AttributeAdapter() {
        @Override
        public void attributeUpdated(AttributeEvent event) {
            if (event.getAttribute().equals(attribute)) {
                attribute = (Attribute) event.getNewValue();
                ViewTitleEvent titleEvent = new ViewTitleEvent(
                        ElistTableTabView.this, event.getAttribute().getName());
                titleChanged(titleEvent);
            }
        }

        @Override
        public void attributeDeleted(AttributeEvent event) {
            if (event.getAttribute().equals(attribute)) {
                close();
            }
        }
    };

    public ElistTableTabView(GUIFramework framework, Attribute attribute) {
        super(framework);
        Engine engine = framework.getEngine();
        ElementListPropertyPersistent p = (ElementListPropertyPersistent) engine
                .getAttribute(null, attribute);
        this.attribute = attribute;
        this.q1 = engine.getQualifier(p.getQualifier1());
        this.q2 = engine.getQualifier(p.getQualifier2());
        engine.addAttributeListener(listener);
    }

    @Override
    public String getTitle() {
        return attribute.getName();
    }

    @Override
    public JComponent createComponent() {
        pane = new JScrollPane();
        left = new ElistTablePanel(framework, q1);
        top = new ElistTablePanel(framework, q2);

        this.model = createModel();

        this.table = new ElistTable() {

            /**
             *
             */
            private static final long serialVersionUID = 5056893447852539087L;

            @Override
            protected Engine getEngine() {
                return framework.getEngine();
            }

            @Override
            protected Attribute getAttribute() {
                return attribute;
            }
        };
        this.table.setModel(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableHeader = new TopTablePanel(top, table);
        table.setTableHeader(tableHeader);
        table.setRowHeight(CELL_BORDER);
        table.setColumnSelectionAllowed(true);

        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            column.setMaxWidth(CELL_BORDER);
            column.setMinWidth(CELL_BORDER);
        }

        table.getColumnModel().addColumnModelListener(
                new TableColumnModelListener() {

                    @Override
                    public void columnAdded(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnMarginChanged(ChangeEvent e) {
                    }

                    @Override
                    public void columnMoved(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnRemoved(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnSelectionChanged(ListSelectionEvent e) {
                        int column = table.getSelectedColumn();
                        if (column >= 0) {
                            top.setSelectionRow(column);
                            tableHeader.repaint();
                        }
                    }

                });

        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        int row = table.getSelectedRow();
                        if (row >= 0) {
                            left.setSelectionRow(row);
                        }
                    }
                });

        pane.setViewportView(table);
        pane.setRowHeaderView(left);

        updateHeadders();

        pane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateHeadders();
            }
        });

        return pane;
    }

    private void updateHeadders() {
        left.setPreferredSize(new Dimension(pane.getSize().width / PART_Q, left
                .getPreferredSize().height));
        Dimension preferredSize = new Dimension(
                top.getRowCount() * CELL_BORDER, pane.getSize().height / PART_Q);
        tableHeader.setPreferredSize(preferredSize);
        top.setSize(preferredSize);

    }

    @Override
    public Action[] getActions() {
        return new Action[]{revertAction};
    }

    @Override
    public void close() {
        super.close();
        left.close();
        top.close();
        model.close();
        framework.getEngine().removeAttributeListener(listener);
    }

    @Override
    public ActionEvent getOpenAction() {
        return new ActionEvent(ElistPlugin.OPEN_ELEMENT_LIST_IN_TABLE,
                attribute);
    }

}
