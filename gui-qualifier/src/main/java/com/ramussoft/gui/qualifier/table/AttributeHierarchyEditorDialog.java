package com.ramussoft.gui.qualifier.table;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class AttributeHierarchyEditorDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -7147683216147121517L;

    private final AttributeHierarchyEditorPanel panel;

    private final JCheckBox box = new JCheckBox(GlobalResourcesManager
            .getString("ShowBaseHierarchi"));

    private Hierarchy hierarchy;

    public AttributeHierarchyEditorDialog(final JDialog dialog,
                                          GUIFramework framwork) {
        super(dialog, true);
        panel = new AttributeHierarchyEditorPanel(framwork);
        init();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setTitle(GlobalResourcesManager.getString("HierarchyType"));
    }

    private void init() {
        final JPanel p = new JPanel(new BorderLayout());
        p.add(panel, BorderLayout.CENTER);
        p.add(box, BorderLayout.SOUTH);
        setMainPane(p);
        setLocationRelativeTo(null);
    }

    public void showModal(Hierarchy hierarchy, Qualifier qualifier) {
        this.hierarchy = hierarchy;
        if (hierarchy == null)
            hierarchy = new Hierarchy();
        List<Attribute> list = qualifier.getAttributes();
        final Attribute[] attrs = hierarchy.getAttributes(list);
        final Attribute[] all = list.toArray(new Attribute[list.size()]);
        panel.setAttributes(all, attrs);
        box.setSelected(hierarchy.isShowBaseHierarchy());
        setVisible(true);
    }

    @Override
    protected void onOk() {
        if (hierarchy == null)
            hierarchy = new Hierarchy();
        hierarchy.setAttributes(panel.getAttributes());
        hierarchy.setShowBaseHierarchy(box.isSelected());
        if (panel.getAttributes().length == 0)
            hierarchy = null;
        super.onOk();
    }

    public Attribute[] getAttributes() {
        return panel.getAttributes();
    }

    public boolean isShowRowFolders() {
        return box.isSelected();
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }
}
