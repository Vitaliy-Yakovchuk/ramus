package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.Timer;

import com.ramussoft.common.Element;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.Commands;

public class ScriptReportEditorView extends ReportEditorView {

    private ScriptEditorView editorView;

    private com.ramussoft.gui.common.event.ActionListener fullRefresh;

    private String saved;

    private Timer timer;

    private boolean changed = false;

    private Object lock = new Object();

    private long changeTime;

    public ScriptReportEditorView(final GUIFramework framework,
                                  final Element element) {
        super(framework, element);
        timer = new Timer(500, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean save;
                synchronized (lock) {
                    if (System.currentTimeMillis() - changeTime < 500)
                        return;
                    save = changed;
                }

                if (save) {
                    save();
                    synchronized (lock) {
                        changed = false;
                    }
                }
            }
        });

        timer.start();

        editorView = new ScriptEditorView(framework) {
            /**
             *
             */
            private static final long serialVersionUID = -8884124882782860341L;

            @Override
            protected void changed() {
                synchronized (lock) {
                    changed = true;
                    changeTime = System.currentTimeMillis();
                }
            }
        };

        editorView.load(framework.getEngine(), element);

        saved = editorView.getText();

        fullRefresh = new com.ramussoft.gui.common.event.ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                editorView.load(framework.getEngine(), element);
            }
        };
        framework.addActionListener(Commands.FULL_REFRESH, fullRefresh);

    }

    @Override
    public JComponent createComponent() {
        JComponent createComponent = super.createComponent();
        content.add(editorView, BorderLayout.CENTER);
        activeView = editorView;
        return createComponent;
    }

    @Override
    protected void createButtons(ButtonGroup group) {
        JToggleButton button1 = createOpenViewButton(group, editorView);
        button1.setSelected(true);
        buttonsPanel.add(button1);
        super.createButtons(group);
    }

    @Override
    public void beforeSubviewActivated(SubView view) {
        save();
        super.beforeSubviewActivated(view);
    }

    @Override
    public void close() {
        super.close();
        save();
        framework.removeActionListener(Commands.FULL_REFRESH, fullRefresh);
    }

    @Override
    protected void save() {
        super.save();
        if (!editorView.getText().equals(saved)) {
            saved = editorView.getText();
            editorView.save(framework.getEngine(), element);
        }
    }
}
