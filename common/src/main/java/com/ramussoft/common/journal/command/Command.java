package com.ramussoft.common.journal.command;

import java.io.IOException;
import java.io.Serializable;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.journal.BinaryAccessFile;
import com.ramussoft.common.journal.BinaryDataInput;
import com.ramussoft.common.journal.BinaryDataOutput;
import com.ramussoft.common.journal.JournaledEngine;

/**
 * Journal command.
 *
 * @author zdd
 */

public abstract class Command implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2148805074911735770L;

    /**
     * Length to previous command less then 256 bytes;
     */

    public static final int LENGTH_BYTE = 0;

    /**
     * Length to previous command less then 65536 bytes;
     */

    public static final int LENGTH_SHORT = 1;

    /**
     * Length to previous command less then Integer.MAX_VALUE bytes;
     */

    public static final int LENGTH_INTEGER = 2;

    protected transient int length = -1;

    private transient int fullLength;

    protected transient JournaledEngine engine;

    /**
     * Length of this command in bytes without length footer.
     */

    public int getLength() {
        return length;
    }

    /**
     * Length type in bytes (, LENGTH_BYTE, LENGTH_SHORT, LENGTH_INTEGER).
     */

    public int getLenghtType() {
        return getLengthType(getLength());
    }

    private static int getLengthType(int length) {
        if (length < 256)
            return LENGTH_BYTE;
        if (length < Short.MAX_VALUE)
            return LENGTH_SHORT;
        return LENGTH_INTEGER;
    }

    public abstract void writeBody(BinaryDataOutput output) throws IOException;

    public abstract void readBody(BinaryDataInput input) throws IOException;

    public abstract void redo(IEngine engine);

    public abstract void undo(IEngine engine);

    public Command(JournaledEngine engine) {
        this.engine = engine;
    }

    /**
     * Write command without command type byte(s).
     *
     * @param file
     * @param commandLength As usual = 1, reserved for future.
     * @throws IOException
     */

    public void writeCommand(BinaryAccessFile file, int commandLength)
            throws IOException {
        long pos = file.getFilePointer() - commandLength;
        writeBody(file);
        length = (int) (file.getFilePointer() - pos);
        fullLength = length;
        int lengthType = getLenghtType();
        switch (lengthType) {
            case LENGTH_BYTE:
                file.writeByte(getLength());
                fullLength++;
                break;
            case LENGTH_SHORT:
                file.writeShort(getLength());
                fullLength += 2;
                break;
            default:
                file.writeInt(getLength());
                fullLength += 4;
                break;
        }
        file.writeByte(lengthType);
        fullLength++;
    }

    /**
     * Read command content (without command type byte(s)).
     *
     * @param commandLength As usual = 1, reserved for future.
     * @throws IOException
     */

    public void readCommand(BinaryAccessFile file, int commandLength)
            throws IOException {
        long pos = file.getFilePointer() - commandLength;
        readBody(file);
        length = (int) (file.getFilePointer() - pos);
        fullLength = length;
        int lengthType = getLenghtType();
        switch (lengthType) {
            case LENGTH_BYTE:
                file.read();
                fullLength += 1;
                break;
            case LENGTH_SHORT:
                file.readShort();
                fullLength += 2;
                break;
            default:
                file.readInt();
                fullLength += 4;
                break;
        }
        file.read();
        fullLength += 1;
    }

    public int getEngineId() {
        return engine.getId();
    }

    public int getFullLength() {
        return fullLength;
    }

    public static void moveCommandStart(BinaryAccessFile file)
            throws IOException {
        file.move(-1);
        int lengthType = file.read();
        int length;
        int move = -1;
        switch (lengthType) {
            case LENGTH_BYTE:
                move -= 1;
                file.move(move);
                length = file.read();
                break;
            case LENGTH_SHORT:
                move -= 2;
                file.move(move);
                length = file.readShort();
                break;
            default:
                move -= 4;
                file.move(move);
                length = file.readInt();
                break;
        }
        move += 1;
        move -= length;
        file.move(move);
    }

    public JournaledEngine getEngine() {
        return engine;
    }
}
