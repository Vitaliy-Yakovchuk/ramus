package com.ramussoft.net.common.tcp;

import java.lang.reflect.Method;
import java.util.Hashtable;

public class InvocableFactory {

    private Hashtable<MethodHolder, Method> methods = new Hashtable<MethodHolder, Method>();

    @SuppressWarnings("unchecked")
    public InvocableFactory(String[] classNames) {
        for (String className : classNames) {
            try {
                Class class1 = Class.forName(className);
                for (Method method : class1.getMethods()) {
                    MethodHolder holder = new MethodHolder(method.getName(),
                            method.getParameterTypes().length);
                    methods.put(holder, method);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Object invoke(Object obj, String methodName, Object[] args)
            throws Exception {
        Method method = methods.get(new MethodHolder(methodName,
                (args == null) ? 0 : args.length));
        return method.invoke(obj, args);
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
