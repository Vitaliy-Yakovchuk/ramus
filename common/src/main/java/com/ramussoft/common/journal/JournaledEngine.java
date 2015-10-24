package com.ramussoft.common.journal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Branch;
import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.attribute.FindObject;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.BranchEvent;
import com.ramussoft.common.event.BranchListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.FormulaEvent;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.event.StreamEvent;
import com.ramussoft.common.event.StreamListener;
import com.ramussoft.common.journal.command.CreateAttributeCommand;
import com.ramussoft.common.journal.command.CreateElementCommand;
import com.ramussoft.common.journal.command.CreateQualifierCommand;
import com.ramussoft.common.journal.command.DeleteAttributeCommand;
import com.ramussoft.common.journal.command.DeleteElementCommand;
import com.ramussoft.common.journal.command.DeleteQualifierCommand;
import com.ramussoft.common.journal.command.FormulaCommand;
import com.ramussoft.common.journal.command.NewBranchCommand;
import com.ramussoft.common.journal.command.NextCommand;
import com.ramussoft.common.journal.command.SetElementQualifierCommand;
import com.ramussoft.common.journal.command.SetStreamCommand;
import com.ramussoft.common.journal.command.TransactionStorageCommand;
import com.ramussoft.common.journal.command.UpdateAttributeCommand;
import com.ramussoft.common.journal.command.UpdateQualifierCommand;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.common.persistent.Transaction;

public class JournaledEngine extends AbstractJournaledEngine {

    IEngine deligate;

    private Journal journal;

    private int id;

    private Hashtable<Class<? extends Persistent>, PersistentWrapper> wrappers = new Hashtable<Class<? extends Persistent>, PersistentWrapper>();

    private Hashtable<Class<? extends Persistent>, PersistentRow> metadata = new Hashtable<Class<? extends Persistent>, PersistentRow>();

    private Hashtable<Long, Long> qualifirsForElements = new Hashtable<Long, Long>();

    private JournalFactory journalFactory;

    private Object swithJournalLock = new Object();

    @SuppressWarnings("unchecked")
    public JournaledEngine(PluginFactory pluginFactory, IEngine deligate,
                           List<PersistentRow> rows, JournalFactory journalFactory,
                           AccessRules accessor) throws ClassNotFoundException {
        super(pluginFactory);
        this.deligate = deligate;
        this.journalFactory = journalFactory;
        this.id = deligate.getId();
        boolean e;
        synchronized (swithJournalLock) {
            journal = journalFactory.getJournal(this, getActiveBranch());
            e = journal.isEnable();
            journal.setEnable(false);
        }
        for (PersistentRow row : rows) {
            Class<? extends Persistent> clazz = (Class<? extends Persistent>) row
                    .getClassLoader().loadClass(row.getClassName());
            metadata.put(clazz, row);
            wrappers.put(clazz, new PersistentWrapper(clazz));
        }
        initPlugins(pluginFactory, accessor);
        journal.setEnable(e);
    }

    protected void initPlugins(PluginFactory pluginFactory, AccessRules accessor) {
        List<Plugin> plugins = pluginFactory.getPlugins();
        for (Plugin plugin : plugins) {
            plugin.init(this, accessor);
        }
    }

    @Override
    public Hashtable<Element, Object[]> getElements(Qualifier qualifier,
                                                    List<Attribute> attributes) {
        Hashtable<Element, Object[]> elements = super.getElements(qualifier,
                attributes);
        Enumeration<Element> e = elements.keys();
        Long id = qualifier.getId();
        while (e.hasMoreElements())
            qualifirsForElements.put(e.nextElement().getId(), id);
        return elements;
    }

    @Override
    public boolean setAttribute(long elementId, long attributeId,
                                Transaction transaction) {
        boolean updated = deligate.setBinaryAttribute(elementId, attributeId,
                transaction);
        transaction.setRemoveBranchInfo(updated);
        TransactionStorageCommand command = new TransactionStorageCommand(this,
                elementId, attributeId, transaction);
        synchronized (swithJournalLock) {
            journal.store(command);
        }
        return updated;
    }

    @Override
    protected boolean setBinaryAttributeN(long elementId, long attributeId,
                                          Transaction transaction) {
        return deligate.setBinaryAttribute(elementId, attributeId, transaction);
    }

    @Override
    public Qualifier createQualifier() {
        return registerCreation(deligate.createQualifier());
    }

    private Qualifier registerCreation(Qualifier qualifier) {
        synchronized (swithJournalLock) {
            journal.store(new CreateQualifierCommand(this, qualifier));
        }

        QualifierEvent event = new QualifierEvent(this, null, qualifier);

        qualifierCreated(event);

        return qualifier;
    }

    public void qualifierCreated(QualifierEvent event) {
        for (QualifierListener listener : qualifierListeners) {
            listener.qualifierCreated(event);
        }
    }

    @Override
    public Qualifier createQualifier(long qualifierId) {
        return registerCreation(deligate.createQualifier(qualifierId));
    }

    @Override
    public Attribute createAttribute(AttributeType attributeType) {
        return registerCreation(deligate.createAttribute(attributeType));
    }

    @Override
    public Attribute createAttribute(long attributeId,
                                     AttributeType attributeType) {
        return registerCreation(deligate.createAttribute(attributeId,
                attributeType));
    }

    @Override
    public Element createElement(long qualifierId) {
        return registerCreation(qualifierId,
                deligate.createElement(qualifierId));
    }

    private Element registerCreation(long qualifierId, Element element) {
        synchronized (swithJournalLock) {
            journal.store(new CreateElementCommand(this, qualifierId, element
                    .getId()));
        }
        ElementEvent event = new ElementEvent(this, null, element, qualifierId);
        elementCreated(event);
        return element;
    }

    @Override
    public Element createElement(long qualifierId, long elementId) {
        return registerCreation(qualifierId,
                deligate.createElement(qualifierId, elementId));
    }

    @Override
    public void deleteAttribute(long id) {
        Attribute attribute = getAttribute(id);
        DeleteAttributeCommand command = new DeleteAttributeCommand(this,
                attribute);
        AttributeEvent event = new AttributeEvent(this, null, attribute,
                attribute, null);
        beforeAttributeDeleted(event);
        deligate.deleteAttribute(id);
        synchronized (swithJournalLock) {
            journal.store(command);
        }
        attributeDeleted(event);
    }

    public void beforeAttributeDeleted(AttributeEvent event) {
        for (AttributeListener listener : attributeListeners) {
            listener.beforeAttributeDeleted(event);
        }
    }

    public void attributeDeleted(AttributeEvent event) {
        for (AttributeListener listener : attributeListeners) {
            listener.attributeDeleted(event);
        }
    }

    @Override
    public void deleteQualifier(long id) {
        Qualifier qualifier = getQualifier(id);
        deligate.deleteQualifier(id);
        synchronized (swithJournalLock) {
            journal.store(new DeleteQualifierCommand(this, qualifier));
        }

        QualifierEvent event = new QualifierEvent(this, qualifier, null);
        qualifierDeleted(event);
    }

    public void qualifierDeleted(QualifierEvent event) {
        for (QualifierListener listener : qualifierListeners) {
            listener.qualifierDeleted(event);
        }
    }

    @Override
    public void deleteElement(long id) {
        qualifirsForElements.remove(id);

        Element element = getElement(id);

        long qualifierId = getQualifierIdForElement(id);
        Transaction[] data = getAttributeWhatWillBeDeleted(id);
        ElementEvent event = new ElementEvent(this, element, null, qualifierId);
        try {
            beforeElementDeleted(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        deligate.deleteElement(id);
        synchronized (swithJournalLock) {
            journal.store(new DeleteElementCommand(this, qualifierId, id, data));
        }

        elementDeleted(event);

    }

    @Override
    public void updateAttribute(Attribute attribute) {
        Attribute old = getAttribute(attribute.getId());
        deligate.updateAttribute(attribute);
        synchronized (swithJournalLock) {
            journal.store(new UpdateAttributeCommand(this, old, attribute));
        }
        AttributeEvent event = new AttributeEvent(this, null, attribute, old,
                attribute);
        for (AttributeListener listener : attributeListeners) {
            listener.attributeUpdated(event);
        }
    }

    @Override
    public void updateQualifier(Qualifier qualifier) {
        Qualifier old = getQualifier(qualifier.getId());
        QualifierEvent event = new QualifierEvent(this, old, qualifier);

        beforeQualifierUpdated(event);

        event = new QualifierEvent(this, old, qualifier);

        UpdateQualifierCommand command = new UpdateQualifierCommand(this, old,
                qualifier);
        deligate.updateQualifier(qualifier);
        synchronized (swithJournalLock) {
            journal.store(command);
        }

        updateElementNames(old, qualifier);

        qualifierUpdated(event);
    }

    public void qualifierUpdated(QualifierEvent event) {
        for (QualifierListener listener : qualifierListeners) {
            listener.qualifierUpdated(event);
        }
    }

    public void beforeQualifierUpdated(QualifierEvent event) {
        for (QualifierListener listener : qualifierListeners) {
            listener.beforeQualifierUpdated(event);
        }
    }

    private Attribute registerCreation(Attribute attribute) {
        synchronized (swithJournalLock) {
            journal.store(new CreateAttributeCommand(this, attribute));
        }
        AttributeEvent event = new AttributeEvent(this, null, attribute, null,
                attribute);
        attributeCreated(event);
        return attribute;
    }

    public void attributeCreated(AttributeEvent event) {
        for (AttributeListener listener : attributeListeners) {
            listener.attributeCreated(event);
        }
    }

    public long getPersistentClassId(Class<? extends Persistent> key) {
        return getPersistentMetadata(key).getId();
    }

    @Override
    public PersistentRow getPersistentMetadata(Class<? extends Persistent> key) {
        return metadata.get(key);
    }

    @Override
    public PersistentWrapper getWrapper(Class<? extends Persistent> key) {
        return wrappers.get(key);
    }

    public Class<? extends Persistent> getPersistentClassById(int id) {
        for (Entry<Class<? extends Persistent>, PersistentRow> entry : metadata
                .entrySet()) {
            if (entry.getValue().getId() == id)
                return entry.getKey();
        }
        throw new RuntimeException("Persistent class for id: " + id
                + " not found");
    }

    @Override
    public boolean setBinaryAttribute(long elementId, long attributeId,
                                      Transaction transaction) {
        throw new RuntimeException("Method "
                + Thread.currentThread().getStackTrace()[0].getMethodName()
                + " not supported by JournaledEngine");
    }

    @Override
    public List<Persistent>[] getBinaryAttribute(long elementId,
                                                 long attributeId) {
        return deligate.getBinaryAttribute(elementId, attributeId);
    }

    @Override
    public Hashtable<Element, List<Persistent>[][]> getBinaryElements(
            long qualifierId, long[] attributeIds) {
        return deligate.getBinaryElements(qualifierId, attributeIds);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Attribute getAttribute(long attributeId) {
        return deligate.getAttribute(attributeId);
    }

    @Override
    public AttributeType[] getAttributeTypes() {
        return deligate.getAttributeTypes();
    }

    @Override
    public List<Attribute> getAttributes() {
        return deligate.getAttributes();
    }

    @Override
    public List<Element> getElements(long qualifierId) {
        List<Element> list = deligate.getElements(qualifierId);
        Long id = qualifierId;
        for (Element e : list)
            qualifirsForElements.put(e.getId(), id);
        return list;
    }

    @Override
    public Qualifier getQualifier(long qualifierId) {
        return deligate.getQualifier(qualifierId);
    }

    @Override
    public long getQualifierIdForElement(long elementId) {
        Long res = qualifirsForElements.get(elementId);
        if (res != null)
            return res;
        res = deligate.getQualifierIdForElement(elementId);
        qualifirsForElements.put(elementId, res);
        return res;
    }

    @Override
    public List<Qualifier> getQualifiers() {
        return deligate.getQualifiers();
    }

    @Override
    public AttributeType[] getSystemAttributeTypes() {
        return deligate.getSystemAttributeTypes();
    }

    @Override
    public Hashtable<Element, Transaction> getAttributeWhatWillBeDeleted(
            long qualifierId, long attributeId) {
        return deligate.getAttributeWhatWillBeDeleted(qualifierId, attributeId);
    }

    @Override
    public Transaction[] getAttributeWhatWillBeDeleted(long elementId) {
        return deligate.getAttributeWhatWillBeDeleted(elementId);
    }

    @Override
    public Element getElement(long id) {
        return deligate.getElement(id);
    }

    @Override
    public Qualifier createSystemQualifier() {
        return registerCreation(deligate.createSystemQualifier());
    }

    @Override
    public Attribute createSystemAttribute(AttributeType attributeType) {
        return deligate.createSystemAttribute(attributeType);
    }

    @Override
    public List<Element> findElements(long qualifierId, Attribute attribute,
                                      Object object) {
        AttributeConverter converter = pluginFactory
                .getAttributeConverter(attribute.getAttributeType());

        FindObject[] findObjects = converter.getFindObjects(object);
        if (findObjects != null)
            return deligate.getElements(qualifierId, attribute, findObjects);

        List<Attribute> list = new ArrayList<Attribute>(1);
        list.add(attribute);
        Hashtable<Element, Object[]> elements = getElements(
                getQualifier(qualifierId), list);
        ArrayList<Element> res = new ArrayList<Element>();
        Enumeration<Element> e = elements.keys();
        while (e.hasMoreElements()) {
            Element element = e.nextElement();
            if (objectEquals(object, elements.get(element)[0])) {
                res.add(element);
            }
        }
        Collections.sort(res, new Comparator<Element>() {

            @Override
            public int compare(Element o1, Element o2) {
                if (o1.getId() < o2.getId())
                    return -1;
                if (o1.getId() > o2.getId())
                    return 1;
                return 0;
            }

        });
        return res;
    }

    private boolean objectEquals(Object o1, Object o2) {
        if (o1 == null)
            return o2 == null;
        return o1.equals(o2);
    }

    @Override
    public String[] getStreamNames() {
        return deligate.getStreamNames();
    }

    public void updateElementNames(Qualifier old, Qualifier qualifier) {
        if (qualifier == null)
            return;
        if ((old == null)
                || (old.getAttributeForName() != qualifier
                .getAttributeForName())) {

            Attribute attr = getAttribute(qualifier.getAttributeForName());
            if (attr != null) {
                List<Attribute> list = new ArrayList<Attribute>(1);
                list.add(attr);
                Hashtable<Element, Object[]> hash = getElements(qualifier, list);
                List<Element> elements = new ArrayList<Element>(hash.size());
                for (Entry<Element, Object[]> entry : hash.entrySet()) {
                    Element element = entry.getKey();
                    if (entry.getValue()[0] == null) {
                        element.setName("");
                    } else {
                        element.setName(entry.getValue()[0].toString());
                    }
                    elements.add(element);
                }
            } else {
                List<Element> list = getElements(qualifier.getId());
                for (Element e : list) {
                    e.setName("");
                }
            }
        }
    }

    @Override
    public boolean deleteStream(String path) {
        boolean deleteStream = deligate.deleteStream(path);
        StreamEvent event = new StreamEvent(this, path, null, false);
        for (StreamListener listener : getStreamListeners())
            listener.streamDeleted(event);
        return deleteStream;
    }

    @Override
    public byte[] getStream(String path) {
        return deligate.getStream(path);
    }

    @Override
    public void setStream(String path, byte[] bytes) {
        byte[] bs = deligate.getStream(path);
        if (!Arrays.equals(bs, bytes)) {
            deligate.setStream(path, bytes);
        }
        StreamEvent event = new StreamEvent(this, path, bytes, false);
        streamUpdated(event);
    }

    public void streamUpdated(StreamEvent event) {
        for (StreamListener listener : getStreamListeners())
            listener.streamUpdated(event);
    }

    @Override
    public IEngine getDeligate() {
        return deligate;
    }

    @Override
    public long getElementCountForQualifier(long qialifierId) {
        return deligate.getElementCountForQualifier(qialifierId);
    }

    @Override
    public long nextValue(String sequence) {
        long value = deligate.nextValue(sequence);
        synchronized (swithJournalLock) {
            journal.store(new NextCommand(this, value, sequence));
        }
        return value;
    }

    @Override
    public Qualifier getSystemQualifier(String qualifierName) {
        return deligate.getSystemQualifier(qualifierName);
    }

    @Override
    public void replaceElements(Element[] oldElements, Element newElement) {
        for (Plugin plugin : pluginFactory.getPlugins()) {
            plugin.replaceElements(this, oldElements, newElement);
        }
        for (Element oldElement : oldElements)
            deleteElement(oldElement.getId());
    }

    @Override
    public Transaction getAttributePropertyWhatWillBeDeleted(long attributeId) {
        return deligate.getAttributePropertyWhatWillBeDeleted(attributeId);
    }

    @Override
    public List<CalculateInfo> getDependences(long elementId, long attributeId,
                                              boolean autoRecalculate) {
        return deligate.getDependences(elementId, attributeId, autoRecalculate);
    }

    @Override
    public CalculateInfo getCalculateInfo(long elementId, long attributeId) {
        return deligate.getCalculateInfo(elementId, attributeId);
    }

    @Override
    public void setCalculateInfo(CalculateInfo formula) {
        CalculateInfo old = deligate.getCalculateInfo(formula.getElementId(),
                formula.getAttributeId());
        deligate.setCalculateInfo(formula);
        synchronized (swithJournalLock) {
            journal.store(new FormulaCommand(this, old, formula));
        }
        FormulaEvent event = new FormulaEvent(this, false, old, formula);
        formulaChanged(event);
    }

    @Override
    public Attribute getAttributeByName(String attributeName) {
        return deligate.getAttributeByName(attributeName);
    }

    @Override
    public Element getElement(String elementName, long qualifierId) {
        return deligate.getElement(elementName, qualifierId);
    }

    @Override
    public Qualifier getQualifierByName(String qualifierName) {
        return deligate.getQualifierByName(qualifierName);
    }

    @Override
    public Attribute getSystemAttribute(String attributeName) {
        return deligate.getSystemAttribute(attributeName);
    }

    @Override
    @Deprecated
    public List<Attribute> getSystemAttributes() {
        return deligate.getSystemAttributes();
    }

    @Override
    @Deprecated
    public List<Qualifier> getSystemQualifiers() {
        return deligate.getSystemQualifiers();
    }

    @Override
    public Qualifier createSystemQualifier(long qualifierId) {
        return registerCreation(deligate.createSystemQualifier(qualifierId));
    }

    @Override
    public List<Element> getElements(long qualifierId, Attribute attribute,
                                     FindObject[] findObjects) {
        return deligate.getElements(qualifierId, attribute, findObjects);
    }

    @Override
    public Transaction[] getAttributesWhatWillBeDeleted(long elementId,
                                                        List<Attribute> attributes) {
        return deligate.getAttributesWhatWillBeDeleted(elementId, attributes);
    }

    @Override
    public void setElementQualifier(long elementId, long qualifierId) {
        qualifirsForElements.remove(elementId);

        Element element = getElement(elementId);

        Qualifier current = getQualifier(element.getQualifierId());

        Qualifier newQualifier = getQualifier(qualifierId);

        final List<Attribute> attrs = new ArrayList<Attribute>();
        addNotPresentAttributes(current.getAttributes(),
                newQualifier.getAttributes(), attrs);
        addNotPresentAttributes(current.getSystemAttributes(),
                newQualifier.getSystemAttributes(), attrs);

        Transaction[] data = getAttributesWhatWillBeDeleted(elementId, attrs);
        ElementEvent event = new ElementEvent(this, element, new Element(
                element.getId(), qualifierId, element.getName()),
                element.getQualifierId());
        beforeElementDeleted(event);

        deligate.setElementQualifier(elementId, qualifierId);
        synchronized (swithJournalLock) {
            journal.store(new SetElementQualifierCommand(this, element
                    .getQualifierId(), qualifierId, elementId, data));
        }

        elementDeleted(event);
        event = new ElementEvent(this, element, new Element(element.getId(),
                qualifierId, element.getName()), qualifierId);
        qualifirsForElements.remove(element.getId());
        elementCreated(event);
    }

    private void addNotPresentAttributes(List<Attribute> attributes,
                                         List<Attribute> attributes2, List<Attribute> attrs) {
        for (Attribute attribute : attributes)
            if (attributes2.indexOf(attribute) < 0)
                attrs.add(attribute);
    }

    public void removeElementQualifierFromBuffer(long elementId) {
        qualifirsForElements.remove(elementId);
    }

    @Override
    public List<CalculateInfo> findCalculateInfos(String reg,
                                                  boolean autoRecalculate) {
        return deligate.findCalculateInfos(reg, autoRecalculate);
    }

    @Override
    public void setUndoableStream(String path, byte[] data) {
        byte[] oldBytes = getStream(path);
        setStream(path, data);
        synchronized (swithJournalLock) {
            journal.store(new SetStreamCommand(this, oldBytes, data, path));
        }
    }

    public Journal getJournal() {
        synchronized (swithJournalLock) {
            return journal;
        }
    }

    @Override
    public long createBranch(long parent, String reason, int type, String module) {
        if (parent == -1l)
            parent = getActiveBranch();
        long branchId = deligate.createBranch(parent, reason, type, module);
        synchronized (swithJournalLock) {
            Journal parentBranchJournal = journalFactory.getJournal(this,
                    parent);
            parentBranchJournal.startUserTransaction();
            parentBranchJournal.store(new NewBranchCommand(this, parent,
                    branchId, reason, type, module));
            parentBranchJournal.commitUserTransaction();
            //parentBranchJournal.setLock(true);
            journal = journalFactory.getJournal(this, branchId);
        }
        BranchEvent event = new BranchEvent(this, false, branchId, parent);
        for (BranchListener listener : getBranchListeners())
            listener.branchCreated(event);
        for (BranchListener listener : getBranchListeners())
            listener.branchActivated(event);
        return branchId;
    }

    @Override
    public long getActiveBranch() {
        return deligate.getActiveBranch();
    }

    @Override
    public void setActiveBranch(long branchId) {
        deligate.setActiveBranch(branchId);
        branchId = deligate.getActiveBranch();
        synchronized (swithJournalLock) {
            journal = journalFactory.getJournal(this, branchId);
        }
        BranchEvent event = new BranchEvent(this, false, branchId, -1l);
        for (BranchListener listener : getBranchListeners())
            listener.branchActivated(event);
    }

    @Override
    public void deleteBranch(long branchId) {
        BranchEvent event = new BranchEvent(this, false, branchId, -1l);
        long active = getActiveBranch();
        deligate.deleteBranch(branchId);
        for (BranchListener listener : getBranchListeners())
            listener.branchDeleted(event);
        if (active == branchId) {
            event = new BranchEvent(this, false, getActiveBranch(), -1l);
            for (BranchListener listener : getBranchListeners())
                listener.branchActivated(event);
        }
    }

    @Override
    public List<Persistent>[] getBinaryBranchAttribute(long elementId,
                                                       long attributeId, long branchId) {
        return deligate.getBinaryBranchAttribute(elementId, attributeId,
                branchId);
    }

    @Override
    public Branch getRootBranch() {
        return deligate.getRootBranch();
    }

    @Override
    public void updateBranch(Branch branch) {
        deligate.updateBranch(branch);
    }

    @Override
    public void createBranch(long parent, long branchId, String reason,
                             int type, String module) {
        if (parent == -1l)
            parent = getActiveBranch();
        deligate.createBranch(parent, branchId, reason, type, module);
        synchronized (swithJournalLock) {
            Journal parentBranchJournal = journalFactory.getJournal(this,
                    parent);
            parentBranchJournal.store(new NewBranchCommand(this, parent,
                    branchId, reason, type, module));
            //parentBranchJournal.setLock(true);
            journal = journalFactory.getJournal(this, branchId);
        }
        BranchEvent event = new BranchEvent(this, false, branchId, parent);
        for (BranchListener listener : getBranchListeners())
            listener.branchCreated(event);
        for (BranchListener listener : getBranchListeners())
            listener.branchActivated(event);
    }

    public JournalFactory getJournalFactory() {
        return journalFactory;
    }
}
