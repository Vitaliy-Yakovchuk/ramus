package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public abstract class AttributeEditorPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 5981349270409250010L;

    private JComponent component = null;

    protected AttributeEditor editor;

    private Engine engine;

    private Attribute attribute;

    private Element element;

    protected Object value;

    private boolean canUpdate;

    private boolean acceptable = true;

    private JButton okButton;

    private AbstractAction okAction = new AbstractAction(GlobalResourcesManager
            .getString("ok")) {

        /**
         *
         */
        private static final long serialVersionUID = -6856310909204918078L;

        @Override
        public void actionPerformed(ActionEvent e) {
            ok();
        }

    };

    private AbstractAction cancelAction = new AbstractAction(
            GlobalResourcesManager.getString("cancel")) {

        /**
         *
         */
        private static final long serialVersionUID = -2356310908975418045L;

        @Override
        public void actionPerformed(ActionEvent e) {
            cancel();
        }

    };

    private AbstractAction applyAction = new AbstractAction(
            GlobalResourcesManager.getString("apply")) {

        /**
         *
         */
        private static final long serialVersionUID = -4566731090897543565L;

        @Override
        public void actionPerformed(ActionEvent e) {
            apply();
        }

    };

    private JPanel okc;

    private JButton cancel;

    private JButton apply;

    public AttributeEditorPanel() {
        super(new BorderLayout());
        okc = new JPanel(new GridLayout(1, 3, 5, 0));

        JButton ok = new JButton(okAction);

        cancel = new JButton(cancelAction);

        apply = new JButton(applyAction);

        addOk(ok, okc);
        okc.add(apply);
        okc.add(cancel);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        panel.add(okc);
        this.add(panel, BorderLayout.SOUTH);
        setAttributeEditor(null, null, null, null, false, null);
    }

    protected void addOk(JButton ok, JPanel okc) {
        okc.add(ok);
        this.okButton = ok;
    }

    public void setAttributeEditor(AttributeEditor editor, Engine engine,
                                   Attribute attribute, Element element, boolean canUpdate,
                                   Object value) {
        if (this.editor != null) {
            savePreferences();
        }
        this.value = value;
        if ((this.editor != null) && (editor == null))
            this.editor.close();
        this.editor = editor;
        this.engine = engine;
        this.attribute = attribute;
        this.element = element;
        if ((editor != null) && (acceptable != editor.isAcceptable())) {
            acceptable = editor.isAcceptable();
            if (acceptable) {
                okc.add(apply);
                okc.add(cancel);
                if (okButton != null)
                    okButton.setText(GlobalResourcesManager.getString("ok"));
                okc.revalidate();
                okc.repaint();
            } else {
                okc.remove(apply);
                okc.remove(cancel);
                if (okButton != null)
                    okButton.setText(GlobalResourcesManager.getString("close"));
                okc.revalidate();
                okc.repaint();
            }
        }

        if (component != null) {
            this.remove(component);
            component = null;
        }
        if (editor != null) {
            component = editor.getComponent();
            value = editor.setValue(value);
        } else {
            component = new JLabel(GlobalResourcesManager
                    .getString("AttributeEditor.Empty"));
        }

        applyAction.setEnabled((editor != null) && (canUpdate));
        okAction.setEnabled((editor != null) && (canUpdate));
        loadPreferences();
        this.add(component, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
        this.canUpdate = canUpdate;
    }

    public void loadPreferences() {
        if ((attribute != null) && (component != null))
            Options.loadOptions(getSavePrefix() + "attributeEditor_"
                    + attribute.getId(), component);
    }

    public Object getValue() {
        if (editor != null)
            value = this.editor.getValue();
        return value;
    }

    public void ok() {
        try {
            if (apply())
                close();
        } catch (Exception e) {
            e.printStackTrace();
            if (((Journaled) engine).isUserTransactionStarted())
                ((Journaled) engine).rollbackUserTransaction();
            close();
        }
    }

    public boolean apply() {
        if (!editor.canApply()) {
            editor.showErrorMessage();
            return false;
        }
        if (engine instanceof Journaled)
            ((Journaled) engine).startUserTransaction();
        editor.apply(engine, element, attribute, getValue());
        if ((engine instanceof Journaled)
                && (((Journaled) engine).isUserTransactionStarted()))
            ((Journaled) engine).commitUserTransaction();
        return true;
    }

    public void cancel() {
        close();
    }

    protected void close() {
        editor.close();
        savePreferences();
    }

    private void savePreferences() {
        Options.saveOptions(getSavePrefix() + "attributeEditor_"
                + attribute.getId(), component);
    }

    public Object getSavedValue() {
        return value;
    }

    public boolean isCanUpdate() {
        return canUpdate;
    }

    public boolean isSaveAnyway() {
        if (editor != null)
            return editor.isSaveAnyway();
        return false;
    }

    public AttributeEditor getEditor() {
        return editor;
    }

    protected String getSavePrefix() {
        return "";
    }
}
