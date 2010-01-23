package org.googlecode.vkontakte_android.service;

import android.os.RemoteException;

public class MyRemoteException extends RemoteException {
    private static final long serialVersionUID = 1L;
    public Exception innerException;

    public MyRemoteException() {
    }

    public MyRemoteException(Exception innerException) {
        this.innerException = innerException;
    }
}
