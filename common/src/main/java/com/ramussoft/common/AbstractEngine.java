package com.ramussoft.common;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Properties;

import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.BranchListener;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.FormulaEvent;
import com.ramussoft.common.event.FormulaListener;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.event.StreamListener;

public abstract class AbstractEngine implements Engine {

    protected AttributeListener[] attributeListeners = new AttributeListener[]{};

    private ElementListener[] nullElementListeners = new ElementListener[]{};

    private Hashtable<Long, ElementListener[]> elementListeners = new Hashtable<Long, ElementListener[]>();

    private ElementAttributeListener[] nullElementAttributeListeners = new ElementAttributeListener[]{};

    private Hashtable<Long, ElementAttributeListener[]> elementAttributeListeners = new Hashtable<Long, ElementAttributeListener[]>();

    protected StreamListener[] streamListeners = new StreamListener[]{};

    protected QualifierListener[] qualifierListeners = new QualifierListener[]{};

    protected FormulaListener[] formulaListeners = new FormulaListener[]{};

    protected BranchListener[] branchListeners = new BranchListener[]{};

    protected Hashtable<String, Object> pluginProperties = new Hashtable<String, Object>();

    public void attributeChanged(AttributeEvent event) {
        if (event.getElement() == null) {
            for (ElementAttributeListener listener : nullElementAttributeListeners) {
                listener.attributeChanged(event);
            }
            return;
        }

        long qualifierId = event.getElement().getQualifierId();
        ElementAttributeListener[] listeners = elementAttributeListeners
                .get(qualifierId);
        if (listeners != null)
            for (ElementAttributeListener listener : listeners) {
                listener.attributeChanged(event);
            }
        for (ElementAttributeListener listener : nullElementAttributeListeners) {
            listener.attributeChanged(event);
        }
    }

    @Override
    public synchronized void addAttributeListener(AttributeListener listener) {
        attributeListeners = addListener(attributeListeners, listener);
    }

    @Override
    public synchronized void addElementListener(Qualifier qualifier,
                                                ElementListener listener) {
        if (qualifier == null) {
            nullElementListeners = addListener(nullElementListeners, listener);
        } else {
            ElementListener[] listeners = elementListeners.get(qualifier
                    .getId());
            if (listeners == null) {
                listeners = new ElementListener[]{listener};
            } else {
                listeners = addListener(listeners, listener);
            }
            elementListeners.put(qualifier.getId(), listeners);
        }
    }

    @Override
    public synchronized void addQualifierListener(QualifierListener listener) {
        qualifierListeners = addListener(qualifierListeners, listener);
    }

    @Override
    public synchronized AttributeListener[] getAttributeListeners() {
        return Arrays.copyOf(attributeListeners, attributeListeners.length);
    }

    @Override
    public synchronized ElementListener[] getElementListeners(
            Qualifier qualifier) {
        if (qualifier == null)
            return Arrays.copyOf(nullElementListeners,
                    nullElementListeners.length);

        ElementListener[] listeners = elementListeners.get(qualifier.getId());
        if (listeners == null)
            return new ElementListener[]{};
        return Arrays.copyOf(listeners, listeners.length);
    }

    @Override
    public synchronized QualifierListener[] getQualifierListeners() {
        return Arrays.copyOf(qualifierListeners, qualifierListeners.length);
    }

    @Override
    public synchronized void removeAttributeListener(AttributeListener listener) {
        attributeListeners = removeListener(attributeListeners, listener);
    }

    @Override
    public synchronized void removeElementListener(Qualifier qualifier,
                                                   ElementListener listener) {
        if (qualifier == null) {
            nullElementListeners = removeListener(nullElementListeners,
                    listener);
        } else {
            ElementListener[] listeners = elementListeners.get(qualifier
                    .getId());
            if (listeners == null)
                listeners = new ElementListener[]{};
            listeners = removeListener(listeners, listener);
            elementListeners.put(qualifier.getId(), listeners);
        }
    }

    @Override
    public synchronized void removeQualifierListener(QualifierListener listener) {
        qualifierListeners = removeListener(qualifierListeners, listener);
    }

    public static <T> T[] addListener(T[] original, T object) {
        T[] res = Arrays.copyOf(original, original.length + 1);
        res[original.length] = object;
        return res;
    }

    public static <T> T[] removeListener(T[] original, T object) {
        for (int i = 0; i < original.length; i++) {
            if (original[i] == object) {
                T[] res = Arrays.copyOf(original, original.length - 1);
                int k = 0;
                for (int j = 0; j < original.length; j++) {
                    if (j != i) {
                        res[k] = original[j];
                        k++;
                    }
                }
                return res;
            }
        }
        throw new RuntimeException("Listener not found and can not be removed");
    }

    @Override
    public synchronized void addElementAttributeListener(Qualifier qualifier,
                                                         ElementAttributeListener listener) {
        if (qualifier == null)
            nullElementAttributeListeners = addListener(
                    nullElementAttributeListeners, listener);
        else {
            ElementAttributeListener[] listeners = elementAttributeListeners
                    .get(qualifier.getId());
            if (listeners == null) {
                listeners = new ElementAttributeListener[]{listener};
            } else {
                listeners = addListener(listeners, listener);
            }
            elementAttributeListeners.put(qualifier.getId(), listeners);
        }
    }

    @Override
    public synchronized ElementAttributeListener[] getElementAttributeListeners(
            long qualifierId) {
        if (qualifierId == -1l)
            return Arrays.copyOf(nullElementAttributeListeners,
                    nullElementAttributeListeners.length);

        ElementAttributeListener[] listeners = elementAttributeListeners
                .get(qualifierId);
        if (listeners == null)
            return new ElementAttributeListener[]{};
        return Arrays.copyOf(listeners, listeners.length);

    }

    @Override
    public synchronized void removeElementAttributeListener(
            Qualifier qualifier, ElementAttributeListener listener) {
        if (qualifier == null) {
            nullElementAttributeListeners = removeListener(
                    nullElementAttributeListeners, listener);
        } else {
            ElementAttributeListener[] listeners = elementAttributeListeners
                    .get(qualifier.getId());
            if (listeners == null)
                listeners = new ElementAttributeListener[]{};
            listeners = removeListener(listeners, listener);
            elementAttributeListeners.put(qualifier.getId(), listeners);
        }
    }

    @Override
    public void setPluginProperty(String pluginName, String key, Object value) {
        String key2 = "/" + pluginName + "/" + key;
        if (value == null)
            pluginProperties.remove(key2);
        else
            pluginProperties.put(key2, value);
    }

    @Override
    public Object getPluginProperty(String pluginName, String key) {
        return pluginProperties.get("/" + pluginName + "/" + key);
    }

    @Override
    public void addStreamListener(StreamListener listener) {
        streamListeners = addListener(streamListeners, listener);
    }

    @Override
    public void removeStreamListener(StreamListener listener) {
        streamListeners = removeListener(streamListeners, listener);
    }

    @Override
    public StreamListener[] getStreamListeners() {
        return streamListeners;
    }

    @Override
    public void addFormulaListener(FormulaListener listener) {
        formulaListeners = addListener(formulaListeners, listener);
    }

    @Override
    public void removeFormulaListener(FormulaListener listener) {
        formulaListeners = removeListener(formulaListeners, listener);
    }

    @Override
    public FormulaListener[] getFormulaListeners() {
        return Arrays.copyOf(formulaListeners, formulaListeners.length);
    }

    @Override
    public Properties getProperties(String path) {
        InputStream is = getInputStream(path);
        Properties res = new Properties();
        if (is != null) {
            try {
                res.loadFromXML(is);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return res;
    }

    @Override
    public InputStream getInputStream(String path) {
        byte[] bs = getStream(path);
        if (bs == null)
            return null;
        return new ByteArrayInputStream(bs);
    }

    @Override
    public OutputStream getOutputStream(final String path) {
        return new ByteArrayOutputStream() {
            boolean closed = false;

            @Override
            public void close() throws IOException {
                super.close();
                if (!closed) {
                    setStream(path, toByteArray());
                    closed = true;
                }
            }
        };
    }

    @Override
    public void setProperties(String path, Properties properties) {
        OutputStream out = getOutputStream(path);
        try {
            properties.storeToXML(out, "Path: " + path);
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void elementCreated(ElementEvent event, ElementListener[] listeners) {
        if (listeners == null)
            return;
        for (ElementListener listener : listeners)
            listener.elementCreated(event);
    }

    private void elementDeleted(ElementEvent event, ElementListener[] listeners) {
        if (listeners == null)
            return;
        for (ElementListener listener : listeners)
            listener.elementDeleted(event);
    }

    private void beforeElementDeleted(ElementEvent event,
                                      ElementListener[] listeners) {
        if (listeners == null)
            return;
        for (ElementListener listener : listeners)
            listener.beforeElementDeleted(event);
    }

    public void elementCreated(ElementEvent event) {
        long qualifierId = event.getNewElement().getQualifierId();

        elementCreated(event, nullElementListeners);
        elementCreated(event, elementListeners.get(qualifierId));
    }

    public void beforeElementDeleted(ElementEvent event) {
        Element oldElement = event.getOldElement();
        if (oldElement == null)
            return;
        long qualifierId = oldElement.getQualifierId();

        beforeElementDeleted(event, nullElementListeners);
        beforeElementDeleted(event, elementListeners.get(qualifierId));
    }

    public void elementDeleted(ElementEvent event) {
        Element oldElement = event.getOldElement();
        if (oldElement == null)
            return;
        long qualifierId = oldElement.getQualifierId();

        elementDeleted(event, nullElementListeners);
        elementDeleted(event, elementListeners.get(qualifierId));
    }

    public void formulaChanged(FormulaEvent event) {
        for (FormulaListener listener : formulaListeners)
            listener.formulaChanged(event);
    }

    @Override
    public void addBranchListener(BranchListener branchListener) {
        this.branchListeners = addListener(this.branchListeners, branchListener);
    }

    @Override
    public void removeBranchListener(BranchListener branchListener) {
        this.branchListeners = removeListener(this.branchListeners,
                branchListener);
    }

    @Override
    public BranchListener[] getBranchListeners() {
        return Arrays.copyOf(branchListeners, branchListeners.length);
    }

}
