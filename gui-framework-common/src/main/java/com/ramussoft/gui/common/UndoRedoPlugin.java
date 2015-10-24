package com.ramussoft.gui.common;

import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import com.ramussoft.common.Engine;
import com.ramussoft.common.event.BranchAdapter;
import com.ramussoft.common.event.BranchEvent;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.event.JournalListener;

public class UndoRedoPlugin extends AbstractViewPlugin {

    private Engine engine;

    public static final String AFTER_UNDO = "AfterUndo";

    public static final String AFTER_REDO = "AfterRedo";

    public static final String AFTER_UNDO_OR_REDO = "AfterUndoOrRedo";

    public UndoRedoPlugin(Engine engine) {
        this.engine = engine;

        if (engine instanceof Journaled) {
            ((Journaled) engine)
                    .addJournalListener((JournalListener) createChangeListener(JournalListener.class));
        }

        engine.addBranchListener(new BranchAdapter() {
            @Override
            public void branchActivated(BranchEvent event) {
                updateUndoRedo();
            }
        });
    }

    private Object createChangeListener(Class<?> clazz) {
        return Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{clazz}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {
                        updateUndoRedo();
                        return null;
                    }
                });
    }

    @Override
    public String getName() {
        return "UndoRedo";
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        ActionDescriptor undo = new ActionDescriptor();

        undoAction.putValue(Action.ACTION_COMMAND_KEY, "Undo");
        undoAction.putValue(Action.SMALL_ICON,
                getIcon("/com/ramussoft/gui/undo.png"));

        undo.setAction(undoAction);
        undo.setMenu("Edit");
        undo.setToolBar("Edit");

        ActionDescriptor redo = new ActionDescriptor();
        redoAction.putValue(Action.ACTION_COMMAND_KEY, "Redo");
        redoAction.putValue(Action.SMALL_ICON,
                getIcon("/com/ramussoft/gui/redo.png"));

        redo.setAction(redoAction);
        redo.setMenu("Edit");
        redo.setToolBar("Edit");

        updateUndoRedo();
        return new ActionDescriptor[]{undo, redo};
    }

    private ImageIcon getIcon(String resourceName) {
        return new ImageIcon(getClass().getResource(resourceName));
    }

    protected void updateUndoRedo() {
        redoAction.setEnabled(false);
        undoAction.setEnabled(false);
        redoAction.setEnabled(((Journaled) engine).canRedo());
        undoAction.setEnabled(((Journaled) engine).canUndo());
    }

    private AbstractAction undoAction = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = -4072851007034587989L;

        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                    KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (((Journaled) engine).canUndo()) {
                ((Journaled) engine).undoUserTransaction();
                framework.propertyChanged(AFTER_UNDO);
                framework.propertyChanged(AFTER_UNDO_OR_REDO);
            }
        }
    };

    private AbstractAction redoAction = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = -5481533393396963128L;

        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                    KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (((Journaled) engine).canRedo()) {
                ((Journaled) engine).redoUserTransaction();
                framework.propertyChanged(AFTER_REDO);
                framework.propertyChanged(AFTER_UNDO_OR_REDO);
            }
        }
    };
}
