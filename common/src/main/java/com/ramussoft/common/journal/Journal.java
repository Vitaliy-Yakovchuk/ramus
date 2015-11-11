package com.ramussoft.common.journal;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import javax.swing.event.EventListenerList;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.command.Command;
import com.ramussoft.common.journal.command.CreateAttributeCommand;
import com.ramussoft.common.journal.command.CreateElementCommand;
import com.ramussoft.common.journal.command.CreateQualifierCommand;
import com.ramussoft.common.journal.command.DeleteAttributeCommand;
import com.ramussoft.common.journal.command.DeleteElementCommand;
import com.ramussoft.common.journal.command.DeleteQualifierCommand;
import com.ramussoft.common.journal.command.EndUserTransactionCommand;
import com.ramussoft.common.journal.command.FormulaCommand;
import com.ramussoft.common.journal.command.NewBranchCommand;
import com.ramussoft.common.journal.command.NextCommand;
import com.ramussoft.common.journal.command.SetElementQualifierCommand;
import com.ramussoft.common.journal.command.SetStreamCommand;
import com.ramussoft.common.journal.command.TransactionStorageCommand;
import com.ramussoft.common.journal.command.StartUserTransactionCommand;
import com.ramussoft.common.journal.command.UpdateAttributeCommand;
import com.ramussoft.common.journal.command.UpdateQualifierCommand;
import com.ramussoft.common.journal.event.JournalEvent;
import com.ramussoft.common.journal.event.JournalListener;

/**
 * Data journal, with redo and undo support.
 *
 * @author zdd
 */

public class Journal implements Journaled {

    public static final RedoCallback TRUE_REDO_CALLBACK = new RedoCallback() {
        @Override
        public boolean execute(Command command) {
            return true;
        }
    };
    protected BinaryAccessFile accessFile;

    private static Hashtable<Class<? extends Command>, Integer> commandsTypes;

    private static Hashtable<Integer, Class<? extends Command>> commandsTypeBytes;

    private Hashtable<Integer, JournaledEngine> engines = new Hashtable<Integer, JournaledEngine>(
            0);

    private EventListenerList listenerList = new EventListenerList();

    private boolean enable = false;

    private boolean transaction = false;

    private Thread userTransactionThread;

    private long minCommandPos = 0;

    public static boolean checkThreads = true;

    private boolean lock;

    private long branch;

    static {
        commandsTypes = new Hashtable<Class<? extends Command>, Integer>();
        commandsTypeBytes = new Hashtable<Integer, Class<? extends Command>>();
        add(CreateQualifierCommand.class);
        add(CreateAttributeCommand.class);
        add(CreateElementCommand.class);
        add(UpdateQualifierCommand.class);
        add(UpdateAttributeCommand.class);
        add(DeleteQualifierCommand.class);
        add(DeleteAttributeCommand.class);
        add(DeleteElementCommand.class);
        add(TransactionStorageCommand.class);
        add(StartUserTransactionCommand.class);
        add(EndUserTransactionCommand.class);
        add(StopUndoPointCommand.class);
        add(NextCommand.class);
        add(FormulaCommand.class);
        add(SetElementQualifierCommand.class);
        add(SetStreamCommand.class);
        add(NewBranchCommand.class);
    }

    public Journal(BinaryAccessFile accessFile, long branch) {
        this.accessFile = accessFile;
        this.branch = branch;
    }

    private static void add(Class<? extends Command> clazz) {
        Integer value = commandsTypes.size() + 1;
        commandsTypes.put(clazz, value);
        commandsTypeBytes.put(value, clazz);
    }

    public void store(Command command) {
        if (!enable)
            return;
        try {
            JournalEvent event = new JournalEvent(this, command, getPointer());
            beforeStore(event);
            accessFile.writeByte(command.getEngineId());
            accessFile.writeByte(commandsTypes.get(command.getClass())
                    .byteValue());
            command.writeCommand(accessFile, 2);
            if (accessFile.length() > accessFile.getFilePointer()) {
                accessFile.setLength(accessFile.getFilePointer());
            }

            if (command instanceof StopUndoPointCommand)
                minCommandPos = accessFile.getFilePointer();

            afterStore(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void afterStore(JournalEvent event) {
        for (JournalListener listener : getJournalListeners())
            listener.afterStore(event);
    }

    protected void beforeStore(JournalEvent event) {
        for (JournalListener listener : getJournalListeners())
            listener.beforeStore(event);
    }

    public void moveStart() {
        if (!enable)
            return;
        try {
            accessFile.seek(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void moveEnd() {
        if (!enable)
            return;
        try {
            accessFile.seek(accessFile.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEnd() throws IOException {
        if (!enable)
            return true;
        return accessFile.getFilePointer() == accessFile.length();
    }

    public boolean isStart() throws IOException {
        if (!enable)
            return true;
        return accessFile.getFilePointer() <= minCommandPos;
    }

    public void registerEngine(JournaledEngine engine) {
        engines.put(engine.getId(), engine);
    }

    public Command redo() {
        return redo(TRUE_REDO_CALLBACK);
    }

    public Command redo(RedoCallback callback) {
        Command command = null;
        long index = getPointer();
        try {
            command = readNext();
            IEngine engine = command.getEngine().deligate;
            if(callback.execute(command))
                command.redo(engine);

            JournalEvent event = new JournalEvent(this, command, index);
            afterRedo(event);
        } catch (Exception e) {
            try {
                accessFile.seek(index);
            } catch (IOException e1) {
            }
            throw new RuntimeException(e);
        }
        return command;
    }

    protected void afterRedo(JournalEvent event) {
        for (JournalListener listener : getJournalListeners()) {
            listener.afterRedo(event);
        }
    }

    public Command undo() {
        long index = getPointer();
        Command command = null;
        try {
            command = readPrev();
            IEngine engine = command.getEngine().deligate;
            command.undo(engine);

            JournalEvent event = new JournalEvent(this, command, getPointer());
            afterUndo(event);
        } catch (Exception e) {
            try {
                accessFile.seek(index);
            } catch (IOException e1) {
            }
            throw new RuntimeException(e);
        }
        return command;
    }

    protected void afterUndo(JournalEvent event) {
        for (JournalListener listener : getJournalListeners()) {
            listener.afterUndo(event);
        }
    }

    private Command readNext() throws IOException {
        return readCommand();
    }

    private Command readCommand() throws IOException {
        int engineId = accessFile.read();
        JournaledEngine engine = getEngine(engineId);
        int commandType = accessFile.read();
        Class<? extends Command> clazz = commandsTypeBytes.get(commandType);
        try {
            Constructor<? extends Command> c = clazz
                    .getConstructor(JournaledEngine.class);
            Command command = c.newInstance(engine);
            command.readCommand(accessFile, 2);
            if (command instanceof StopUndoPointCommand)
                minCommandPos = accessFile.getFilePointer();
            return command;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected JournaledEngine getEngine(int engineId) {
        return engines.get(engineId);
    }

    private Command readPrev() throws IOException {
        Command.moveCommandStart(accessFile);
        long pos = accessFile.getFilePointer();
        Command res = readCommand();
        accessFile.seek(pos);
        return res;
    }

    public void close() {
        if (accessFile == null)
            return;
        try {
            accessFile.close();
            accessFile = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canUndo() {
        if (lock)
            return false;
        try {
            return !isStart();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canRedo() {
        if (lock)
            return false;
        try {
            return !isEnd();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startUserTransaction() {
        synchronized (this) {
            if (transaction) {
                if (checkThreads) {
                    if (userTransactionThread != Thread.currentThread())
                        return;
                }
                throw new RuntimeException("Transaction is already started");
            }
            transaction = true;
            StartUserTransactionCommand command = new StartUserTransactionCommand(
                    null);
            store(command);
            if (checkThreads) {
                userTransactionThread = Thread.currentThread();
            }
        }
    }

    @Override
    public void commitUserTransaction() {
        synchronized (this) {
            if (checkThreads) {
                if (userTransactionThread != Thread.currentThread())
                    return;
            }
            if (!transaction)
                throw new RuntimeException("Transaction is not started");
            transaction = false;
            EndUserTransactionCommand command = new EndUserTransactionCommand(
                    null);
            store(command);
            if (checkThreads) {
                userTransactionThread = null;
            }
        }
    }

    @Override
    public void rollbackUserTransaction() {
        Command command;
        while (!((command = undo()) instanceof StartUserTransactionCommand)) {
            if (command instanceof EndUserTransactionCommand) {
                redo();
                throw new RuntimeException(
                        "Trying to rollback commited transaction");
            }
        }
        ;
        transaction = false;
        try {
            if (accessFile.length() > accessFile.getFilePointer()) {
                accessFile.setLength(accessFile.getFilePointer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param enable the enabled to set
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * @return the enabled
     */
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void redoUserTransaction() {
        int counter = 0;

        try {
            Command command = redo();
            counter++;
            if (!(command instanceof StartUserTransactionCommand)) {
                throw new Exception("Command is not user transaction");
            }

            while (!(redo() instanceof EndUserTransactionCommand)) {
                counter++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            while (counter > 0) {
                counter--;
                undo();
            }
        }
    }

    @Override
    public void undoUserTransaction() {
        int counter = 0;

        try {
            Command command = undo();
            counter++;
            if (!(command instanceof EndUserTransactionCommand)) {
                throw new Exception("Command is not user transaction");
            }
            while (!((command = undo()) instanceof StartUserTransactionCommand)) {
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            while (counter > 0) {
                counter--;
                redo();
            }
        }
    }

    public long getPointer() {
        if (!enable)
            return 0;
        try {
            return accessFile.getFilePointer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addJournalListener(JournalListener listener) {
        listenerList.add(JournalListener.class, listener);
    }

    @Override
    public JournalListener[] getJournalListeners() {
        return listenerList.getListeners(JournalListener.class);
    }

    @Override
    public void removeJournalListener(JournalListener listener) {
        listenerList.remove(JournalListener.class, listener);
    }

    public BinaryAccessFile getAccessFile() {
        return accessFile;
    }

    @Override
    public boolean isUserTransactionStarted() {
        return transaction;
    }

    @Override
    public void setNoUndoPoint() {
        store(new StopUndoPointCommand((JournaledEngine) engines.values()
                .toArray()[0]));
    }

    /**
     * @return the lock
     */
    public boolean isLock() {
        return lock;
    }

    /**
     * @param lock the lock to set
     */
    public void setLock(boolean lock) {
        this.lock = lock;
    }

    @Override
    public long getBranch() {
        return branch;
    }

    public interface RedoCallback {
        boolean execute(Command command);
    }
}
