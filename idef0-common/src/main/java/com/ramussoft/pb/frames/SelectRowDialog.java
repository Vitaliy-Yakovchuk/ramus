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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.prefrence.Options;
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
public class SelectRowDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean ok;

    private JPanel root = new JPanel(new BorderLayout());

    private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    private GUIFramework framework;

    private DataPlugin dataPlugin;

    private AccessRules accessRules;

    private QualifierView qualifierView;

    private SelectableTableView selectableTableView;

    private Hashtable<Qualifier, List<Row>> selectedRows = new Hashtable<Qualifier, List<Row>>();

    private Qualifier qualifier;

    private List<Element> qualifierHideElements = null;

    private boolean addQualifier = false;

    private SelectType selectType = SelectType.CHECK;

    private SelectionListener selectionListener = new SelectionListener() {
        public void changeSelection(SelectionEvent event) {
            addQualifier = false;
        }

        ;
    };

    public void init(GUIFramework framework, DataPlugin dataPlugin,
                     AccessRules accessRules) {
        this.framework = framework;
        this.dataPlugin = dataPlugin;
        this.accessRules = accessRules;
    }

    public List<com.ramussoft.pb.Row> showModal() {
        final Qualifier q1 = qualifier;
        selectedRows.clear();
        createComponents();
        if (q1 != null) {
            Options.saveOptions(this);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    qualifierView.setSelectedQualifier(q1);
                    Options.loadOptions(SelectRowDialog.this);
                    addQualifier = false;
                }
            });
        }
        ok = false;
        setVisible(true);
        deleteComponents();
        List<com.ramussoft.pb.Row> result = new ArrayList<com.ramussoft.pb.Row>();
        if (ok) {
            for (List<Row> list : selectedRows.values()) {
                for (Row row : list) {
                    com.ramussoft.pb.Row r = dataPlugin.findRowByGlobalId(row
                            .getElementId());
                    if (r != null)
                        result.add(r);
                }
            }
            if ((addQualifier) && (qualifier != null)) {
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
        }
        Options.saveOptions(this);
        return result;
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
        addQualifier = b;
        if (b) {
            framework.setOpenDynamikViewEvent(new ActionEvent(
                    QualifierPlugin.OPEN_QUALIFIER, qualifier));
            selectableTableView = new SelectableTableView(framework, dataPlugin
                    .getEngine(), accessRules, qualifier) {
            };

            int dl = splitPane.getDividerLocation();
            splitPane.setRightComponent(createComponent());
            for (Row row : getSelectedRows(qualifier))
                selectableTableView.getComponent().getModel().setSelectedRow(
                        row, true);
            splitPane.revalidate();
            splitPane.repaint();
            splitPane.setDividerLocation(dl);
            selectableTableView.getComponent().getModel().addSelectionListener(
                    selectionListener);
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

    private void deleteComponents() {
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
     * This is the default constructor
     */
    public SelectRowDialog() {
        super();
        initialize();
    }

    public SelectRowDialog(final JFrame frame) {
        super(frame);
        initialize();
    }

    public SelectRowDialog(final JDialog dialog) {
        super(dialog);
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setModal(true);
        setTitle("select_row");
        root.add(splitPane, BorderLayout.CENTER);
        setMainPane(root);
        setMinSizePack();
        this.setSize(776, 515);
        splitPane.setDividerLocation(250);
        ResourceLoader.setJComponentsText(this);
        setLocationRelativeTo(null);
        com.ramussoft.gui.common.prefrence.Options.loadOptions(this);
    }

    @Override
    protected void onOk() {
        ok = true;
        super.onOk();
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public void setVisible(boolean b) {
        if (!b) {
            com.ramussoft.gui.common.prefrence.Options.saveOptions(this);
        }
        super.setVisible(b);
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
} // @jve:decl-index=0:visual-constraint="10,10"
