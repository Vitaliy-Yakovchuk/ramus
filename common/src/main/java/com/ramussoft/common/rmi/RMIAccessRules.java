package com.ramussoft.common.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ramussoft.common.DeleteStatusList;

public interface RMIAccessRules extends Remote {

    public boolean canCreateAttribute() throws RemoteException;

    public boolean canCreateElement(long qualifierId) throws RemoteException;

    public boolean canCreateQualifier() throws RemoteException;

    public boolean canDeleteAttribute(long attributeId) throws RemoteException;

    public boolean canDeleteElements(long[] elementIds) throws RemoteException;

    public boolean canDeleteQualifier(long qualifierId) throws RemoteException;

    public boolean canReadAttribute(long qualifierId, long attributeId)
            throws RemoteException;

    public boolean canReadElement(long elementId) throws RemoteException;

    public boolean canReadElement(long elementId, long attributeId)
            throws RemoteException;

    public boolean canReadQualifier(long qualifierId) throws RemoteException;

    public boolean canUpdateAttribute(long attribueId) throws RemoteException;

    public boolean canUpdateAttribute(long qualifierId, long attributeId)
            throws RemoteException;

    public boolean canUpdateElement(long elementId, long attributeId)
            throws RemoteException;

    public boolean canUpdateQualifier(long qualifierId) throws RemoteException;

    public DeleteStatusList getElementsDeleteStatusList(long[] elementIds)
            throws RemoteException;

    public boolean canUpdateStream(String path) throws RemoteException;

    public boolean canCreateScript() throws RemoteException;

}
