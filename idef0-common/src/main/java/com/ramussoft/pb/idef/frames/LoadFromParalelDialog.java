package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.pb.frames.BaseDialog;

public class LoadFromParalelDialog extends BaseDialog {

    private final JPanel panel = new JPanel(new BorderLayout());

    private boolean res;

    private final JCheckBox importAllClasificators = new JCheckBox(
            "importAllClasificators");

    private ArrayList<JCheckBox> boxes = new ArrayList<JCheckBox>();

    private ArrayList<Qualifier> qualifiers = new ArrayList<Qualifier>();

    public LoadFromParalelDialog(final JFrame frame, Engine engine) {
        super(frame, true);
        setTitle("loadFromParalel");

        JPanel qPanel = new JPanel();
        qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
        //qPanel.setLayout(new FlowLayout());

        List<Qualifier> list = engine.getQualifiers();

        for (Qualifier qualifier : list) {
            if (IDEF0Plugin.isFunction(qualifier)) {
                qualifiers.add(qualifier);
                JCheckBox box = new JCheckBox();
                box.setText(qualifier.getName());
                boxes.add(box);
                qPanel.add(box);
            }
        }

        JScrollPane pane = new JScrollPane();


        panel.add(pane, BorderLayout.CENTER);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(importAllClasificators);
        panel.add(p, BorderLayout.SOUTH);

        setMainPane(panel);

        ResourceLoader.setJComponentsText(this);
        pane.setViewportView(qPanel);
        pack();
        centerDialog();
        Options.loadOptions("loadFromParalel", this);
    }

    @Override
    protected void onOk() {
        res = true;
        super.onOk();
    }

    public boolean isImportAll() {
        return importAllClasificators.isSelected();
    }

    public List<Qualifier> getSelected() {
        ArrayList<Qualifier> res = new ArrayList<Qualifier>();
        int i = 0;
        for (JCheckBox box : boxes) {
            if (box.isSelected()) {
                res.add(qualifiers.get(i));
            }
            i++;
        }
        return res;
    }

    public boolean showModal() {
        res = false;
        setVisible(true);
        Options.saveOptions("loadFromParalel", this);
        return res;
    }
}
