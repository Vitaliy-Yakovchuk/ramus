package com.ramussoft.common.event;

import java.util.EventListener;

public interface ElementAttributeListener extends EventListener {

    public void attributeChanged(AttributeEvent event);

}
