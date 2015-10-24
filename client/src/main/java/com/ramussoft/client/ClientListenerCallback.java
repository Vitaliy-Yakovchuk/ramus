package com.ramussoft.client;

import java.rmi.RemoteException;

import com.ramussoft.net.common.tcp.EvenstHolder;

public interface ClientListenerCallback {

    void call(EvenstHolder holder) throws RemoteException;

}
