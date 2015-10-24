package com.ramussoft.common.event;

import java.util.EventListener;

public interface ElementListener extends EventListener {

    void elementCreated(ElementEvent event);

    void elementDeleted(ElementEvent event);

    void beforeElementDeleted(ElementEvent event);

}
