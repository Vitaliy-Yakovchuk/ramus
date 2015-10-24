package com.ramussoft.client;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import com.ramussoft.common.AbstractEngine;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Branch;
import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.FindObject;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.BranchEvent;
import com.ramussoft.common.event.BranchListener;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.Event;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.event.StreamListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.event.JournalEvent;
import com.ramussoft.common.journal.event.JournalListener;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;
import com.ramussoft.net.common.tcp.CallParameters;
import com.ramussoft.net.common.tcp.CallResult;
import com.ramussoft.net.common.tcp.EngineInvocker;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.net.common.tcp.EventHolder;

public class TcpClientEngine extends AbstractEngine implements Journaled,
        InvocationHandler, ClientListenerCallback {

    private EngineInvocker invocker;

    private Hashtable<MethodHolder, Object> hashtable = new Hashtable<MethodHolder, Object>();

    private Hashtable<String, Hashtable<String, Method>> methodListeners = new Hashtable<String, Hashtable<String, Method>>();

    private Hashtable<String, ArrayGetCallback> classListeners = new Hashtable<String, ArrayGetCallback>();

    private EventListenerList journalListenerList = new EventListenerList();

    private EventListenerList branchListenerList = new EventListenerList();

    private Hashtable<Long, Long> qualifirsForElements = new Hashtable<Long, Long>();

    private Engine engine;

    private TcpClientConnection connection;

    public TcpClientEngine(EngineInvocker invocker,
                           TcpClientConnection connection) {
        this.invocker = invocker;
        this.connection = connection;
        try {
            putMethod(
                    getClass().getMethod("addJournalListener",
                            JournalListener.class), this);
            putMethod(
                    getClass().getMethod("removeJournalListener",
                            JournalListener.class), this);
            putMethod(getClass().getMethod("getJournalListeners"), this);
            putMethod(getClass().getMethod("getDeligate"), this);

            List<String> methods = new ArrayList<String>();

            methods.add("addAttributeListener");

            methods.add("addElementAttributeListener");

            methods.add("addElementListener");

            methods.add("addFormulaListener");

            methods.add("addQualifierListener");

            methods.add("addStreamListener");

            methods.add("getAttributeListeners");

            methods.add("getElementAttributeListeners");

            methods.add("getElementListeners");

            methods.add("getFormulaListeners");

            methods.add("getInputStream");

            methods.add("getOutputStream");

            methods.add("getPluginProperty");

            methods.add("getProperties");

            methods.add("getQualifierListeners");

            methods.add("getStreamListeners");

            methods.add("removeAttributeListener");

            methods.add("removeElementAttributeListener");

            methods.add("removeElementListener");

            methods.add("removeFormulaListener");
            methods.add("removeQualifierListener");

            methods.add("removeStreamListener");

            methods.add("setPluginProperty");

            methods.add("getQualifierIdForElement");

            methods.add("setProperties");

            methods.add("addBranchListener");

            methods.add("removeBranchListener");

            methods.add("getBranchListeners");

            for (Method method : getClass().getMethods()) {
                if (methods.indexOf(method.getName()) >= 0) {
                    putMethod(method, this);
                }
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        ArrayGetCallback t = new ArrayGetCallback() {
            @Override
            public Object[] getArray() {
                return new Object[]{TcpClientEngine.this};
            }
        };

        addMethods(StreamListener.class, new ArrayGetCallback() {
            public Object[] getArray() {
                return streamListeners;
            }
        });
        addMethods(AttributeListener.class, new ArrayGetCallback() {
            public Object[] getArray() {
                return attributeListeners;
            }
        });
        addMethods(QualifierListener.class, new ArrayGetCallback() {
            public Object[] getArray() {
                return qualifierListeners;
            }
        });

        classListeners.put(ElementListener.class.getName(), t);
        classListeners.put(ElementAttributeListener.class.getName(), t);

        Hashtable<String, Method> elementHash = new Hashtable<String, Method>(3);
        addElementMethods(elementHash, "elementCreated");
        addElementMethods(elementHash, "elementDeleted");
        addElementMethods(elementHash, "beforeElementDeleted");
        methodListeners.put(ElementListener.class.getName(), elementHash);

        Hashtable<String, Method> elementAttributeHash = new Hashtable<String, Method>(
                1);
        addElementAttributeMethods(elementAttributeHash, "attributeChanged");
        methodListeners.put(ElementAttributeListener.class.getName(),
                elementAttributeHash);

        Hashtable<String, Method> journalMethdos = new Hashtable<String, Method>(
                4);
        addJournalMethods(journalMethdos, "beforeStore");

        addJournalMethods(journalMethdos, "afterStore");

        addJournalMethods(journalMethdos, "afterUndo");

        addJournalMethods(journalMethdos, "afterRedo");
        methodListeners.put(JournalListener.class.getName(), journalMethdos);
        classListeners.put(JournalListener.class.getName(),
                new ArrayGetCallback() {
                    @Override
                    public Object[] getArray() {
                        return ((Journaled) engine).getJournalListeners();
                    }
                });

        Hashtable<String, Method> branchMethdos = new Hashtable<String, Method>(
                3);
        addBranchMethods(branchMethdos, "branchCreated");

        addBranchMethods(branchMethdos, "branchDeleted");

        addBranchMethods(branchMethdos, "branchActivated");
        methodListeners.put(BranchListener.class.getName(), branchMethdos);

        classListeners.put(BranchListener.class.getName(),
                new ArrayGetCallback() {

                    @Override
                    public Object[] getArray() {
                        return getBranchListeners();
                    }
                });
    }

    private void putMethod(Method method, Object value) {
        hashtable.put(
                new MethodHolder(method.getName(),
                        method.getParameterTypes().length), value);

    }

    private void addMethods(Class<?> clazz, ArrayGetCallback callback) {
        classListeners.put(clazz.getName(), callback);
        Hashtable<String, Method> hash = new Hashtable<String, Method>();
        for (Method m : clazz.getMethods()) {
            hash.put(m.getName(), m);
        }
        methodListeners.put(clazz.getName(), hash);
    }

    private interface ArrayGetCallback {
        Object[] getArray();
    }

    ;

    private void addElementMethods(Hashtable<String, Method> elementHash,
                                   String key) {
        try {
            elementHash.put(key,
                    this.getClass().getMethod(key, ElementEvent.class));
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }
    }

    private void addJournalMethods(Hashtable<String, Method> elementHash,
                                   String key) {
        try {
            elementHash.put(key,
                    JournalListener.class.getMethod(key, JournalEvent.class));
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }
    }

    private void addBranchMethods(Hashtable<String, Method> elementHash,
                                  String key) {
        try {
            elementHash.put(key,
                    BranchListener.class.getMethod(key, BranchEvent.class));
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }
    }

    private void addElementAttributeMethods(
            Hashtable<String, Method> elementHash, String key) {
        try {
            elementHash.put(key,
                    this.getClass().getMethod(key, AttributeEvent.class));
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

        MethodHolder methodHolder = new MethodHolder(method.getName(),
                (args == null) ? 0 : args.length);

        Object object = hashtable.get(methodHolder);
        if (object != null) {
            return method.invoke(object, args);
        }

        CallParameters parameters = new CallParameters(method.getName(), args);

        try {
            CallResult result;
            result = invocker.invoke(parameters);

            for (EventHolder holder : result.holder.getEvents()) {
                callEvent(holder, true);
            }

            if (result.exception != null)
                throw result.exception;
            return result.result;
        } catch (RemoteException e) {
            e.printStackTrace();
            remoteException(e);
        }
        return null;
    }

    private void callEvent(EventHolder event, boolean my) {
        if (!my && event.getInterfaceName().contains("BranchListener"))
            return;
        Method m = getMethods(event.getInterfaceName()).get(
                event.getMethodName());
        Object[] classes = classListeners.get(event.getInterfaceName())
                .getArray();
        Object[] args = event.getParameters();
        if (args[0] instanceof Event) {
            Event event2 = (Event) args[0];
            event2.setEngine(this);
            if (!my)
                event2.setJournaled(true);
        }

        for (Object object : classes) {
            try {
                m.invoke(object, args);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private Hashtable<String, Method> getMethods(String className) {
        return methodListeners.get(className);
    }

    static void remoteException(final RemoteException exception) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,
                        exception.getLocalizedMessage());
                System.exit(0);
            }
        });
    }

    @Override
    public void addJournalListener(JournalListener listener) {
        journalListenerList.add(JournalListener.class, listener);
    }

    @Override
    public JournalListener[] getJournalListeners() {
        return journalListenerList.getListeners(JournalListener.class);
    }

    @Override
    public void removeJournalListener(JournalListener listener) {
        journalListenerList.remove(JournalListener.class, listener);
    }

    @Override
    public List<Element> findElements(long qualifierId, Attribute attribute,
                                      Object object) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public String[] getAllImplementationClasseNames() {
        throw new RuntimeException("Method cannot be called derectly");

    }

    @Override
    public Object getAttribute(Element element, Attribute attribute) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public IEngine getDeligate() {
        return null;
    }

    @Override
    public Hashtable<Element, Object[]> getElements(Qualifier qualifier,
                                                    List<Attribute> attributes) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void replaceElements(Element[] oldElements, Element newElement) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void setAttribute(Element element, Attribute attribute, Object object) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Object toUserValue(Attribute attribute, Element element, Object value) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Attribute createAttribute(AttributeType attributeType) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Attribute createAttribute(long attributeId,
                                     AttributeType attributeType) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Element createElement(long qualifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Element createElement(long qualifierId, long elementId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Qualifier createQualifier() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Qualifier createQualifier(long qualifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Attribute createSystemAttribute(AttributeType attributeType) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Qualifier createSystemQualifier() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Qualifier createSystemQualifier(long qualifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void deleteAttribute(long id) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void deleteElement(long id) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void deleteQualifier(long id) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public boolean deleteStream(String path) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<CalculateInfo> findCalculateInfos(String reg,
                                                  boolean autoRecalculate) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Attribute getAttribute(long attributeId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Attribute getAttributeByName(String attributeName) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Transaction getAttributePropertyWhatWillBeDeleted(long attributeId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public AttributeType[] getAttributeTypes() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Transaction[] getAttributeWhatWillBeDeleted(long elementId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Hashtable<Element, Transaction> getAttributeWhatWillBeDeleted(
            long qualifierId, long attributeId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<Attribute> getAttributes() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Transaction[] getAttributesWhatWillBeDeleted(long elementId,
                                                        List<Attribute> attributes) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<Persistent>[] getBinaryAttribute(long elementId,
                                                 long attributeId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Hashtable<Element, List<Persistent>[][]> getBinaryElements(
            long qualifierId, long[] attributeIds) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public CalculateInfo getCalculateInfo(long elementId, long attributeId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<CalculateInfo> getDependences(long elementId, long attributeId,
                                              boolean autoRecalculate) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Element getElement(long id) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Element getElement(String elementName, long qualifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public long getElementCountForQualifier(long qialifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<Element> getElements(long qualifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<Element> getElements(long qualifierId, Attribute attribute,
                                     FindObject[] findObjects) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public int getId() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Qualifier getQualifierByName(String qualifierName) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Qualifier getQualifier(long qualifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public long getQualifierIdForElement(long elementId) {
        Long res = qualifirsForElements.get(elementId);
        if (res != null)
            return res.longValue();
        Element element1 = engine.getElement(elementId);
        if (element1 == null) {
            qualifirsForElements.put(elementId, -1l);
            return -1l;
        }
        long id = element1.getQualifierId();
        if (id >= 0) {
            List<Element> list = engine.getElements(id);
            for (Element element : list)
                qualifirsForElements.put(element.getId(),
                        element.getQualifierId());
        }
        return id;
    }

    @Override
    public List<Qualifier> getQualifiers() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public byte[] getStream(String path) {
        try {
            return (byte[]) invocker.invoke(new CallParameters("getStream",
                    new Object[]{path})).result;
        } catch (RemoteException e) {
            e.printStackTrace();
            remoteException(e);
        }
        return null;
    }

    @Override
    public String[] getStreamNames() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Attribute getSystemAttribute(String attributeName) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public AttributeType[] getSystemAttributeTypes() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<Attribute> getSystemAttributes() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Qualifier getSystemQualifier(String qualifierName) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<Qualifier> getSystemQualifiers() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public long nextValue(String sequence) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public boolean setBinaryAttribute(long elementId, long attributeId,
                                      Transaction transaction) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void setCalculateInfo(CalculateInfo formula) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void setElementQualifier(long elementId, long qualifierId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void setStream(String path, byte[] bytes) {
        try {
            invocker.invoke(new CallParameters("setStream", new Object[]{
                    path, bytes}));
        } catch (RemoteException e) {
            e.printStackTrace();
            remoteException(e);
        }
    }

    @Override
    public void updateAttribute(Attribute attribute) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void updateQualifier(Qualifier qualifier) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public boolean canRedo() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public boolean canUndo() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commitUserTransaction() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public boolean isEnable() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public boolean isUserTransactionStarted() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void redoUserTransaction() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void rollbackUserTransaction() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void setEnable(boolean b) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void setNoUndoPoint() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void startUserTransaction() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void undoUserTransaction() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public long getBranch() {
        throw new RuntimeException("Method cannot be called derectly");
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

    public void setEngine(Engine engine) {
        this.engine = engine;
        engine.addElementListener(null, new ElementAdapter() {
            @Override
            public void elementCreated(ElementEvent event) {
                qualifirsForElements.put(event.getNewElement().getId(), event
                        .getNewElement().getQualifierId());
            }

            @Override
            public void elementDeleted(ElementEvent event) {
                qualifirsForElements.remove(event.getOldElement().getId());
            }
        });
    }

    @Override
    public void call(final EvenstHolder holder) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (EventHolder holder2 : holder.getEvents())
                    callEvent(holder2, false);
            }
        });
    }

    @Override
    public void setUndoableStream(String path, byte[] data) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public long createBranch(long parent, String reason, int type, String module) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public Branch getRootBranch() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public long getActiveBranch() {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void setActiveBranch(long branchToActivate) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void deleteBranch(long branch) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public List<Persistent>[] getBinaryBranchAttribute(long elementId,
                                                       long attributeId, long branchId) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void addBranchListener(BranchListener branchListener) {
        branchListenerList.add(BranchListener.class, branchListener);
    }

    @Override
    public void removeBranchListener(BranchListener branchListener) {
        branchListenerList.remove(BranchListener.class, branchListener);
    }

    @Override
    public BranchListener[] getBranchListeners() {
        return branchListenerList.getListeners(BranchListener.class);
    }

    @Override
    public void updateBranch(Branch branch) {
        throw new RuntimeException("Method cannot be called derectly");
    }

    @Override
    public void createBranch(long parentBranchId, long branchId, String reason,
                             int type, String module) {
        throw new RuntimeException("Method cannot be called derectly");
    }
}
