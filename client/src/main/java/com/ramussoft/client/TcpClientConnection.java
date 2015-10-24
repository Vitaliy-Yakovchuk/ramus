package com.ramussoft.client;

import java.io.BufferedOutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
//import java.util.HashMap;

import com.ramussoft.net.common.tcp.CallParameters;
import com.ramussoft.net.common.tcp.Result;

public class TcpClientConnection implements Runnable {

    private String host;

    private int port;

    private Thread thread = new Thread(this, "client-connection");

    private ObjectOutputStream outputStream;

    private ObjectInputStream inputStream;

    private Socket socket;

    private Result result;

    private Object resultLock = new Object();

    private Object callLock = new Object();

    private boolean closed = false;

    // private HashMap<String, SimpleProfiler> methodProfiler = new
    // HashMap<String, TcpClientConnection.SimpleProfiler>();

    // private class SimpleProfiler {
    // String method;

    // long times;

    // long time;

    // public String toString() {
    // return method + ", times: " + times + " time: " + time;
    // }
    // }

    public TcpClientConnection(String aHost, int aPort) {
        this.host = aHost;
        this.port = aPort;
    }

    public void start() throws UnknownHostException, IOException,
            ClassNotFoundException {
        socket = new Socket(host, port);
        inputStream = new ObjectInputStream(socket.getInputStream());
        System.out.println("Server: " + inputStream.readObject());
        OutputStream stream = new BufferedOutputStream(
                socket.getOutputStream(), 64 * 1024);
        outputStream = new ObjectOutputStream(stream);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object object = inputStream.readObject();
                if (object instanceof Result) {
                    synchronized (resultLock) {
                        result = (Result) object;
                        resultLock.notifyAll();
                    }
                } else {
                    objectReaded(object);
                }
            }
        } catch (IOException e) {
            if (closed) {
                synchronized (resultLock) {
                    resultLock.notifyAll();
                }
                return;
            }
            e.printStackTrace();
            silentClose();
            String message = "Зв’язок з сервером перервався";
            showDialogEndExit(message);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            silentClose();
            String message = "Сталась критична помилка, дивіться відповідний log для деталей "
                    + e.getLocalizedMessage();
            showDialogEndExit(message);
        }
    }

    protected void showDialogEndExit(String message) {

    }

    protected void objectReaded(Object object) {
    }

    private void silentClose() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        setClosed(true);
        socket.close();
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Result call(CallParameters parameters) throws IOException {
        Result result;
        synchronized (callLock) {
            synchronized (resultLock) {
                outputStream.writeUnshared(parameters);
                outputStream.flush();
                outputStream.reset();
                try {
                    resultLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result = this.result;
            }
        }
        return result;
    }

    public Object invoke(String methodName, Object[] objects) throws Exception {
        // String m = methodName;
        // if(m.equals("invoke")){
        // CallParameters callParameters = (CallParameters)objects[0];
        // m = callParameters.getMethodName();
        // }
        // SimpleProfiler profiler = methodProfiler.get(m);
        // if (profiler == null) {
        // profiler = new SimpleProfiler();
        // profiler.method = m;
        // methodProfiler.put(m, profiler);
        // }
        // profiler.times++;
        // long st = System.currentTimeMillis();
        try {

            CallParameters parameters = new CallParameters(methodName, objects);
            Result result = call(parameters);
            if (result.exception != null)
                throw result.exception;
            return result.result;
        } finally {
            // long l = System.currentTimeMillis() - st;
            // profiler.time += l;
            // System.out.println(profiler);
            // System.out.println("This time: "+l);
        }
    }

}
