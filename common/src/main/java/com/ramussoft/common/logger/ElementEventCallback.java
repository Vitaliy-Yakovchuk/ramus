package com.ramussoft.common.logger;

import com.ramussoft.common.event.AttributeEvent;

import static com.ramussoft.common.logger.EngineLogExtension.ATTRIBUTE_ID;
import static com.ramussoft.common.logger.EngineLogExtension.ELEMENT_ID;
import static com.ramussoft.common.logger.EngineLogExtension.QUALIFIER_ID;

public class ElementEventCallback extends AbstractUpdateEventCallback {

    private final AttributeEvent attributeEvent;

    public ElementEventCallback(AttributeEvent attributeEvent) {
        this.attributeEvent = attributeEvent;
    }

    @Override
    public void init(Event event) {
        super.init(event);
        event.setChangeType(Event.DATA_UPDATED);
        event.setAttribute(ELEMENT_ID, attributeEvent.getElement().getId());
        event.setAttribute(ATTRIBUTE_ID, attributeEvent.getAttribute().getId());
        event.setAttribute(QUALIFIER_ID, attributeEvent.getElement()
                .getQualifierId());
        event.setOldValue(toString(event, attributeEvent.getOldValue()));
    }

    private String toString(Event event, Object value) {
        if (value == null)
            return null;
        value = event.log.engine.toUserValue(attributeEvent.getAttribute(),
                attributeEvent.getElement(), value);
        if (value == null)
            return null;
        return value.toString();
    }

    @Override
    public void update(Event event) {
        event.setNewValue(toString(event, attributeEvent.getNewValue()));
    }

    @Override
    public String getType() {
        return EngineLogExtension.NAME;
    }

    @Override
    public boolean canAddInfo(Event event) {
        return event.type.equals(getType())
                && hasAttribute(event, ATTRIBUTE_ID, attributeEvent
                .getAttribute().getId())
                && hasAttribute(event, ELEMENT_ID, attributeEvent.getElement()
                .getId());
    }

}
