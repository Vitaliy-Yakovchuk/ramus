package com.ramussoft.gui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.command.StartUserTransactionCommand;
import com.ramussoft.common.journal.event.JournalAdatper;
import com.ramussoft.common.journal.event.JournalEvent;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.CloseMainFrameAdapter;

public class History extends JournalAdatper {

    private GUIFramework framework;

    private Hashtable<Long, Hashtable<Long, Command>> data;

    private Engine engine;

    private ObjectOutputStream objectOut;

    private static class Command implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 6992353024918108046L;

        public Command(long index, ActionEvent event, String workspace,
                       long branchId) {
            this.index = index;
            this.event = event;
            this.workspace = workspace;
            this.branchId = branchId;
        }

        long index;

        ActionEvent event;

        String workspace;

        long branchId;
    }

    @SuppressWarnings("unchecked")
    public History(GUIFramework framework, Engine engine) {
        this.framework = framework;
        this.engine = engine;
        framework.addCloseMainFrameListener(new CloseMainFrameAdapter() {
            @Override
            public void closed() {
                if (objectOut != null)
                    try {
                        objectOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                objectOut = null;
            }
        });
        ((Journaled) engine).addJournalListener(this);
        synchronized (engine) {
            try {
                data = (Hashtable<Long, Hashtable<Long, Command>>) engine
                        .getPluginProperty("History", "Hash");

                if (data == null) {
                    data = new Hashtable<Long, Hashtable<Long, Command>>();
                    engine.setPluginProperty("History", "Hash", data);
                    IEngine engine2 = engine.getDeligate();
                    if (engine2 instanceof FileIEngineImpl) {
                        FileIEngineImpl impl = (FileIEngineImpl) engine2;

                        String fileName = impl.getTmpPath() + File.separator
                                + "gui.journal";

                        List<Command> commands = new ArrayList<Command>();

                        if (new File(fileName).exists()) {
                            ObjectInputStream in = new ObjectInputStream(
                                    new FileInputStream(fileName));
                            while (in.read() == 0) {
                                Command command = (Command) in.readObject();
                                commands.add(command);
                                long branchId = command.branchId;
                                Hashtable<Long, Command> hash = getHash(branchId);
                                hash.put(command.index, command);
                            }
                            in.close();
                        }
                        objectOut = new ObjectOutputStream(
                                new FileOutputStream(fileName));
                        for (Command command : commands)
                            writeCommand(command);
                    }
                } else {
                    objectOut = (ObjectOutputStream) engine.getPluginProperty(
                            "History", "ObjectOut");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected Hashtable<Long, Command> getHash(long branchId) {
        Hashtable<Long, Command> hash = data.get(branchId);
        if (hash == null) {
            hash = new Hashtable<Long, Command>();
            data.put(branchId, hash);
        }
        return hash;
    }

    @Override
    public void afterStore(JournalEvent event) {
        if ((framework == null) || (framework.getMainFrame() == null))
            return;
        // if (!framework.getMainFrame().isActive())
        // return;
        synchronized (engine) {
            if (event.getCommand() instanceof StartUserTransactionCommand) {
                ActionEvent actionEvent = framework.getOpenDynamicViewEvent();

                if (actionEvent == null) {
                    View view = framework.getLastActiveView();
                    if (view != null) {
                        actionEvent = view.getOpenActionForSave();
                    }
                }

                if (actionEvent != null) {
                    long branch = -1l;
                    if (event.getJournal() != null)
                        branch = event.getJournal().getBranch();
                    Command command = new Command(event.getIndex(),
                            actionEvent, framework.getCurrentWorkspace(),
                            branch);
                    Hashtable<Long, Command> hash = getHash(command.branchId);
                    hash.put(event.getIndex(), command);
                    if (objectOut != null) {
                        writeCommand(command);
                    }
                }
            }
        }
    }

    private void writeCommand(Command command) {
        try {
            objectOut.write(0);
            objectOut.writeObject(command);
            objectOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
            objectOut = null;
        }
    }

    @Override
    public void afterRedo(JournalEvent event) {
        try {
            refresh(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterUndo(JournalEvent event) {
        try {
            refresh(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refresh(JournalEvent event) {
        if (!framework.getMainFrame().isActive())
            return;
        if (event.getCommand() instanceof StartUserTransactionCommand) {
            long branch = -1;
            if (event.getJournal() != null)
                branch = event.getJournal().getBranch();
            Hashtable<Long, Command> hash = getHash(branch);
            Command command = hash.get(event.getIndex());
            if (command == null)
                return;
            ActionEvent actionEvent = command.event;
            if (actionEvent != null) {
                framework.propertyChanged(actionEvent);
                if (command.workspace != null) {
                    if (!command.workspace.equals(framework
                            .getCurrentWorkspace())) {
                        framework.propertyChanged("ShowWorkspace",
                                command.workspace);
                    }
                }
            }
        }
    }

}
