package com.ramussoft.common.journal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.MessageFormat;

public class BinaryAccessFile extends RandomAccessFile implements
        BinaryDataInput, BinaryDataOutput {

    public BinaryAccessFile(File file, String mode)
            throws FileNotFoundException {
        super(file, mode);
    }

    public BinaryAccessFile(String name, String mode)
            throws FileNotFoundException {
        super(name, mode);
    }

    /**
     * This method reads string in UTF8 even it's length more then 65536, string
     * length can be up to Integer.MAX_VALUE. String can be <code>null</code>.
     */

    @Override
    public String readString() throws IOException {
        int length = readInt();
        if (length < 0)
            return null;
        byte[] bs = new byte[length];
        readFully(bs);
        return new String(bs, "UTF8");
    }

    /**
     * This method reads string in UTF8 even if its length less then 128 bytes
     * in UTF8, string length can be up to Integer.MAX_VALUE. String can be
     * <code>null</code>.
     */

    @Override
    public String read254String() throws IOException {
        int length = read();
        if (length == 255) {
            return null;
        }
        byte[] bs = new byte[length];
        readFully(bs);
        return new String(bs, "UTF8");
    }

    /**
     * This method writes string in UTF8 even it's length more then 65536,
     * string length can be up to Integer.MAX_VALUE. String can be
     * <code>null</code>.
     */

    @Override
    public void writeString(String string) throws IOException {
        if (string == null) {
            writeInt(-1);
        } else {
            byte[] bs = string.getBytes("UTF-8");
            writeInt(bs.length);
            write(bs);
        }
    }

    /**
     * This method writes string in UTF8 even if its length less then 254 bytes
     * in UTF8, string length can be up to Integer.MAX_VALUE. String can be
     * <code>null</code>.
     */

    @Override
    public void write254String(String string) throws IOException {
        if (string == null) {
            write(255);
        } else {
            byte[] bs = string.getBytes("UTF8");
            int length = bs.length;
            if (length > 254) {
                throw new IOException(
                        MessageFormat
                                .format(
                                        "String \"{0}\" can not be written, its length {1} bytes (more then 254 bytes)",
                                        string, length));
            }
            write(length);
            write(bs);
        }
    }

    /**
     * Method is looked like to skipBytes, but if byte < 0 then pointer reduces
     * equivalently.
     *
     * @throws IOException if an I/O error occurs.
     */

    public int move(int n) throws IOException {
        if (n >= 0)
            return skipBytes(n);
        long pos;
        long newpos;

        pos = getFilePointer();
        newpos = pos + n;
        if (newpos < 0) {
            newpos = 0;
        }
        seek(newpos);

        return (int) (newpos - pos);

    }

    @Override
    public String readSwimedString() throws IOException {
        int type = read();
        if (type == 0)
            return null;
        if (type == 1)
            return read254String();
        return readString();
    }

    @Override
    public void writeSwimedString(String string) throws IOException {
        if (string == null) {
            writeByte(0);
            return;
        }
        if (string.getBytes("UTF8").length > 254) {
            writeByte(2);
            writeString(string);
        } else {
            writeByte(1);
            write254String(string);
        }
    }

    public byte[] readSwimedBytes() throws IOException {
        int type = read();
        if (type == 0)
            return null;
        if (type == 1)
            return read254Bytes();
        return readBytes();
    }

    private byte[] read254Bytes() throws IOException {
        int length = read();
        if (length == 255) {
            return null;
        }
        byte[] bs = new byte[length];
        readFully(bs);
        return bs;
    }

    private byte[] readBytes() throws IOException {
        int length = readInt();
        if (length < 0)
            return null;
        byte[] bs = new byte[length];
        readFully(bs);
        return bs;
    }

    public void writeSwimedBytes(byte[] bs) throws IOException {
        if (bs == null)
            writeByte(0);
        if (bs.length > 254) {
            writeByte(2);
            writeBytes(bs);
        } else {
            writeByte(1);
            write254Bytes(bs);
        }
    }

    private void writeBytes(byte[] bs) throws IOException {
        if (bs == null) {
            writeInt(-1);
        } else {
            writeInt(bs.length);
            write(bs);
        }
    }

    private void write254Bytes(byte[] bs) throws IOException {
        if (bs == null) {
            write(255);
        } else {
            int length = bs.length;
            if (length > 254) {
                throw new IOException(
                        MessageFormat
                                .format(
                                        "Bytes can not be written, its length {0} bytes (more then 254 bytes)",
                                        length));
            }
            write(length);
            write(bs);
        }
    }

    @Override
    public byte[] readBinary() throws IOException {
        return readSwimedBytes();
    }

    @Override
    public void writeBinary(byte[] bs) throws IOException {
        writeSwimedBytes(bs);
    }

}
