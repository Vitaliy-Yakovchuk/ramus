package com.ramussoft.server;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;
import com.ramussoft.net.common.UserProvider;

public class UserProviderImpl implements UserProvider {

    private UserFactory factory;

    @Override
    public void changePassword(String newPassword) {
        User me = me();
        me.setPassword(newPassword);
        factory.updateUser(me);
    }

    @Override
    public User me() {
        String login = getLogin();
        if (login == null)
            return null;
        return factory.getUser(login);
    }

    protected String getLogin() {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() == null)
            return null;
        return sc.getAuthentication().getName();
    }

    /**
     * @param factory the factory to set
     */
    public void setUserFactory(UserFactory factory) {
        this.factory = factory;
    }

}
