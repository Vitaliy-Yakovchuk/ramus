package com.ramussoft.common.logger;

import com.ramussoft.common.event.AttributeEvent;

import static com.ramussoft.common.logger.EngineLogExtension.ATTRIBUTE_ID;

public class AttributeUpdateCallback extends AbstractUpdateEventCallback {

    private final AttributeEvent event;

    public AttributeUpdateCallback(AttributeEvent event) {
        this.event = event;
    }

    @Override
    public void init(Event event) {
        super.init(event);
        event.setAttribute(ATTRIBUTE_ID, this.event.getAttribute().getId());
        if (this.event.getOldValue() == null)
            event.setChangeType(Event.DATA_CREATED);
        else {
            if (this.event.getNewValue() == null) {
                if (event.getChangeType() == Event.DATA_CREATED)
                    event.cancel();
                else {
                    event.setChangeType(Event.DATA_REMOVED);
                    event.setOldValue(this.event.getOldValue().toString());
                }
            } else {
                event.setChangeType(Event.DATA_UPDATED);
                event.setOldValue(this.event.getOldValue().toString());
            }
        }
    }

    @Override
    public void update(Event event) {
        if (this.event.getNewValue() != null)
            event.setNewValue(this.event.getAttribute().toString());
    }

    @Override
    public String getType() {
        return EngineLogExtension.NAME;
    }

    @Override
    public boolean canAddInfo(Event event) {
        return event.type.equals(getType())
                && hasAttribute(event, ATTRIBUTE_ID, this.event.getAttribute()
                .getId());
    }

}
