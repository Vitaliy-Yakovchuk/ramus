package com.ramussoft.net.common.tcp;

import java.io.IOException;
import java.io.OutputStream;

public class TcpOutputStream extends OutputStream {

    private long count;

    private final OutputStream deligate;

    public TcpOutputStream(OutputStream deligate) {
        this.deligate = deligate;
    }

    @Override
    public void write(int b) throws IOException {
        deligate.write(b);
        count++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        deligate.write(b);
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        deligate.write(b, off, len);
        count += len;
    }

    public long getCount() {
        return count;
    }

    @Override
    public void flush() throws IOException {
        deligate.flush();
    }

    @Override
    public void close() throws IOException {
        deligate.close();
    }

}
