package com.ramussoft.common.event;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ramussoft.common.Engine;

public class Event implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2664932915565503943L;

    private transient Engine engine;

    private boolean journaled;

    public Event(Engine engine, boolean journaled) {
        this.engine = engine;
        this.journaled = journaled;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public boolean isJournaled() {
        return journaled;
    }

    @Override
    public String toString() {
        Method[] methods = this.getClass().getMethods();
        StringBuffer res = new StringBuffer();
        boolean first = true;

        for (Method m : methods)
            if ((m.getName().startsWith("get"))
                    && (m.getParameterTypes().length == 0)
                    && (!"getClass".equals(m.getName()))
                    && (!"getEngine".equals(m.getName()))) {
                if (!first)
                    res.append(", ");
                else
                    first = false;
                res.append(m.getName());
                res.append(" = ");
                try {
                    res.append(m.invoke(this, new Object[]{}));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

        return res.toString();
    }

    public void setJournaled(boolean journaled) {
        this.journaled = journaled;
    }
}
