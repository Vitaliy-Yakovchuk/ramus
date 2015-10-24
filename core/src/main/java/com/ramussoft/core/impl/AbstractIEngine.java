package com.ramussoft.core.impl;

import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;

public abstract class AbstractIEngine implements IEngine {

    private int id;

    public AbstractIEngine(int id) {
        this.id = id;
    }

    @Override
    public Attribute createAttribute(AttributeType attributeType) {
        return createAttribute(-1, attributeType);
    }

    @Override
    public Attribute createSystemAttribute(AttributeType attributeType) {
        return createAttribute(-1, attributeType, true);
    }

    @Override
    public Attribute createAttribute(long attributeId,
                                     AttributeType attributeType) {
        return createAttribute(attributeId, attributeType, false);
    }

    protected abstract Attribute createAttribute(long attributeId,
                                                 AttributeType attributeType, boolean system);

    @Override
    public Qualifier createQualifier() {
        return createQualifier(-1);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Element createElement(long qualifierId) {
        return createElement(qualifierId, -1);
    }

    @Override
    public Qualifier createQualifier(long qualifierId) {
        return createQualifier(qualifierId, false);
    }

    @Override
    public Qualifier createSystemQualifier(long qualifierId) {
        return createQualifier(qualifierId, true);
    }

    @Override
    public Qualifier createSystemQualifier() {
        return createQualifier(-1, true);
    }

    protected abstract Qualifier createQualifier(long qualifierId,
                                                 boolean system);

    @Override
    public List<Qualifier> getQualifiers() {
        return getQualifiers(false);
    }

    @Override
    public List<Qualifier> getSystemQualifiers() {
        return getQualifiers(true);
    }

    protected abstract List<Qualifier> getQualifiers(boolean system);

}
