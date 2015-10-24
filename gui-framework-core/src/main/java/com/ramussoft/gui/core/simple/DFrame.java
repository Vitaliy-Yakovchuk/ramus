package com.ramussoft.gui.core.simple;

import java.awt.BorderLayout;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.ramussoft.gui.common.View;

public abstract class DFrame extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 7023744457343751095L;

    public abstract String getId();

    protected String title;

    private JToolBar toolBar;

    private JPanel right = new JPanel(new BorderLayout());

    public DFrame(final Control control) {
        super(new BorderLayout());
        setFocusable(true);
        setFocusCycleRoot(true);
        this.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                control.focusGained(DFrame.this);
            }
        });
        control.addDFrame(this);
    }

    public abstract View getView();

    public void addSeparator() {
        if (toolBar != null)
            toolBar.addSeparator();
    }

    public void setTitleText(String newTitle) {
        this.title = newTitle;
    }

    public String getTitleText() {
        return title;
    }

    public void removeAction(Action button) {
        if (toolBar == null)
            return;

        KeyStroke stroke = (KeyStroke) button
                .getValue(AbstractAction.ACCELERATOR_KEY);
        if (stroke != null) {
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(stroke);
            getActionMap().remove(button.getValue(Action.NAME));
        }

        for (Component component : toolBar.getComponents()) {
            if (component instanceof AbstractButton) {
                AbstractButton button2 = (AbstractButton) component;
                if (button2.getAction() == button) {
                    toolBar.remove(button2);
                }
            }
        }
        int bc = 0;
        for (Component component : toolBar.getComponents())
            if (component instanceof AbstractButton)
                bc++;
        if (bc == 0) {
            right.remove(toolBar);
            remove(right);
            toolBar = null;
            revalidate();
            repaint();
        }
    }

    public void addAction(Action button, String tooltip) {
        KeyStroke stroke = (KeyStroke) button
                .getValue(AbstractAction.ACCELERATOR_KEY);
        if (stroke != null) {
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke,
                    button.getValue(Action.NAME));
            getActionMap().put(button.getValue(Action.NAME), button);
        }

        if (toolBar == null) {
            toolBar = new JToolBar();
            JButton add = toolBar.add(button);
            if (tooltip != null)
                add.setToolTipText(tooltip);
            toolBar.setFloatable(false);
            right.add(toolBar, BorderLayout.EAST);
            add(right, BorderLayout.NORTH);
            revalidate();
            repaint();
        } else {
            JButton add = toolBar.add(button);
            if (tooltip != null)
                add.setToolTipText(tooltip);
        }

    }

}
