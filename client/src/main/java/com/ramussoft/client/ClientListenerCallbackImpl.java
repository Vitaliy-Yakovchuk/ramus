package com.ramussoft.client;

import java.rmi.RemoteException;

import com.ramussoft.net.common.tcp.EvenstHolder;

public class ClientListenerCallbackImpl implements ClientListenerCallback {

    private ClientListenerCallback deligate;

    public ClientListenerCallbackImpl(ClientListenerCallback deligate) {
        this.deligate = deligate;
    }

    @Override
    public void call(EvenstHolder holder) throws RemoteException {
        deligate.call(holder);
    }

}
