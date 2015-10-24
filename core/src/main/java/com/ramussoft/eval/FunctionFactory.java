package com.ramussoft.eval;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

public class FunctionFactory {

    private static Hashtable<String, Function> functions = new Hashtable<String, Function>();

    static {
        addFunctionClass(StandardFunctions.class);
        addNativeClass(Math.class);
    }

    public static Function getFunction(String functionName) {
        return functions.get(functionName);
    }

    private static void addNativeClass(Class<Math> clazz) {
        Method[] methods = clazz.getMethods();
        for (final Method m : methods) {
            final String name = m.getName().toUpperCase();
            if (functions.get(name) == null) {
                if ((Modifier.isStatic(m.getModifiers()))) {
                    if (!m.getReturnType().equals(void.class)) {
                        if (isDoubleParameters(m)) {
                            Function function = new Function() {
                                @Override
                                public EObject calculate(EObject[] params) {
                                    try {
                                        if (params.length != m
                                                .getParameterTypes().length)
                                            throw new RuntimeException(
                                                    "Method "
                                                            + name
                                                            + " can have "
                                                            + m
                                                            .getParameterTypes().length
                                                            + " parameter(s), got "
                                                            + params.length);
                                        Object[] objects = new Object[params.length];
                                        for (int i = 0; i < objects.length; i++)
                                            objects[i] = params[i]
                                                    .doubleValue();

                                        return new EObject(m.invoke(null,
                                                objects));
                                    } catch (Exception e) {
                                        throw new RuntimeException();
                                    }
                                }
                            };
                            functions.put(name, function);
                        }
                    }
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private static boolean isDoubleParameters(Method m) {
        for (Class clazz : m.getParameterTypes()) {
            if (!clazz.equals(double.class))
                return false;
        }
        return true;
    }

    private static void addFunctionClass(Class<StandardFunctions> clazz) {
        Method[] methods = clazz.getMethods();
        for (final Method m : methods) {
            if (m.getReturnType().equals(EObject.class)) {
                Function function = new Function() {
                    @Override
                    public EObject calculate(EObject[] params) {
                        try {
                            return (EObject) m.invoke(null,
                                    new Object[]{params});
                        } catch (Exception e) {
                            throw new RuntimeException();
                        }
                    }
                };
                functions.put(m.getName(), function);
            }
        }
    }
}
