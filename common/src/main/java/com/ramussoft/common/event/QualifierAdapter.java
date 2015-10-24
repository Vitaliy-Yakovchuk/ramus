package com.ramussoft.common.event;

public abstract class QualifierAdapter implements QualifierListener {

    @Override
    public void qualifierUpdated(QualifierEvent event) {
    }

    @Override
    public void qualifierCreated(QualifierEvent event) {
    }

    @Override
    public void qualifierDeleted(QualifierEvent event) {
    }

    @Override
    public void beforeQualifierUpdated(QualifierEvent event) {
    }

}
