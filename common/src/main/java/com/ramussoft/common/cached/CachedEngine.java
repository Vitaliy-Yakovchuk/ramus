package com.ramussoft.common.cached;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

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
import com.ramussoft.common.event.BranchAdapter;
import com.ramussoft.common.event.BranchEvent;
import com.ramussoft.common.event.BranchListener;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.FormulaListener;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.event.StreamListener;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;

public class CachedEngine extends AbstractEngine implements Cached {

    private Engine deligate;

    private Hashtable<Long, CachedElement> elements = new Hashtable<Long, CachedElement>();

    private Hashtable<Long, CachedQualifier> qualifiers = new Hashtable<Long, CachedQualifier>();

    private Hashtable<Long, CachedAttributeData> attributeData = new Hashtable<Long, CachedAttributeData>();

    private Hashtable<String, Qualifier> systemQualifiers = new Hashtable<String, Qualifier>();

    private Hashtable<String, Attribute> systemAttributes = new Hashtable<String, Attribute>();

    private Hashtable<Long, Attribute> attributes = new Hashtable<Long, Attribute>();

    private Object loadLock = new Object();

    private boolean dirtyUpdate;

    private CachedData cachedData;

    public CachedEngine(Engine deligate, CachedData cachedData) {
        this(deligate);
        this.cachedData = cachedData;
        CachedData.CacheHolder holder = cachedData.getCacheHolder(deligate
                .getActiveBranch());
        getCacheFromHolder(holder);
        loadLock = cachedData.loadLock;
    }

    protected void getCacheFromHolder(CachedData.CacheHolder holder) {
        elements = holder.elements;
        qualifiers = holder.qualifiers;
        attributeData = holder.attributeData;
        systemQualifiers = holder.systemQualifiers;
        systemAttributes = holder.systemAttributes;
        attributes = holder.attributes;
        attributes.clear();
        for (Attribute attribute : deligate.getAttributes())
            attributes.put(attribute.getId(), attribute);
    }

    public CachedEngine(Engine deligate, boolean addListeners,
                        boolean dirtyUpdate, CachedData cachedData) {
        this(deligate, addListeners, dirtyUpdate);
        this.cachedData = cachedData;
        CachedData.CacheHolder holder = cachedData.getCacheHolder(deligate
                .getActiveBranch());
        getCacheFromHolder(holder);
        loadLock = cachedData.loadLock;
    }

    public CachedEngine(Engine deligate) {
        this(deligate, true, false);
    }

    public CachedEngine(Engine deligate, boolean addListeners,
                        boolean dirtyUpdate) {
        this.deligate = deligate;
        this.dirtyUpdate = dirtyUpdate;

        for (Attribute attribute : deligate.getAttributes())
            attributes.put(attribute.getId(), attribute);

        if (addListeners) {
            deligate.addElementAttributeListener(null,
                    new ElementAttributeListener() {

                        @Override
                        public void attributeChanged(AttributeEvent event) {
                            Element element = event.getElement();
                            if (element != null) {
                                synchronized (loadLock) {
                                    CachedElement e = elements.get(element
                                            .getId());
                                    if (e != null) {
                                        int i = e.qualifier.getIndex(event
                                                .getAttribute());
                                        if (i >= 0) {
                                            e.objects[i] = event.getNewValue();
                                        }
                                        if (e.qualifier.qualifier
                                                .getAttributeForName() == event
                                                .getAttribute().getId()) {
                                            Object newValue = event
                                                    .getNewValue();
                                            if (newValue == null)
                                                e.element.setName("");
                                            else
                                                e.element.setName(newValue
                                                        .toString());
                                        }
                                    }
                                }
                            } else {
                                synchronized (loadLock) {
                                    CachedAttributeData a = attributeData
                                            .get(event.getAttribute().getId());
                                    if (a != null)
                                        a.data = event.getNewValue();
                                }
                            }
                            CachedEngine.this.attributeChanged(event);
                        }
                    });

            deligate.addElementListener(null, new ElementListener() {

                @Override
                public void elementDeleted(ElementEvent event) {
                    synchronized (loadLock) {
                        Element element = event.getOldElement();
                        CachedElement e = elements.get(element.getId());
                        if (e != null) {
                            elements.remove(element.getId());
                            e.qualifier.elements.remove(e);
                        }
                    }
                    CachedEngine.this.elementDeleted(event);
                }

                @Override
                public void elementCreated(ElementEvent event) {
                    synchronized (loadLock) {
                        Element element = event.getNewElement();
                        CachedQualifier q = qualifiers.get(element
                                .getQualifierId());
                        if (q != null) {
                            CachedElement e = new CachedElement(element,
                                    new Object[q.allAttributes.length], q);
                            q.elements.add(e);
                            elements.put(element.getId(), e);
                        }
                    }
                    CachedEngine.this.elementCreated(event);
                }

                @Override
                public void beforeElementDeleted(ElementEvent event) {
                    CachedEngine.this.beforeElementDeleted(event);
                }
            });

            deligate.addQualifierListener(new QualifierListener() {

                @Override
                public void qualifierUpdated(QualifierEvent event) {
                    synchronized (loadLock) {
                        CachedQualifier q = qualifiers.get(event
                                .getOldQualifier().getId());
                        if (q != null) {
                            List<Attribute> old = q.buildAttributes();
                            q.qualifier = event.getNewQualifier();
                            List<Attribute> nAttrs = q.buildAttributes();
                            if (!old.equals(nAttrs)) {
                                qualifiers.remove(event.getOldQualifier()
                                        .getId());
                                for (CachedElement e : q.elements)
                                    elements.remove(e.element.getId());
                            }
                        }
                    }

                    Qualifier q = event.getOldQualifier();
                    if (q.isSystem()) {
                        if (systemQualifiers.get(q.getName()) != null)
                            systemQualifiers.put(q.getName(),
                                    event.getNewQualifier());
                    }

                    for (QualifierListener l : CachedEngine.this
                            .getQualifierListeners())
                        l.qualifierUpdated(event);
                }

                @Override
                public void qualifierDeleted(QualifierEvent event) {
                    synchronized (loadLock) {
                        CachedQualifier q = qualifiers.get(event
                                .getOldQualifier().getId());
                        if (q != null) {
                            qualifiers.remove(event.getOldQualifier().getId());
                            for (CachedElement e : q.elements)
                                elements.remove(e.element.getId());
                        }
                    }

                    Qualifier q = event.getOldQualifier();
                    if (q.isSystem())
                        systemQualifiers.remove(q.getName());

                    for (QualifierListener l : CachedEngine.this
                            .getQualifierListeners())
                        l.qualifierDeleted(event);
                }

                @Override
                public void qualifierCreated(QualifierEvent event) {
                    for (QualifierListener l : CachedEngine.this
                            .getQualifierListeners())
                        l.qualifierCreated(event);
                }

                @Override
                public void beforeQualifierUpdated(QualifierEvent event) {
                    for (QualifierListener l : CachedEngine.this
                            .getQualifierListeners())
                        l.beforeQualifierUpdated(event);
                }
            });

            deligate.addAttributeListener(new AttributeListener() {

                @Override
                public void beforeAttributeDeleted(AttributeEvent event) {
                    for (AttributeListener l : CachedEngine.this
                            .getAttributeListeners())
                        l.beforeAttributeDeleted(event);
                }

                @Override
                public void attributeUpdated(AttributeEvent event) {
                    synchronized (loadLock) {
                        Attribute attribute = (Attribute) event.getOldValue();
                        if (attribute != null)
                            systemAttributes.remove(attribute.getName());
                        attributes.put(event.getAttribute().getId(),
                                (Attribute) event.getNewValue());
                    }
                    for (AttributeListener l : CachedEngine.this
                            .getAttributeListeners())
                        l.attributeUpdated(event);
                }

                @Override
                public void attributeDeleted(AttributeEvent event) {
                    synchronized (loadLock) {
                        Attribute attribute = (Attribute) event.getOldValue();
                        CachedAttributeData a = attributeData.get(event
                                .getAttribute().getId());
                        attributes.remove(attribute.getId());
                        if (a != null)
                            attributeData.remove(attribute.getId());
                        systemAttributes.remove(attribute.getName());
                    }
                    for (AttributeListener l : CachedEngine.this
                            .getAttributeListeners())
                        l.attributeDeleted(event);
                }

                @Override
                public void attributeCreated(AttributeEvent event) {
                    synchronized (loadLock) {
                        attributes.put(event.getAttribute().getId(),
                                (Attribute) event.getNewValue());
                    }
                    for (AttributeListener l : CachedEngine.this
                            .getAttributeListeners())
                        l.attributeCreated(event);
                }
            });

            deligate.addBranchListener(new BranchAdapter() {
                @Override
                public void branchActivated(BranchEvent event) {
                    clearCache();
                    for (BranchListener l : CachedEngine.this
                            .getBranchListeners())
                        l.branchActivated(event);
                }

                @Override
                public void branchCreated(BranchEvent event) {
                    for (BranchListener l : CachedEngine.this
                            .getBranchListeners())
                        l.branchCreated(event);
                }

                @Override
                public void branchDeleted(BranchEvent event) {
                    for (BranchListener l : CachedEngine.this
                            .getBranchListeners())
                        l.branchDeleted(event);
                }
            });
        }
    }

    @Override
    public void addStreamListener(StreamListener listener) {
        deligate.addStreamListener(listener);
    }

    @Override
    public void removeStreamListener(StreamListener listener) {
        deligate.removeStreamListener(listener);
    }

    @Override
    public StreamListener[] getStreamListeners() {
        return deligate.getStreamListeners();
    }

    @Override
    public void addFormulaListener(FormulaListener listener) {
        deligate.addFormulaListener(listener);
    }

    @Override
    public void removeFormulaListener(FormulaListener listener) {
        deligate.removeFormulaListener(listener);
    }

    @Override
    public FormulaListener[] getFormulaListeners() {
        return deligate.getFormulaListeners();
    }

    @Override
    public Object getPluginProperty(String pluginName, String key) {
        return deligate.getPluginProperty(pluginName, key);
    }

    @Override
    public void setPluginProperty(String pluginName, String key, Object value) {
        deligate.setPluginProperty(pluginName, key, value);
    }

    @Override
    public Properties getProperties(String path) {
        return deligate.getProperties(path);
    }

    @Override
    public void setProperties(String path, Properties properties) {
        deligate.setProperties(path, properties);
    }

    protected CachedAttributeData getCachedAttribute(Attribute attribute) {
        synchronized (loadLock) {
            CachedAttributeData a = attributeData.get(attribute.getId());
            if (a == null) {
                a = new CachedAttributeData();
                a.data = deligate.getAttribute(null, attribute);
                attributeData.put(attribute.getId(), a);
            }
            return a;
        }
    }

    private CachedQualifier getCachedQualifier(Long id) {
        synchronized (loadLock) {
            CachedQualifier q = qualifiers.get(id);
            if (q == null) {
                q = loadQualifier(id);
                if (q != null)
                    qualifiers.put(id, q);
            }
            return q;
        }

    }

    private CachedElement getCachedElement(Long id) {
        synchronized (loadLock) {
            CachedElement e = elements.get(id);
            if (e == null) {
                e = loadCachedElement(id);
            }

            return e;
        }
    }

    private CachedElement loadCachedElement(Long id) {
        Element element = deligate.getElement(id);
        if (element == null)
            return null;
        getCachedQualifier(element.getQualifierId());
        return elements.get(id);
    }

    private CachedQualifier loadQualifier(Long id) {
        CachedQualifier q = new CachedQualifier();
        q.qualifier = deligate.getQualifier(id);
        if (q.qualifier == null)
            return null;
        List<Attribute> attributes = q.buildAttributes();
        q.setAllAttributes(attributes.toArray(new Attribute[attributes.size()]));
        Hashtable<Element, Object[]> hashtable = deligate.getElements(
                q.qualifier, attributes);
        List<CachedElement> elements = q.elements;
        for (Entry<Element, Object[]> entry : hashtable.entrySet()) {
            Element key = entry.getKey();
            CachedElement e = new CachedElement(key, entry.getValue(), q);
            elements.add(e);
            this.elements.put(key.getId(), e);
        }
        return q;
    }

    @Override
    public List<Element> findElements(long qualifierId, Attribute attribute,
                                      Object object) {
        CachedQualifier q = getCachedQualifier(qualifierId);
        if (q == null)
            return new ArrayList<Element>(0);
        int index = q.getIndex(attribute);
        List<Element> result = new ArrayList<Element>();
        if (object == null) {
            for (CachedElement e : q.elements) {
                if (e.objects[index] == null)
                    result.add(e.element);
            }
        } else {
            for (CachedElement e : q.elements) {
                if (object.equals(e.objects[index]))
                    result.add(e.element);
            }
        }

        Collections.sort(result, new Comparator<Element>() {

            @Override
            public int compare(Element o1, Element o2) {
                if (o1.getId() < o2.getId())
                    return -1;
                if (o1.getId() > o2.getId())
                    return 1;
                return 0;
            }

        });

        return result;
    }

    @Override
    public String[] getAllImplementationClasseNames() {
        return deligate.getAllImplementationClasseNames();
    }

    @Override
    public Object getAttribute(Element element, Attribute attribute) {
        if (element == null) {
            CachedAttributeData a = getCachedAttribute(attribute);
            if (a == null)
                return null;
            else
                return a.data;
        }
        if (!attribute.getAttributeType().isLight())
            return deligate.getAttribute(element, attribute);
        CachedElement e = getCachedElement(element.getId());
        if (e != null) {
            int index = e.qualifier.getIndex(attribute);
            if (index == -1)
                return null;
            else
                return e.objects[index];
        }
        return null;
    }

    @Override
    public IEngine getDeligate() {
        return deligate.getDeligate();
    }

    @Override
    public Hashtable<Element, Object[]> getElements(Qualifier qualifier,
                                                    List<Attribute> attributes) {
        CachedQualifier q = getCachedQualifier(qualifier.getId());
        if (q == null)
            return null;
        try {
            return q.getDataAsHash(attributes);
        } catch (Exception e) {
            return deligate.getElements(qualifier, attributes);
        }
    }

    @Override
    public void replaceElements(Element[] oldElements, Element newElement) {
        deligate.replaceElements(oldElements, newElement);
    }

    @Override
    public void setAttribute(Element element, Attribute attribute, Object object) {
        if (dirtyUpdate) {
            if (equals(getAttribute(element, attribute), object))
                return;
        }

        attributeData.remove(attribute.getId());

        if (element != null && attribute.getAttributeType().isLight()) {
            synchronized (loadLock) {
                CachedElement e = elements.get(element.getId());
                if (e != null) {
                    int index = e.qualifier.getIndex(attribute);
                    if (index >= 0) {
                        e.objects[index] = object;
                    }
                }
            }
        }
        aSetAttribute(element, attribute, object);
    }

    protected void aSetAttribute(Element element, Attribute attribute,
                                 Object object) {
        deligate.setAttribute(element, attribute, object);
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 == null)
                return true;
            else
                return false;
        }
        if (o2 == null)
            return false;
        return o1.equals(o2);
    }

    @Override
    public void setUndoableStream(String path, byte[] data) {
        deligate.setUndoableStream(path, data);
    }

    @Override
    public Object toUserValue(Attribute attribute, Element element, Object value) {
        return deligate.toUserValue(attribute, element, value);
    }

    @Override
    public Attribute createAttribute(AttributeType attributeType) {
        return deligate.createAttribute(attributeType);
    }

    @Override
    public Attribute createAttribute(long attributeId,
                                     AttributeType attributeType) {
        return deligate.createAttribute(attributeId, attributeType);
    }

    @Override
    public Element createElement(long qualifierId) {
        return deligate.createElement(qualifierId);
    }

    @Override
    public Element createElement(long qualifierId, long elementId) {
        return deligate.createElement(qualifierId, elementId);
    }

    @Override
    public Qualifier createQualifier() {
        return deligate.createQualifier();
    }

    @Override
    public Qualifier createQualifier(long qualifierId) {
        return deligate.createQualifier(qualifierId);
    }

    @Override
    public Attribute createSystemAttribute(AttributeType attributeType) {
        return deligate.createSystemAttribute(attributeType);
    }

    @Override
    public Qualifier createSystemQualifier() {
        return deligate.createSystemQualifier();
    }

    @Override
    public Qualifier createSystemQualifier(long qualifierId) {
        return deligate.createSystemQualifier(qualifierId);
    }

    @Override
    public void deleteAttribute(long id) {
        deligate.deleteAttribute(id);
    }

    @Override
    public void deleteElement(long id) {
        deligate.deleteElement(id);
    }

    @Override
    public void deleteQualifier(long id) {
        deligate.deleteQualifier(id);
    }

    @Override
    public boolean deleteStream(String path) {
        return deligate.deleteStream(path);
    }

    @Override
    public List<CalculateInfo> findCalculateInfos(String reg,
                                                  boolean autoRecalculate) {
        return deligate.findCalculateInfos(reg, autoRecalculate);
    }

    @Override
    public Attribute getAttribute(long attributeId) {
        Attribute attribute = attributes.get(attributeId);
        if (attribute == null)
            return deligate.getAttribute(attributeId);
        return attribute;
    }

    @Override
    public Attribute getAttributeByName(String attributeName) {
        Attribute attribute = systemAttributes.get(attributeName);
        if (attribute != null)
            return attribute;
        attribute = deligate.getAttributeByName(attributeName);
        if (attribute != null)
            systemAttributes.put(attributeName, attribute);
        return attribute;
    }

    @Override
    public Transaction getAttributePropertyWhatWillBeDeleted(long attributeId) {
        return deligate.getAttributePropertyWhatWillBeDeleted(attributeId);
    }

    @Override
    public AttributeType[] getAttributeTypes() {
        return deligate.getAttributeTypes();
    }

    @Override
    public Transaction[] getAttributeWhatWillBeDeleted(long elementId) {
        return deligate.getAttributeWhatWillBeDeleted(elementId);
    }

    @Override
    public Hashtable<Element, Transaction> getAttributeWhatWillBeDeleted(
            long qualifierId, long attributeId) {
        return deligate.getAttributeWhatWillBeDeleted(qualifierId, attributeId);
    }

    @Override
    public List<Attribute> getAttributes() {
        return new ArrayList<Attribute>(attributes.values());
    }

    @Override
    public Transaction[] getAttributesWhatWillBeDeleted(long elementId,
                                                        List<Attribute> attributes) {
        return deligate.getAttributesWhatWillBeDeleted(elementId, attributes);
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
    public CalculateInfo getCalculateInfo(long elementId, long attributeId) {
        return deligate.getCalculateInfo(elementId, attributeId);
    }

    @Override
    public List<CalculateInfo> getDependences(long elementId, long attributeId,
                                              boolean autoRecalculate) {
        return deligate.getDependences(elementId, attributeId, autoRecalculate);
    }

    @Override
    public Element getElement(long id) {
        CachedElement e = getCachedElement(id);
        if (e == null)
            return null;
        return e.element;
    }

    @Override
    public Element getElement(String elementName, long qualifierId) {
        return deligate.getElement(elementName, qualifierId);
    }

    @Override
    public long getElementCountForQualifier(long qialifierId) {
        CachedQualifier q = getCachedQualifier(qialifierId);
        if (q != null)
            return q.elements.size();
        return 0;
    }

    @Override
    public List<Element> getElements(long qualifierId) {
        CachedQualifier q = getCachedQualifier(qualifierId);
        if (q == null)
            return null;
        return q.getDataAsList();
    }

    @Override
    public List<Element> getElements(long qualifierId, Attribute attribute,
                                     FindObject[] findObjects) {
        return deligate.getElements(qualifierId, attribute, findObjects);
    }

    @Override
    public int getId() {
        return deligate.getId();
    }

    @Override
    public Qualifier getQualifier(long qualifierId) {
        synchronized (loadLock) {
            CachedQualifier q = qualifiers.get(qualifierId);
            if (q != null)
                return q.qualifier.createSaveCopy();
        }
        return deligate.getQualifier(qualifierId);
    }

    @Override
    public Qualifier getQualifierByName(String qualifierName) {
        return deligate.getQualifierByName(qualifierName);
    }

    @Override
    public long getQualifierIdForElement(long elementId) {
        Element element = getElement(elementId);
        if (element != null)
            return element.getQualifierId();
        return -1;
    }

    @Override
    public List<Qualifier> getQualifiers() {
        return deligate.getQualifiers();
    }

    @Override
    public byte[] getStream(String path) {
        return deligate.getStream(path);
    }

    @Override
    public String[] getStreamNames() {
        return deligate.getStreamNames();
    }

    @Override
    public Attribute getSystemAttribute(String attributeName) {
        return deligate.getSystemAttribute(attributeName);
    }

    @Override
    public AttributeType[] getSystemAttributeTypes() {
        return deligate.getSystemAttributeTypes();
    }

    @Override
    @Deprecated
    public List<Attribute> getSystemAttributes() {
        return deligate.getSystemAttributes();
    }

    @Override
    public Qualifier getSystemQualifier(String qualifierName) {
        Qualifier q = systemQualifiers.get(qualifierName);
        if (q == null) {
            q = deligate.getSystemQualifier(qualifierName);
            if (q == null)
                return null;
            systemQualifiers.put(qualifierName, q);
        }
        return q;
    }

    @Override
    @Deprecated
    public List<Qualifier> getSystemQualifiers() {
        return deligate.getSystemQualifiers();
    }

    @Override
    public long nextValue(String sequence) {
        return deligate.nextValue(sequence);
    }

    @Override
    public boolean setBinaryAttribute(long elementId, long attributeId,
                                      Transaction transaction) {
        return deligate.setBinaryAttribute(elementId, attributeId, transaction);
    }

    @Override
    public void setCalculateInfo(CalculateInfo formula) {
        deligate.setCalculateInfo(formula);
    }

    @Override
    public void setElementQualifier(long elementId, long qualifierId) {
        long oldQ = deligate.getQualifierIdForElement(elementId);
        synchronized (loadLock) {
            CachedQualifier q = qualifiers.get(qualifierId);
            if (q != null) {
                qualifiers.remove(qualifierId);
                for (CachedElement e : q.elements)
                    elements.remove(e.element.getId());
            }
            q = qualifiers.get(oldQ);
            if (q != null) {
                qualifiers.remove(oldQ);
                for (CachedElement e : q.elements)
                    elements.remove(e.element.getId());
            }
        }
        aSetElementQualifier(elementId, qualifierId);
    }

    protected void aSetElementQualifier(long elementId, long qualifierId) {
        deligate.setElementQualifier(elementId, qualifierId);
    }

    @Override
    public void setStream(String path, byte[] bytes) {
        deligate.setStream(path, bytes);
    }

    @Override
    public void updateAttribute(Attribute attribute) {
        deligate.updateAttribute(attribute);
    }

    @Override
    public void updateQualifier(Qualifier qualifier) {
        synchronized (loadLock) {
            CachedQualifier q = qualifiers.get(qualifier.getId());
            if (q != null) {
                qualifiers.remove(qualifier.getId());
                for (CachedElement e : q.elements)
                    elements.remove(e.element.getId());
            }
        }
        aUpdateQualifier(qualifier);
    }

    protected void aUpdateQualifier(Qualifier qualifier) {
        deligate.updateQualifier(qualifier);
    }

    @Override
    public void clearCache() {
        synchronized (loadLock) {
            if (cachedData != null) {
                // cachedData.removeCacheHolder(deligate.getActiveBranch());
            } else {
                elements.clear();
                qualifiers.clear();
                attributeData.clear();
                attributes.clear();
                systemAttributes.clear();
                systemQualifiers.clear();
                for (Attribute attribute : deligate.getAttributes())
                    attributes.put(attribute.getId(), attribute);
            }
        }
    }

    public Engine getSource() {
        return deligate;
    }

    @Override
    public long createBranch(long parent, String reason, int type, String module) {
        try {
            return deligate.createBranch(parent, reason, type, module);
        } finally {
            // clearCache();
        }
    }

    @Override
    public long getActiveBranch() {
        return deligate.getActiveBranch();
    }

    @Override
    public void setActiveBranch(long branchToActivate) {
        long old = deligate.getActiveBranch();
        if (cachedData != null)
            cachedData.removeCacheHolder(old);
        deligate.setActiveBranch(branchToActivate);
        if (cachedData != null)
            getCacheFromHolder(cachedData.getCacheHolder(deligate
                    .getActiveBranch()));
    }

    @Override
    public void deleteBranch(long branch) {
        long old = deligate.getActiveBranch();
        if (cachedData != null)
            cachedData.removeCacheHolder(old);
        deligate.deleteAttribute(branch);
        if (cachedData != null)
            getCacheFromHolder(cachedData.getCacheHolder(deligate
                    .getActiveBranch()));
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
    public void createBranch(long parentBranchId, long branchId, String reason,
                             int type, String module) {
        try {
            long old = deligate.getActiveBranch();
            deligate.createBranch(parentBranchId, branchId, reason, type,
                    module);
            if (cachedData != null)
                cachedData.removeCacheHolder(old);
            if (cachedData != null)
                getCacheFromHolder(cachedData.getCacheHolder(deligate
                        .getActiveBranch()));
        } finally {
            // clearCache();
        }
    }

    public void removeBranchCache() {
        if (cachedData != null)
            cachedData.removeCacheHolder(deligate.getActiveBranch());
    }
}
