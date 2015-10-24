package com.ramussoft.server.tcp;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.net.common.tcp.CallParameters;
import com.ramussoft.net.common.tcp.CallResult;
import com.ramussoft.net.common.tcp.EngineInvocker;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.net.common.tcp.InvocableFactory;

public class EngineInvockerImpl implements EngineInvocker {

    private Engine engine;

    private EventsFactory factory;

    private InvocableFactory invocableFactory;

    public EngineInvockerImpl(Engine engine, EventsFactory factory) {
        this.engine = engine;
        this.factory = factory;
        this.invocableFactory = new InvocableFactory(engine
                .getAllImplementationClasseNames());
    }

    @Override
    public CallResult invoke(CallParameters parameters) throws RemoteException {
        synchronized (factory) {
            factory.setEventHolder(new EvenstHolder());
            CallResult result = new CallResult();
            try {
                try {
                    result.result = invocableFactory.invoke(engine,
                            parameters.methodName, parameters.parameters);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    throw new RemoteException();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RemoteException();
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
            EvenstHolder holder = factory.getEventHolder();
            result.holder = holder;
            if (holder.getEvents().size() > 0)
                processEndInvoke(holder);
            return result;
        }
    }

    protected void processEndInvoke(EvenstHolder holder) {
    }

    public Engine getEngine() {
        return engine;
    }

    public IEngine getDeligate() {
        return engine.getDeligate();
    }

}
