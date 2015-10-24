package com.ramussoft.common.journal;

import java.lang.reflect.Method;
import java.util.Hashtable;

public class SuperInvoker {

    private Hashtable<MethodHolder, Method> methods = new Hashtable<MethodHolder, Method>();

    private Hashtable<MethodHolder, Object> objects = new Hashtable<MethodHolder, Object>();

    @SuppressWarnings("unchecked")
    public SuperInvoker(String[] classNames, Class[] classes, Object[] objects) {
        for (int i = 0; i < classNames.length; i++) {
            Class class1 = classes[i];
            if (class1 != null)
                for (Method method : class1.getMethods()) {
                    MethodHolder holder = new MethodHolder(method.getName(),
                            method.getParameterTypes().length);
                    methods.put(holder, method);
                    if (objects[i] != null)
                        this.objects.put(holder, objects[i]);
                    else
                        System.err.println("Object for " + class1.getName()
                                + " not found!");
                }

        }
    }

    public Object invoke(String methodName, Object[] args) throws Exception {
        MethodHolder holder = new MethodHolder(methodName, (args == null) ? 0
                : args.length);
        Method method = methods.get(holder);
        return method.invoke(objects.get(holder), args);
    }

    private class MethodHolder {

        private String methodName;

        private int paramCount;

        public MethodHolder(String methodName, int paramCount) {
            this.methodName = methodName;
            this.paramCount = paramCount;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + methodName.hashCode();
            result = prime * result + paramCount;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            MethodHolder other = (MethodHolder) obj;
            if (!methodName.equals(other.methodName))
                return false;
            if (paramCount != other.paramCount)
                return false;
            return true;
        }
    }
}
