package com.ramussoft.server;

import java.util.Hashtable;
import java.util.Vector;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.DeleteStatusList;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;

public class ServerAccessRules implements AccessRules {

    private UserFactory userFactory;

    private Hashtable<String, User> users = new Hashtable<String, User>();

    private Object lock = new Object();

    private IEngine engine;

    private Thread clearUsersCache;

    public ServerAccessRules(IEngine engine, UserFactory userFactory) {
        this(engine, userFactory, true);
    }

    public ServerAccessRules(IEngine engine, UserFactory userFactory,
                             boolean createCache) {
        this.engine = engine;
        this.userFactory = userFactory;
        if (createCache) {
            clearUsersCache = new Thread("Clear users cache") {

                {
                    setPriority(MIN_PRIORITY);
                }

                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (lock) {
                            users.clear();
                        }
                    }
                }
            };
            clearUsersCache.start();
        }
    }

    private SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }

    public User getUser() {
        synchronized (lock) {
            String login = getLogin();
            User user = users.get(login);
            if (user != null)
                return user;
            user = userFactory.getUser(login);
            if (user != null)
                users.put(login, user);
            return user;
        }
    }

    protected String getLogin() {
        SecurityContext context = getSecurityContext();
        String login = context.getAuthentication().getName();
        return login;
    }

    private boolean isAdmin() {
        User user = getUser();
        for (Group group : user.getGroups())
            if (group.getName().equals("admin"))
                return true;
        return false;
    }

    private boolean isHasAccess(long qualifierId) {

        boolean admin = isAdmin();

        if (admin)
            return true;

        if (qualifierId == -1)
            return false;

        User user = getUser();
        for (Group group : user.getGroups())
            if (group.getName().equals("admin"))
                return true;
            else {
                for (Long long1 : group.getQualifierIds())
                    if (long1.longValue() == qualifierId)
                        return true;
            }

        Qualifier qualifier = engine.getQualifier(qualifierId);

        if (qualifier == null)
            return admin;

        if (qualifier.isSystem()) {

            if (qualifier.getName().equals(
                    StandardAttributesPlugin.QUALIFIERS_QUALIFIER))
                return admin;

            return true;
        }

        return false;
    }

    @Override
    public boolean canCreateAttribute() {
        return isAdmin();
    }

    @Override
    public boolean canCreateElement(long qualifierId) {
        return isHasAccess(qualifierId);
    }

    @Override
    public boolean canCreateQualifier() {
        return isAdmin();
    }

    @Override
    public boolean canDeleteAttribute(long attributeId) {
        return isAdmin();
    }

    @Override
    public boolean canDeleteElements(long[] elementIds) {
        Vector<Long> vector = new Vector<Long>();
        for (long elementId : elementIds) {
            Long qualifierId = engine.getQualifierIdForElement(elementId);
            if (vector.indexOf(qualifierId) < 0) {
                if (!isHasAccess(qualifierId.longValue()))
                    return false;
            }
            vector.add(qualifierId);
        }
        return true;
    }

    @Override
    public boolean canDeleteQualifier(long qualifierId) {
        boolean admin = isAdmin();
        if (admin)
            return true;
        Qualifier q = engine.getQualifier(qualifierId);
        if (q == null)
            return false;
        if (q.isSystem())
            return true;
        return admin;
    }

    @Override
    public boolean canReadAttribute(long qualifierId, long attributeId) {
        return true;
    }

    @Override
    public boolean canReadElement(long elementId) {
        return true;
    }

    @Override
    public boolean canReadElement(long elementId, long attributeId) {
        return true;
    }

    @Override
    public boolean canReadQualifier(long qualifierId) {
        return true;
    }

    @Override
    public boolean canUpdateAttribute(long attribueId) {
        return isAdmin();
    }

    @Override
    public boolean canUpdateAttribute(long qualifierId, long attributeId) {
        return isHasAccess(qualifierId);
    }

    @Override
    public boolean canUpdateElement(long elementId, long attributeId) {
        return isHasAccess(engine.getQualifierIdForElement(elementId));
    }

    @Override
    public boolean canUpdateQualifier(long qualifierId) {
        return isHasAccess(qualifierId);
    }

    @Override
    public DeleteStatusList getElementsDeleteStatusList(long[] elementIds) {
        return new DeleteStatusList();
    }

    @Override
    public boolean canUpdateStream(String path) {
        if (path.startsWith("/script/"))
            return isAdmin();
        return true;
    }

    @Override
    public boolean canCreateStript() {
        return isAdmin();
    }

}
