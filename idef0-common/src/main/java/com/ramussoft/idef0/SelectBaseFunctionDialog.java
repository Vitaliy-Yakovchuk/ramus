package com.ramussoft.idef0;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class SelectBaseFunctionDialog extends BaseDialog {

    private JList list = new JList();

    private Qualifier result;

    public SelectBaseFunctionDialog(GUIFramework framework) {
        super(framework.getMainFrame(), true);
        setTitle(GlobalResourcesManager.getString("SelectModel"));
        result = null;
        final List<Qualifier> base = IDEF0Plugin.getBaseQualifiers(framework
                .getEngine());
        Collections.sort(base, new Comparator<Qualifier>() {

            private Collator collator = Collator.getInstance();

            @Override
            public int compare(Qualifier o1, Qualifier o2) {
                return collator.compare(o1.getName(), o2.getName());
            }
        });
        list.setModel(new AbstractListModel() {

            @Override
            public Object getElementAt(int index) {
                return base.get(index);
            }

            @Override
            public int getSize() {
                return base.size();
            }

        });

        JScrollPane pane = new JScrollPane();
        pane.setViewportView(list);
        setMainPane(pane);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        Options.loadOptions(this);
    }

    @Override
    protected void onOk() {
        Options.saveOptions(this);
        result = (Qualifier) list.getSelectedValue();
        if (result == null) {
            JOptionPane.showMessageDialog(this, ResourceLoader
                    .getString("select_model_first"));
        } else
            super.onOk();
    }

    public Qualifier getResult() {
        return result;
    }
}
