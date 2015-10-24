package com.ramussoft.net.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;

public class SuperInvoker implements InvocationHandler {

    @SuppressWarnings("unchecked")
    private Class[] classes;

    private Hashtable<Method, Object> objects = new Hashtable<Method, Object>();

    @SuppressWarnings("unchecked")
    public SuperInvoker(Object[] objects, Class[] classes) {
        this.classes = classes;
        for (int i = 0; i < classes.length; i++) {
            Class clazz = classes[i];
            for (Method method : clazz.getMethods()) {
                this.objects.put(method, objects[i]);
            }
        }
    }

    public Object createProxy() {
        return Proxy.newProxyInstance(getClass().getClassLoader(), classes,
                this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        return method.invoke(objects.get(method), args);
    }

}
