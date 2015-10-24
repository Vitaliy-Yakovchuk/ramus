package com.ramussoft.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.LinkedList;

import com.ramussoft.common.Metadata;
import com.ramussoft.net.common.tcp.CallParameters;
import com.ramussoft.net.common.tcp.InvocableFactory;
import com.ramussoft.net.common.tcp.Result;

public class TcpServerConnection implements Runnable {

    private ObjectInputStream inputStream;

    private ObjectOutputStream outputStream;

    private Socket socket;

    private InvocableFactory factory;

    private Object proxy;

    private boolean closed = false;

    private Object outputLock = new Object();

    private Object outputWaitLock = new Object();

    private LinkedList<Object> outputs = new LinkedList<Object>();

    private Thread outputThread = new Thread("client-results") {
        public void run() {

            while (true) {

                Object object;
                synchronized (outputs) {
                    if (outputs.isEmpty())
                        object = null;
                    else
                        object = outputs.removeLast();
                }

                if (object != null) {
                    try {
                        writeObject(object);
                    } catch (IOException e) {
                        e.printStackTrace();
                        silentClose();
                    }
                } else {
                    synchronized (outputWaitLock) {
                        boolean w;
                        synchronized (outputs) {
                            w = outputs.isEmpty();
                        }
                        try {
                            if (w)
                                outputWaitLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (closed)
                    return;
            }
        }

        ;
    };

    public TcpServerConnection(Socket socket, String[] classes, Object proxy)
            throws IOException {
        this.socket = socket;
        BufferedOutputStream stream = new BufferedOutputStream(socket
                .getOutputStream(), 512 * 1024);
        outputStream = new ObjectOutputStream(stream);
        outputStream
                .writeObject("Version: " + Metadata.getApplicationVersion());
        outputStream.flush();
        outputStream.reset();
        inputStream = new ObjectInputStream(socket.getInputStream());

        factory = new InvocableFactory(classes);
        this.proxy = proxy;
    }

    @Override
    public void run() {
        outputThread.setPriority(Thread.MIN_PRIORITY);

        outputThread.start();

        while (true) {
            Result result = new Result();
            try {
                try {
                    CallParameters p = (CallParameters) inputStream
                            .readObject();
                    try {
                        result.result = factory.invoke(proxy, p.methodName,
                                p.parameters);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    silentClose();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    silentClose();
                } catch (InvocationTargetException e) {
                    Throwable throwable = e.getTargetException();
                    if (throwable instanceof Exception)
                        result.exception = (Exception) throwable;
                    else
                        result.exception = e;
                }
            } catch (Exception e) {
                result.exception = e;
            }
            if (closed)
                return;
            try {
                writeObject(result);
            } catch (IOException e) {
                e.printStackTrace();
                silentClose();
            }
        }
    }

    private void silentClose() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(Object object) throws IOException {
        synchronized (outputLock) {
            outputStream.writeUnshared(object);
            outputStream.flush();
            outputStream.reset();
        }
    }

    public void writeAsynchrone(Object object) {
        synchronized (outputWaitLock) {
            synchronized (outputs) {
                outputs.addFirst(object);
            }
        }

        synchronized (outputWaitLock) {
            outputWaitLock.notifyAll();
        }
    }

    public void close() throws IOException {
        this.closed = true;
        synchronized (outputWaitLock) {
            outputWaitLock.notifyAll();
        }
        socket.close();
    }

}
