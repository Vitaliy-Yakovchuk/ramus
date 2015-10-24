package com.ramussoft.common.journal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import com.ramussoft.common.AbstractEngine;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentField;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.common.persistent.PersistentWrapper;
import com.ramussoft.common.persistent.Transaction;

public abstract class AbstractJournaledEngine extends AbstractEngine implements
        Engine {

    protected PluginFactory pluginFactory;

    private String[] implementationClasseNames;

    private HashMap<Long, Boolean> missAttributes = new HashMap<Long, Boolean>();

    @SuppressWarnings("unchecked")
    public AbstractJournaledEngine(PluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
        List<String> classes = new ArrayList<String>();
        classes.add(Journaled.class.getName());
        classes.add(Engine.class.getName());
        for (Plugin plugin : pluginFactory.getPlugins()) {
            Class clazz = plugin.getFunctionalInterface();
            if (clazz != null)
                classes.add(clazz.getName());
        }
        implementationClasseNames = classes.toArray(new String[classes.size()]);
    }

    private class PersistentPare {
        Persistent oldPersistent;
        Persistent newPersistent;
    }

    private void setPersistentAttribute(Element element, Attribute attribute,
                                        Object newObject, AttributeConverter converter) {

        long elementId = (element == null) ? -1 : element.getId();

        long attributeId = attribute.getId();

        List<Persistent>[] object = converter.toPersistens(newObject,
                elementId, attributeId, this);

        for (int i = 0; i < object.length; i++) {
            object[i] = new ArrayList<Persistent>(object[i]);
        }

        for (List<Persistent> list : object) {
            for (Persistent p : list) {
                PersistentRow row = getPersistentMetadata(p.getClass());
                PersistentWrapper wrapper = getWrapper(p.getClass());
                for (PersistentField field : row.getFields()) {
                    if (field.isAutoset()) {
                        if (field.getType() == PersistentField.ATTRIBUTE) {
                            wrapper.setField(p, field.getName(), attributeId);
                        } else if (field.getType() == PersistentField.ELEMENT) {
                            wrapper.setField(p, field.getName(), elementId);
                        } else if (field.getType() == PersistentField.ID) {
                        }/*
                         * else throw new NullPointerException(
						 * "No way to autoset field: " + field.getName() +
						 * " for " + row.getClassName());
						 */
                    }
                }
            }
        }

        long activeBranch = getActiveBranch();
        List<Persistent>[] old = getBinaryBranchAttribute(elementId,
                attributeId, activeBranch);

        Transaction transaction = new Transaction();

        for (PersistentPare pare : findChanges(old, object)) {
            if (pare.newPersistent == null) {
                transaction.getDelete().add(pare.oldPersistent);
            } else if (pare.oldPersistent == null) {
                pare.newPersistent.setValueBranchId(activeBranch);
                transaction.getSave().add(pare.newPersistent);
            } else {
                transaction.getUpdate().add(pare.newPersistent);
                transaction.getOldUpdate().add(pare.oldPersistent);
            }

        }
        setAttribute(elementId, attributeId, transaction);
    }

    public void savePersistent(Persistent persistent, Transaction transaction) {

    }

    private List<PersistentPare> findChanges(List<Persistent>[] oldP,
                                             List<Persistent>[] newP) {
        List<PersistentPare> list = new ArrayList<PersistentPare>(1);
        for (int i = 0; i < oldP.length; i++) {
            List<Persistent> oldList = oldP[i];
            List<Persistent> newList = newP[i];
            for (Persistent p : oldList) {
                PersistentPare pare = new PersistentPare();
                pare.oldPersistent = p;
                int j = getIndex(p, newList);
                if (j >= 0) {
                    pare.newPersistent = newList.get(j);
                    newList.remove(j);
                    if (!equalsAllFields(pare))
                        list.add(pare);
                } else
                    list.add(pare);
            }
            for (Persistent p : newList) {
                PersistentPare pare = new PersistentPare();
                pare.newPersistent = p;
                list.add(pare);
            }
        }
        return list;
    }

    ;

    private int getIndex(Persistent p, List<Persistent> newList) {
        int size = newList.size();
        for (int i = 0; i < size; i++) {
            if (equalsPrimaryKeys(p, newList.get(i)))
                return i;
        }
        return -1;
    }

    private boolean equalsPrimaryKeys(Persistent a, Persistent b) {
        PersistentWrapper wrapper = getWrapper(a.getClass());
        PersistentRow row = getPersistentMetadata(a.getClass());
        List<PersistentField> fields = row.getFields();
        for (PersistentField field : fields)
            if (field.isPrimary()) {
                if (!equals(wrapper.getField(a, field.getName()),
                        wrapper.getField(b, field.getName())))
                    return false;
            }
        return true;
    }

    private boolean equalsAllFields(PersistentPare pare) {
        Persistent a = pare.oldPersistent;
        Persistent b = pare.newPersistent;
        PersistentWrapper wrapper = getWrapper(a.getClass());
        PersistentRow row = getPersistentMetadata(a.getClass());
        for (PersistentField f : row.getFields()) {
            if (!f.isAutoset()) {
                String field = f.getName();
                if (!equals(wrapper.getField(a, field),
                        wrapper.getField(b, field)))
                    return false;
            }
        }
        return true;
    }

    private boolean equals(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        if (b == null)
            return false;
        return a.equals(b);
    }

    public abstract PersistentRow getPersistentMetadata(
            Class<? extends Persistent> key);

    public abstract PersistentWrapper getWrapper(Class<? extends Persistent> key);

    public boolean setAttribute(long elementId, long attributeId,
                                Transaction transaction) {
        return setBinaryAttributeN(elementId, attributeId, transaction);
    }

    protected boolean setBinaryAttributeN(long elementId, long attributeId,
                                          Transaction transaction) {
        return setBinaryAttribute(elementId, attributeId, transaction);
    }

    @Override
    public Hashtable<Element, Object[]> getElements(Qualifier qualifier,
                                                    List<Attribute> attributes) {
        try {
            return convert(
                    getBinaryElements(qualifier.getId(),
                            convertToIds(attributes)), attributes);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private long[] convertToIds(List<Attribute> list) {
        long[] res = new long[list.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = list.get(i).getId();
        }
        return res;
    }

    private Hashtable<Element, Object[]> convert(
            Hashtable<Element, List<Persistent>[][]> res,
            List<Attribute> attributes) {
        AttributeConverter[] converters = new AttributeConverter[attributes
                .size()];
        for (int i = 0; i < converters.length; i++) {
            converters[i] = pluginFactory.getAttributeConverter(attributes.get(
                    i).getAttributeType());
        }
        Hashtable<Element, Object[]> hashtable = new Hashtable<Element, Object[]>();
        for (Entry<Element, List<Persistent>[][]> entry : res.entrySet()) {
            Object[] objects = new Object[converters.length];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = converters[i].toObject(entry.getValue()[i], entry
                        .getKey().getId(), attributes.get(i).getId(), this);
            }

            hashtable.put(entry.getKey(), objects);
        }
        return hashtable;
    }

    @Override
    public Object getAttribute(Element element, Attribute attribute) {
        long elementId = (element == null) ? -1 : element.getId();
        return pluginFactory
                .getAttributeConverter(attribute.getAttributeType()).toObject(
                        getBinaryAttribute(elementId, attribute.getId()),
                        elementId, attribute.getId(), this);
    }

    @Override
    public void setAttribute(Element element, Attribute attribute, Object object) {
        if (element != null)
            if (missAttributes.get(attribute.getId()) == null) {
                missAttributes.put(attribute.getId(), Boolean.TRUE);
            } else
                return;
        try {
            AttributeConverter converter = pluginFactory
                    .getAttributeConverter(attribute.getAttributeType());
            Object old = getAttribute(element, attribute);
            setPersistentAttribute(element, attribute, object, converter);
            String typeName = attribute.getAttributeType().toString();
            if (element != null
                    && (typeName.equals("Core.Text") || typeName
                    .equals("IDEF0.DFDSName"))) {
                Qualifier q = getQualifier(element.getQualifierId());
                if (q.getAttributeForName() == attribute.getId()) {
                    if (object == null)
                        element.setName("");
                    else
                        element.setName(object.toString());
                }
            }
            AttributeEvent event = new AttributeEvent(this, element, attribute,
                    old, object);

            attributeChanged(event);
        } catch (Exception e) {
        }
        if (element != null)
            missAttributes.remove(attribute.getId());

    }

    @Override
    public Object toUserValue(Attribute attribute, Element element, Object value) {
        if (value == null)
            return null;
        AttributePlugin plugin = pluginFactory.getAttributePlugin(attribute
                .getAttributeType());
        if (plugin != null) {
            return plugin.toUserValue(this, attribute, element, value);
        }
        return value;
    }

    @Override
    public String[] getAllImplementationClasseNames() {
        return implementationClasseNames;
    }

    @SuppressWarnings("unchecked")
    public SuperInvoker createSuperInvoker(String[] classes, Class[] clazzes,
                                           Journaled journal) {
        Object[] objects = new Object[classes.length];
        for (int i = 0; i < objects.length; i++) {
            String className = classes[i];
            if (className.equals(Engine.class.getName())) {
                objects[i] = this;
            } else if (className.equals(Journaled.class.getName())) {
                objects[i] = new UserTransactional(journal);
            } else {
                for (Plugin plugin : pluginFactory.getPlugins()) {
                    Class class1 = plugin.getFunctionalInterface();
                    if (class1 != null)
                        if (class1.equals(clazzes[i])) {
                            objects[i] = plugin
                                    .createFunctionalInterfaceObject(this,
                                            getDeligate());
                        }
                }
            }
        }
        return new SuperInvoker(classes, clazzes, objects);
    }

}