package com.ramussoft.client;

import java.rmi.RemoteException;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.DeleteStatusList;
import com.ramussoft.common.rmi.RMIAccessRules;

import static com.ramussoft.client.TcpClientEngine.remoteException;

public class RMIClientAccessRules implements AccessRules {

    private final RMIAccessRules deligate;

    public RMIClientAccessRules(RMIAccessRules accessRules) {
        this.deligate = accessRules;
    }

    @Override
    public boolean canCreateAttribute() {
        try {
            return deligate.canCreateAttribute();
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canCreateElement(long qualifierId) {
        try {
            return deligate.canCreateElement(qualifierId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canCreateQualifier() {
        try {
            return deligate.canCreateQualifier();
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canDeleteAttribute(long attributeId) {
        try {
            return deligate.canDeleteAttribute(attributeId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canDeleteElements(long[] elementIds) {
        try {
            return deligate.canDeleteElements(elementIds);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canDeleteQualifier(long qualifierId) {
        try {
            return deligate.canDeleteQualifier(qualifierId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canReadAttribute(long qualifierId, long attributeId) {
        try {
            return deligate.canReadAttribute(qualifierId, attributeId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canReadElement(long elementId) {
        try {
            return deligate.canReadElement(elementId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canReadElement(long elementId, long attributeId) {
        try {
            return deligate.canReadElement(elementId, attributeId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canReadQualifier(long qualifierId) {
        try {
            return deligate.canReadQualifier(qualifierId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canUpdateAttribute(long attribueId) {
        try {
            return deligate.canUpdateAttribute(attribueId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canUpdateAttribute(long qualifierId, long attributeId) {
        try {
            return deligate.canUpdateAttribute(qualifierId, attributeId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canUpdateElement(long elementId, long attributeId) {
        try {
            return deligate.canUpdateElement(elementId, attributeId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canUpdateQualifier(long qualifierId) {
        try {
            return deligate.canUpdateQualifier(qualifierId);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public DeleteStatusList getElementsDeleteStatusList(long[] elementIds) {
        try {
            return deligate.getElementsDeleteStatusList(elementIds);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canUpdateStream(String path) {
        try {
            return deligate.canUpdateStream(path);
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    @Override
    public boolean canCreateStript() {
        try {
            return deligate.canCreateScript();
        } catch (RemoteException e) {
            remoteException(e);
            e.printStackTrace();
            throw new ClientException(e);
        }
    }
}