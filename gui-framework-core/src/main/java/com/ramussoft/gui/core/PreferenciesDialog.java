package com.ramussoft.gui.core;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.prefrence.Preferences;

public class PreferenciesDialog extends BaseDialog {

    private List<Preferences> list;

    private Engine engine;

    public PreferenciesDialog(JFrame mainFrame, List<Preferences> list,
                              Engine engine) {
        super(mainFrame, true);
        this.list = list;
        this.engine = engine;
        JTabbedPane pane = new JTabbedPane();
        for (Preferences p : list) {
            pane.addTab(p.getTitle(), p.createComponent());
        }
        setMainPane(pane);
        pack();
        this.setMinimumSize(new Dimension(800, 510));
        centerDialog();
        Options.loadOptions(this);
    }

    /**
     *
     */
    private static final long serialVersionUID = 7550468939406299738L;

    @Override
    protected void onOk() {
        for (Preferences p : list) {
            if (engine instanceof Journaled)
                ((Journaled) engine).startUserTransaction();
            if (!p.save(this)) {
                if (engine instanceof Journaled)
                    ((Journaled) engine).undoUserTransaction();
                return;
            }
        }
        super.onOk();
        if (engine instanceof Journaled)
            ((Journaled) engine).commitUserTransaction();
    }

    @Override
    public void setVisible(boolean b) {
        if (!b) {
            Options.saveOptions(this);
            for (Preferences p : list) {
                p.close();
            }
        }
        super.setVisible(b);
    }
}
