package com.ramussoft.gui.qualifier.table;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.core.attribute.standard.EvalPlugin;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.eval.FunctionPersistent;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.eval.SetFormulaDialog;

public abstract class TableTabView extends TableView {

    private RecalculateAction recalculateAction = new RecalculateAction();

    private SetFormulaAction setFormulaAction = new SetFormulaAction();

    private ConvertElementsToQualifierAction convertElementsToQualifierAction = new ConvertElementsToQualifierAction();

    private ElementAttributeListener elementAttributeListener;

    private ActionListener actionListener;

    private Hashtable<String, Object> tag = new Hashtable<String, Object>();

    public TableTabView(GUIFramework framework, final Engine engine,
                        final AccessRules rules, final Qualifier qualifier) {
        super(framework, engine, rules, qualifier);
        updateRecalculateAction(qualifier, rules,
                EvalPlugin.getFunctions(engine, qualifier));

        final Element element = StandardAttributesPlugin.getElement(engine,
                qualifier.getId());

        elementAttributeListener = new ElementAttributeListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void attributeChanged(AttributeEvent event) {
                if ((event.getAttribute().equals(EvalPlugin
                        .getFunctionAttribute(engine)))
                        && (event.getElement().equals(element))) {
                    updateRecalculateAction(qualifier, rules,
                            (List<FunctionPersistent>) event.getNewValue());
                }
            }
        };
        engine.addElementAttributeListener(
                StandardAttributesPlugin.getQualifiersQualifier(engine),
                elementAttributeListener);
        actionListener = new ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                if (event.getValue().equals(new Long(qualifier.getId())))
                    close();
            }
        };
        framework.addActionListener("CloseQualifier", actionListener);
    }

    @Override
    public void close() {
        super.close();
        engine.removeElementAttributeListener(
                StandardAttributesPlugin.getQualifiersQualifier(engine),
                elementAttributeListener);
        framework.removeActionListener("CloseQualifier", actionListener);
    }

    private void updateRecalculateAction(Qualifier qualifier,
                                         AccessRules rules, List<FunctionPersistent> functions) {
        if (rules.canCreateElement(qualifier.getId()))
            recalculateAction.setEnabled(getFunctions(
                    new ArrayList<FunctionPersistent>(functions)).size() > 0);
        else
            recalculateAction.setEnabled(false);
    }

    @Override
    public Action[] getActions() {
        Action[] tmp = super.getActions();

        List<Action> plugable = new ArrayList<Action>();

        for (Action action : tmp)
            plugable.add(action);

        for (GUIPlugin plugin : framework.getPlugins()) {
            if (plugin instanceof ElementActionPlugin) {
                Action[] actions = ((ElementActionPlugin) plugin)
                        .getActions(this);
                if (actions != null) {
                    plugable.add(null);
                    for (Action action : actions) {
                        plugable.add(action);
                    }
                }
            }
        }

        plugable.add(6, convertElementsToQualifierAction);

        return plugable.toArray(new Action[plugable.size()]);
    }

    @Override
    protected void tableSelectedValueChanged() {
        super.tableSelectedValueChanged();
        try {
            if ((table.getSelectedColumn() >= 0)
                    && (table.getSelectedNode() != null)
                    && (table.getSelectedNode().getRow() != null)) {
                setFormulaAction.setEnabled(accessRules.canUpdateElement(
                        elementAttribute.element.getId(),
                        elementAttribute.attribute.getId()));

            } else {
                setFormulaAction.setEnabled(false);
                setElementIconAction.setEnabled(false);
            }
        } catch (Exception e) {
        }
        convertElementsToQualifierAction.setEnabled(accessRules
                .canCreateQualifier() && table.getRowCount() > 0);
    }

    private List<FunctionPersistent> getFunctions() {
        List<FunctionPersistent> functions = EvalPlugin.getFunctions(engine,
                qualifier);
        return getFunctions(functions);
    }

    @Override
    protected void createInnerComponent() {
        super.createInnerComponent();
        convertElementsToQualifierAction.setEnabled(accessRules
                .canCreateQualifier() && table.getRowCount() > 0);
    }

    private List<FunctionPersistent> getFunctions(
            List<FunctionPersistent> functions) {
        for (int i = functions.size() - 1; i >= 0; i--) {
            if (functions.get(i).getAutochange() != 0)
                functions.remove(i);
        }
        return functions;
    }

    public RecalculateAction getRecalculateAction() {
        return recalculateAction;
    }

    public SetFormulaAction getSetFormulaAction() {
        return setFormulaAction;
    }

    private class RecalculateAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1356276512919498315L;

        @Override
        public void actionPerformed(ActionEvent e) {
            List<FunctionPersistent> functions = getFunctions();
            RecalculateDialog dialog = new RecalculateDialog(framework,
                    functions, qualifier);
            dialog.setVisible(true);
            List<Attribute> attributes = dialog.getResult();
            if (attributes.size() > 0) {
                ((Journaled) engine).startUserTransaction();
                for (int i = functions.size() - 1; i >= 0; i--) {
                    FunctionPersistent fp = functions.get(i);
                    boolean rem = true;
                    for (Attribute attr : attributes) {
                        if (attr.getId() == fp.getQualifierAttributeId()) {
                            rem = false;
                            break;
                        }
                    }
                    if (rem)
                        functions.remove(i);
                }
                EvalPlugin.calculate(engine, qualifier, functions);
                ((Journaled) engine).commitUserTransaction();
            }
        }

        public RecalculateAction() {
            putValue(ACTION_COMMAND_KEY, "Action.Recalculate");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/recalculate.png")));
        }
    }

    ;

    private class SetFormulaAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5874999065483075657L;

        public SetFormulaAction() {
            putValue(ACTION_COMMAND_KEY, "Action.SetFormula");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/formula.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Engine engine = framework.getEngine();
            CalculateInfo info = engine.getCalculateInfo(
                    elementAttribute.element.getId(),
                    elementAttribute.attribute.getId());
            if (info == null) {
                info = new CalculateInfo(elementAttribute.element.getId(),
                        elementAttribute.attribute.getId(), null);
            }

            SetFormulaDialog dialog = new SetFormulaDialog(framework, info);
            dialog.setVisible(true);
        }

    }

    ;

    private class ConvertElementsToQualifierAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -915489301203615980L;

        public ConvertElementsToQualifierAction() {
            putValue(ACTION_COMMAND_KEY, "Action.ElementsToQualifier");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(
                            getClass()
                                    .getResource(
                                            "/com/ramussoft/gui/table/element-to-qualifier.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            String message = "<html><body>"
                    + GlobalResourcesManager
                    .getString("Warning.ElementsWillBeConverted")
                    + "<hr>" + GlobalResourcesManager.getString("Elements")
                    + ":<br><table>" + getFirstLevel()
                    + "</table></body></html>";

            if (JOptionPane.showConfirmDialog(framework.getMainFrame(),
                    message, UIManager.getString("OptionPane.titleText"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;
            framework.showAnimation(GlobalResourcesManager
                    .getString("Wait.DataProcessing"));
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {
                        ((Journaled) engine).startUserTransaction();
                        convertFirstLevelToQualifiers();
                        ((Journaled) engine).commitUserTransaction();
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(
                                        framework.getMainFrame(),
                                        ex.getLocalizedMessage());
                            }
                        });

                        ((Journaled) engine).rollbackUserTransaction();
                    }

                    framework.hideAnimation();
                }
            });
            thread.start();
        }

        private String getFirstLevel() {
            StringBuffer sb = new StringBuffer();
            for (Row row : getComponent().getRowSet().getRoot().getChildren()) {
                sb.append("<tr>");
                sb.append("<td>");
                sb.append(row.getCode());
                sb.append("</td>");
                sb.append("<td>");
                sb.append(row.getName());
                sb.append("</td>");
                sb.append("</tr>");
            }
            return sb.toString();
        }
    }

    public void convertFirstLevelToQualifiers() {
        Qualifier qq = StandardAttributesPlugin.getQualifiersQualifier(engine);
        HierarchicalPersistent hp = new HierarchicalPersistent();
        Element element = StandardAttributesPlugin.getElement(engine,
                getQualifier().getId());
        hp.setParentElementId(element.getId());
        hp.setPreviousElementId(-1l);

        Attribute hAttribute = StandardAttributesPlugin
                .getHierarchicalAttribute(engine);

        Attribute nameAttribute = StandardAttributesPlugin
                .getAttributeNameAttribute(engine);

        for (Row row : toArray(component.getRowSet().getRoot().getChildren())) {
            String name = row.getName();
            Row[] children = toArray(row.getChildren());
            engine.setElementQualifier(row.getElementId(), qq.getId());
            row.getElement().setQualifierId(qq.getId());
            engine.setAttribute(row.getElement(), hAttribute, hp);
            engine.setAttribute(row.getElement(), nameAttribute, name);
            hp.setPreviousElementId(row.getElementId());
            Qualifier qualifier = StandardAttributesPlugin.getQualifier(engine,
                    row.getElement());

            for (Attribute attribute : getQualifier().getAttributes()) {
                if (qualifier.getAttributes().indexOf(attribute) < 0)
                    qualifier.getAttributes().add(attribute);
            }

            engine.updateQualifier(qualifier);

            for (Row row2 : children) {
                moveRows(row2, qualifier.getId());
                HierarchicalPersistent h = (HierarchicalPersistent) engine
                        .getAttribute(row2.getElement(), hAttribute);
                h.setParentElementId(-1l);
                row2.setAttribute(hAttribute, h);
            }
        }

        Attribute nameAttr = null;
        for (Attribute attribute : qualifier.getAttributes())
            if (attribute.getId() == qualifier.getAttributeForName())
                nameAttr = attribute;

        getQualifier().getAttributes().clear();

        if (nameAttr != null)
            getQualifier().getAttributes().add(nameAttr);

        engine.updateQualifier(getQualifier());
    }

    ;

    private void moveRows(Row row, long qualifierId) {
        for (Row row2 : toArray(row.getChildren())) {
            moveRows(row2, qualifierId);
        }
        engine.setElementQualifier(row.getElementId(), qualifierId);
    }

    private Row[] toArray(List<Row> children) {
        return children.toArray(new Row[children.size()]);
    }

    public Hashtable<String, Object> getTag() {
        return tag;
    }
}
