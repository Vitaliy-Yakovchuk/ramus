package com.ramussoft.net.common.tcp;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EngineInvocker extends Remote {

    public CallResult invoke(CallParameters parameters) throws RemoteException;

}
