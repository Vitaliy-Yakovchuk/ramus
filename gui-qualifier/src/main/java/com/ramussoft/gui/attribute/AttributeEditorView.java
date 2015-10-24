package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.util.ArrayList;

import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.View;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;

public class AttributeEditorView extends AbstractUniqueView implements UniqueView,
        Commands {

    private Object savedValue = null;

    private ElementAttribute eAttribute;

    private Action[] actions;

    private ElementListener listener = new ElementAdapter() {
        public void elementDeleted(
                final com.ramussoft.common.event.ElementEvent event) {
            if ((eAttribute != null)
                    && (event.getOldElement().equals(eAttribute.element))) {

                panel.setAttributeEditor(null, event.getEngine(), null,
                        null, false, null);


            }
        }

        ;
    };

    public static class ElementAttribute {

        private boolean isFindAction;

        public ElementAttribute() {
        }

        public ElementAttribute(Element element, Attribute attribute) {
            this.element = element;
            this.attribute = attribute;
        }

        public ElementAttribute(Element element, Attribute attribute,
                                Object value) {
            this(element, attribute);
            this.value = value;
        }

        public boolean canUpdate(AccessRules rules) {
            return rules.canUpdateElement(element.getId(), attribute.getId());
        }

        /**
         * @param isFindAction the isFindAction to set
         */
        public void setFindAction(boolean isFindAction) {
            this.isFindAction = isFindAction;
        }

        /**
         * @return the isFindAction
         */
        public boolean isFindAction() {
            return isFindAction;
        }

        public Element element;

        public Attribute attribute;

        public Object value;
    }

    private AttributeEditorPanel panel = new AttributeEditorPanel() {

        /**
         *
         */
        private static final long serialVersionUID = 1566154537830962733L;

        @Override
        public boolean apply() {
            boolean b = super.apply();
            if (b)
                savedValue = getSavedValue();
            return b;
        }

        @Override
        public void cancel() {
            editor.setValue(savedValue);
        }

        @Override
        protected void addOk(JButton ok, JPanel okc) {
        }

        protected void close() {

        }

        ;
    };

    public AttributeEditorView(final GUIFramework framework,
                               final Engine engine, final AccessRules rules) {
        super(framework);
        framework.addActionListener(ACTIVATE_ATTRIBUTE, new ActionListener() {
            @Override
            public void onAction(final ActionEvent event) {

                if (panel.isCanUpdate()) {

                    Object value = panel.getValue();

                    if ((!AttributeEditorView.this.equals(savedValue, value))
                            || (panel.isSaveAnyway())) {
                        /*
                         * if (showConfirmDialog( framework.getMainFrame(),
						 * format( GlobalResourcesManager
						 * .getString("AttributeChangedConformMessage"),
						 * eAttribute.attribute.getName()),
						 * GlobalResourcesManager
						 * .getString("ConformDialogTitle"),
						 * JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						 */
                        panel.ok();
						/*
						 * else panel.cancel();
						 */
                    }
                }

                if (actions != null)
                    actionsRemoved(actions);

                eAttribute = (ElementAttribute) event.getValue();

                if (eAttribute == null) {
                    try {
                        panel.setAttributeEditor(null, engine, null, null,
                                false, null);
                    } catch (NullPointerException e) {
                        // e.printStackTrace();
                        return;
                    }
                    return;
                }

                AttributePlugin ap = framework
                        .findAttributePlugin(eAttribute.attribute);

                AttributeEditor editor = null;

                actions = null;

                if (ap == null) {
                    System.err.println("WARNING: Attribute plugin for "
                            + eAttribute.attribute.getAttributeType()
                            + " not found.");
                } else {
                    editor = ap.getAttributeEditor(engine, rules,
                            eAttribute.element, eAttribute.attribute,
                            "editorView", panel.getEditor());
                }

                try {
                    Object value;
                    if (event.getMetadata() == null)
                        value = engine.getAttribute(eAttribute.element,
                                eAttribute.attribute);
                    else
                        value = event.getMetadata();
                    panel.setAttributeEditor(editor, engine,
                            eAttribute.attribute, eAttribute.element,
                            eAttribute.canUpdate(rules), value);
                } catch (NullPointerException e) {
                    // e.printStackTrace();
                    return;
                }
                savedValue = panel.getSavedValue();
                if (editor != null) {
                    List<Action> list = new ArrayList<Action>();
                    for (Action a : editor.getActions())
                        if (a != null)
                            list.add(a);
                    actions = list.toArray(new Action[list.size()]);
                    actionsAdded(actions);
                }
            }
        });

        engine.addElementListener(null, listener);
    }

    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    @Override
    public String getId() {
        return "AttributeEditorView";
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null)
            return o2 == null;
        return o1.equals(o2);
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.Qualifiers";
    }

    @Override
    public void close() {
        panel.setAttributeEditor(null, null, null, null, false, null);
        framework.getEngine().removeElementListener(null, listener);
        super.close();
    }

    @Override
    public ActionEvent getOpenActionForSave() {
        View view = framework.getLastDinamicView();
        if (view != null) {
            ActionEvent event = view.getOpenAction();
            if (event != null)
                return event;
        }
        return super.getOpenActionForSave();
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.EAST;
    }
}
