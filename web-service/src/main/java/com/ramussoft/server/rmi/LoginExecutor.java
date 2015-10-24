package com.ramussoft.server.rmi;

import java.lang.reflect.InvocationTargetException;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;

public class LoginExecutor extends DefaultRemoteInvocationExecutor {

    private DaoAuthenticationProvider daoAuthenticationProvider;

    @Override
    public Object invoke(RemoteInvocation invocation, Object arg1)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Object object = super.invoke(invocation, arg1);
        UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(
                invocation.getArguments()[0].toString(), invocation
                .getArguments()[1].toString());
        Authentication auth = daoAuthenticationProvider.authenticate(userToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return object;
    }

    /**
     * @param daoAuthenticationProvider the daoAuthenticationProvider to set
     */
    public void setDaoAuthenticationProvider(
            DaoAuthenticationProvider daoAuthenticationProvider) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
    }

    /**
     * @return the daoAuthenticationProvider
     */
    public DaoAuthenticationProvider getDaoAuthenticationProvider() {
        return daoAuthenticationProvider;
    }

}
