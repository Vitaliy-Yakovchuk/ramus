package com.ramussoft.common.event;

import java.util.EventListener;

public interface AttributeListener extends EventListener {

    public void attributeUpdated(AttributeEvent event);

    public void attributeCreated(AttributeEvent event);

    public void attributeDeleted(AttributeEvent event);

    public void beforeAttributeDeleted(AttributeEvent event);

}
