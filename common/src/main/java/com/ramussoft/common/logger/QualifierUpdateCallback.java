package com.ramussoft.common.logger;

import com.ramussoft.common.event.QualifierEvent;

import static com.ramussoft.common.logger.EngineLogExtension.QUALIFIER_ID;

public class QualifierUpdateCallback extends AbstractUpdateEventCallback {

    private final QualifierEvent event;

    public QualifierUpdateCallback(QualifierEvent event) {
        this.event = event;
    }

    @Override
    public void init(Event event) {
        super.init(event);
        event.setAttribute(QUALIFIER_ID, this.event.getQualifier().getId());
        if (this.event.getOldQualifier() == null)
            event.setChangeType(Event.DATA_CREATED);
        else {
            if (this.event.getNewQualifier() == null) {
                if (event.getChangeType() == Event.DATA_CREATED)
                    event.cancel();
                else {
                    event.setChangeType(Event.DATA_REMOVED);
                    event.setOldValue(this.event.getOldQualifier().toString());
                }
            } else {
                event.setChangeType(Event.DATA_UPDATED);
                if (this.event.getOldQualifier().getName().trim().length() > 0)
                    event.setOldValue(this.event.getOldQualifier().toString());
            }
        }
    }

    @Override
    public void update(Event event) {
        if (this.event.getNewQualifier() != null)
            event.setNewValue(this.event.getNewQualifier().toString());
    }

    @Override
    public String getType() {
        return EngineLogExtension.NAME;
    }

    @Override
    public boolean canAddInfo(Event event) {
        return event.type.equals(getType())
                && hasAttribute(event, QUALIFIER_ID, this.event.getQualifier()
                .getId());
    }

}
