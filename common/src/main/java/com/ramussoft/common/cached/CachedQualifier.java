package com.ramussoft.common.cached;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;

public class CachedQualifier {

    Qualifier qualifier;

    Attribute[] allAttributes;

    List<CachedElement> elements = new ArrayList<CachedElement>();

    private Hashtable<Attribute, Integer> indexes;

    public List<Attribute> buildAttributes() {
        List<Attribute> list = new ArrayList<Attribute>();
        list.addAll(qualifier.getAttributes());
        list.addAll(qualifier.getSystemAttributes());
        return list;
    }

    public void setAllAttributes(Attribute[] allAttributes) {
        this.allAttributes = allAttributes;
        indexes = new Hashtable<Attribute, Integer>();
        for (int i = 0; i < allAttributes.length; i++) {
            indexes.put(allAttributes[i], i);
        }
    }

    public int getIndex(Attribute attribute) {
        Integer integer = indexes.get(attribute);
        if (integer == null)
            return -1;
        return integer.intValue();
    }

    public Hashtable<Element, Object[]> getDataAsHash(List<Attribute> attributes) {
        Hashtable<Element, Object[]> res = new Hashtable<Element, Object[]>();
        int[] is = new int[attributes.size()];
        for (int i = 0; i < is.length; i++) {
            is[i] = getIndex(attributes.get(i));
        }

        for (CachedElement element : elements) {
            Object[] objects = new Object[is.length];
            for (int i = 0; i < is.length; i++) {
                objects[i] = element.objects[is[i]];
            }
            res.put(element.element, objects);
        }
        return res;
    }

    public List<Element> getDataAsList() {
        List<Element> result = new ArrayList<Element>(elements.size());
        for (CachedElement e : elements) {
            result.add(e.element);
        }
        return result;
    }

}
