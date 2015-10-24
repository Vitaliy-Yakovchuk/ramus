package com.ramussoft.net.common.tcp;

import java.io.IOException;
import java.io.InputStream;

public class TcpInputStream extends InputStream {

    private final InputStream deligate;

    private long count;

    public TcpInputStream(InputStream deligate) {
        this.deligate = deligate;
    }

    @Override
    public int read() throws IOException {
        int read = deligate.read();
        if (read >= 0)
            count++;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = deligate.read(b);
        count += read;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = deligate.read(b, off, len);
        count += read;
        return read;
    }

    @Override
    public int available() throws IOException {
        return deligate.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        deligate.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return deligate.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        deligate.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return deligate.skip(n);
    }

    public long getCount() {
        return count;
    }

    @Override
    public void close() throws IOException {
        deligate.close();
    }
}
