/**
 *
 */
package com.ramussoft.client;

import java.rmi.RemoteException;

public class ClientException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 3081597794383080970L;

    public ClientException(RemoteException exception) {
        super(exception);
    }
}