package com.ramussoft.common.journal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.cached.CachedEngine;

public class SuperEngineFactory {

    private static class Caller implements InvocationHandler {

        private SuperInvoker invoker;

        public Caller(SuperInvoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {

                Object res = invoker.invoke(method.getName(), args);

                return res;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } finally {
                if ("close".equals(method.getName())) {
                    invoker = null;
                }
            }
        }

    }

    ;

    private static class CallerWithImpl implements InvocationHandler {

        private SuperInvoker invoker;

        private IEngine impl;

        public CallerWithImpl(SuperInvoker invoker, IEngine impl) {
            this.invoker = invoker;
            this.impl = impl;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                if (method.getName().equals("getDeligate"))
                    return impl;

                Object res = invoker.invoke(method.getName(), args);

                return res;
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } finally {
                if ("close".equals(method.getName())) {
                    invoker = null;
                }
            }
        }

    }

    ;

    @SuppressWarnings("unchecked")
    public static Object createTransactionalEngine(Engine engine,
                                                   Journaled journal) {

        String[] classes = engine.getAllImplementationClasseNames();

        Class[] clazzes = new Class[classes.length];
        for (int i = 0; i < classes.length; i++) {
            try {
                clazzes[i] = Class.forName(classes[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        SuperInvoker invoker = null;

        if (engine instanceof AbstractJournaledEngine) {
            AbstractJournaledEngine abstractJournaledEngine = (AbstractJournaledEngine) engine;
            invoker = abstractJournaledEngine.createSuperInvoker(classes,
                    clazzes, journal);
        } else if (engine instanceof CachedEngine) {
            Object[] objects = new Object[classes.length];
            Engine engine2 = ((CachedEngine) engine).getSource();
            AbstractJournaledEngine abstractJournaledEngine = null;
            if (engine2 instanceof AbstractJournaledEngine) {
                abstractJournaledEngine = (AbstractJournaledEngine) engine2;
                invoker = abstractJournaledEngine.createSuperInvoker(classes,
                        clazzes, journal);
            }
            for (int i = 0; i < objects.length; i++) {
                String className = classes[i];
                if (className.equals(Engine.class.getName())) {
                    objects[i] = engine;
                } else if (className.equals(Journaled.class.getName())) {
                    objects[i] = new UserTransactional(journal);
                } else {
                    for (Plugin plugin : abstractJournaledEngine.pluginFactory
                            .getPlugins()) {
                        Class class1 = plugin.getFunctionalInterface();
                        if (class1 != null)
                            if (class1.equals(clazzes[i])) {
                                objects[i] = plugin
                                        .createFunctionalInterfaceObject(
                                                engine, engine.getDeligate());
                            }
                    }
                }
            }
            invoker = new SuperInvoker(classes, clazzes, objects);
        }

        return Proxy.newProxyInstance(
                SuperEngineFactory.class.getClassLoader(), clazzes, new Caller(
                        invoker));
    }

    @SuppressWarnings("unchecked")
    public static Class[] getImplementations(Engine engine) {
        String[] classes = engine.getAllImplementationClasseNames();
        Class[] clazzes = new Class[classes.length];
        for (int i = 0; i < classes.length; i++) {
            try {
                clazzes[i] = Class.forName(classes[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return clazzes;
    }

    @SuppressWarnings("unchecked")
    public static Object createTransactionalEngine(Engine engine,
                                                   Journaled journal, IEngine iEngine, PluginFactory pluginFactory) {

        String[] classes = engine.getAllImplementationClasseNames();

        Class[] clazzes = new Class[classes.length];
        for (int i = 0; i < classes.length; i++) {
            try {
                clazzes[i] = Class.forName(classes[i]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        SuperInvoker invoker = null;

        if (engine instanceof AbstractJournaledEngine) {
            AbstractJournaledEngine abstractJournaledEngine = (AbstractJournaledEngine) engine;
            invoker = abstractJournaledEngine.createSuperInvoker(classes,
                    clazzes, journal);
        } else {
            Object[] objects = new Object[classes.length];
            for (int i = 0; i < objects.length; i++) {
                String className = classes[i];
                if (className.equals(Engine.class.getName())) {
                    objects[i] = engine;
                } else if (className.equals(Journaled.class.getName())) {
                    objects[i] = new UserTransactional(journal);
                } else {
                    for (Plugin plugin : pluginFactory.getPlugins()) {
                        Class class1 = plugin.getFunctionalInterface();
                        if (class1 != null)
                            if (class1.equals(clazzes[i])) {
                                objects[i] = plugin
                                        .createFunctionalInterfaceObject(
                                                engine, iEngine);
                            }
                    }
                }
            }
            invoker = new SuperInvoker(classes, clazzes, objects);
        }

        return Proxy.newProxyInstance(
                SuperEngineFactory.class.getClassLoader(), clazzes,
                new CallerWithImpl(invoker, iEngine));
    }

}
