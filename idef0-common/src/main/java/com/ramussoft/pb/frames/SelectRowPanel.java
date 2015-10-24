package com.ramussoft.pb.frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.qualifier.QualifierPlugin;
import com.ramussoft.gui.qualifier.QualifierView;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;
import com.ramussoft.pb.DataPlugin;

/**
 * @author ZDD
 */
public class SelectRowPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private GUIFramework framework;

    private DataPlugin dataPlugin;

    private AccessRules accessRules;

    private QualifierView qualifierView;

    private SelectableTableView selectableTableView;

    private Hashtable<Qualifier, List<Row>> selectedRows = new Hashtable<Qualifier, List<Row>>();

    private Qualifier qualifier;

    private List<Element> qualifierHideElements = null;

    private SelectType selectType = SelectType.CHECK;

    private SelectionListener selectionListener = new SelectionListener() {
        public void changeSelection(SelectionEvent event) {
            for (SelectionListener listener : selectionListeners)
                listener.changeSelection(event);
        }

        ;
    };

    private List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

    public SelectRowPanel(GUIFramework framework, DataPlugin dataPlugin,
                          AccessRules accessRules) {
        super(new BorderLayout());
        this.framework = framework;
        this.dataPlugin = dataPlugin;
        this.accessRules = accessRules;
        initialize();
    }

    private void createComponents() {
        if (framework == null) {
            throw new NullPointerException("framework equals to null");
        }
        splitPane.setRightComponent(new JPanel());

        qualifierView = new QualifierView(framework, dataPlugin.getEngine(),
                accessRules) {
            @Override
            protected void addListeners() {
                table.addSelectionListener(new SelectionListener() {
                    @Override
                    public void changeSelection(SelectionEvent event) {
                        deleteRightPanel();
                        createRightComponent();
                    }
                });
                table.setEditIfNullEvent(false);
                table.getInputMap().put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "EditCell");
                table.getActionMap().put("EditCell", new AbstractAction() {
                    /**
                     *
                     */
                    private static final long serialVersionUID = 3229634866196074563L;

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        if ((table.getSelectedRow() >= 0)
                                && (table.getSelectedColumn() >= 0))
                            editTableField();
                    }
                });
            }
        };

        if (qualifierHideElements != null)
            qualifierView.setHideElements(qualifierHideElements);

        splitPane.setLeftComponent(qualifierView.createComponent());
        createRightComponent();

    }

    private void createRightComponent() {
        if (qualifierView == null)
            return;
        qualifier = qualifierView.getSelectedQualifier();
        boolean b = qualifier != null;
        if (b) {
            framework.setOpenDynamikViewEvent(new ActionEvent(
                    QualifierPlugin.OPEN_QUALIFIER, qualifier));
            selectableTableView = new SelectableTableView(framework,
                    dataPlugin.getEngine(), accessRules, qualifier) {
            };

            int dl = splitPane.getDividerLocation();
            splitPane.setRightComponent(createComponent());
            for (Row row : getSelectedRows(qualifier))
                selectableTableView
                        .getComponent()
                        .getModel()
                        .setSelectedRow(
                                selectableTableView.getComponent().getRowSet()
                                        .findRow(row.getElementId()), true);

            splitPane.revalidate();
            splitPane.repaint();
            splitPane.setDividerLocation(dl);
            selectableTableView.getComponent().getModel()
                    .addSelectionListener(selectionListener);
            if (!getSelectType().equals(SelectType.CHECK))
                selectableTableView.setSelectType(getSelectType());
        }
    }

    private JComponent createComponent() {
        JPanel result = new JPanel(new BorderLayout());
        JComponent component = selectableTableView.createComponent();
        result.add(component, BorderLayout.CENTER);
        result.add(createToolBar(selectableTableView.getActions()),
                BorderLayout.NORTH);
        return result;
    }

    private Component createToolBar(Action[] actions) {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        for (Action action : actions) {
            if (action == null)
                bar.addSeparator();
            else
                bar.add(action).setFocusable(false);
        }
        return bar;
    }

    private List<Row> getSelectedRows(Qualifier qualifier) {
        List<Row> res = selectedRows.get(qualifier);
        if (res == null) {
            res = new ArrayList<Row>();
            selectedRows.put(qualifier, res);
        }
        return res;
    }

    public void dispose() {
        deleteRightPanel();
        qualifierView.close();
        qualifierView = null;
        framework.setOpenDynamikViewEvent(null);
    }

    private void deleteRightPanel() {
        if (selectableTableView != null) {
            selectableTableView.getComponent().getModel()
                    .removeSelectionListener(selectionListener);
            List<Row> list = selectableTableView.getComponent().getModel()
                    .getSelectedRows();
            selectedRows.put(qualifier, list);
            selectableTableView.close();
            selectableTableView = null;
            splitPane.setRightComponent(new JPanel());
        }
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(250);
        ResourceLoader.setJComponentsText(this);
        final Qualifier q1 = qualifier;
        selectedRows.clear();
        createComponents();
        if (q1 != null) {
            qualifierView.setSelectedQualifier(q1);
        }

    }

    public void setQuaifierHideElements(List<Element> quaifierHideElements) {
        this.qualifierHideElements = quaifierHideElements;
        if (qualifierView != null)
            qualifierView.setHideElements(quaifierHideElements);
    }

    /**
     * @param selectType the selectType to set
     */
    public void setSelectType(SelectType selectType) {
        this.selectType = selectType;
        if (selectableTableView != null)
            selectableTableView.setSelectType(selectType);
    }

    /**
     * @return the selectType
     */
    public SelectType getSelectType() {
        return selectType;
    }

    public void addSelectionListener(SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
    }

    public void removeSelectionListener(SelectionListener selectionListener) {
        selectionListeners.remove(selectionListener);
    }

    public void selectRows(List<Row> rows) {
        selectedRows.clear();

        if (selectableTableView != null)
            selectableTableView.clearSelection();

        List<Long> ss = new ArrayList<Long>();

        for (Row row : rows) {
            Qualifier qualifier = row.getRowSet().getQualifier();
            List<Row> sels = getSelectedRows(qualifier);
            sels.add(row);
            if (this.qualifier != null && this.selectableTableView != null
                    && this.qualifier.equals(qualifier))
                ss.add(row.getElementId());
        }
        if (selectableTableView != null)
            selectableTableView.selectRows(ss);
        repaint();
    }

    public List<com.ramussoft.pb.Row> getSelected() {
        {
            List<Row> list = selectableTableView.getComponent().getModel()
                    .getSelectedRows();
            selectedRows.put(qualifier, list);
        }

        List<com.ramussoft.pb.Row> result = new ArrayList<com.ramussoft.pb.Row>();
        for (List<Row> list : selectedRows.values()) {
            for (Row row : list) {
                com.ramussoft.pb.Row r = dataPlugin.findRowByGlobalId(row
                        .getElementId());
                if (r != null)
                    result.add(r);
            }
        }
        if (qualifier != null) {
            Engine engine = dataPlugin.getEngine();
            Qualifier q = StandardAttributesPlugin
                    .getQualifiersQualifier(engine);
            List<Element> list = engine.getElements(q.getId());
            for (Element element : list) {
                if (qualifier.equals(StandardAttributesPlugin.getQualifier(
                        engine, element))) {
                    com.ramussoft.pb.Row r = dataPlugin
                            .findRowByGlobalId(element.getId());
                    if (r != null) {
                        result.add(r);
                    } else {
                        System.err
                                .println("WARNING: Can not find element for qualifier ("
                                        + qualifier.getName() + ")");
                    }
                }
            }
        }
        return result;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
