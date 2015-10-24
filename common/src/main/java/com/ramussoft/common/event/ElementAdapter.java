package com.ramussoft.common.event;

public abstract class ElementAdapter implements ElementListener {

    @Override
    public void elementCreated(ElementEvent event) {
    }

    @Override
    public void elementDeleted(ElementEvent event) {
    }

    @Override
    public void beforeElementDeleted(ElementEvent event) {
    }
}
