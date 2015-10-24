package com.ramussoft.common.logger;

import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.command.EndUserTransactionCommand;
import com.ramussoft.common.journal.event.JournalEvent;
import com.ramussoft.common.journal.event.JournalListener;

public class EngineLogExtension extends AbstractLogExtension implements
        ElementListener, ElementAttributeListener, AttributeListener,
        QualifierListener, JournalListener {

    public static final String NAME = "engine";

    public static final String ATTRIBUTE_ID = "attribute_id";

    public static final String QUALIFIER_ID = "qualifier_id";

    public static final String ELEMENT_ID = "element_id";

    public final Engine engine;

    public final Journaled journaled;

    public EngineLogExtension(Engine engine, Journaled journaled) {
        this.engine = engine;
        this.journaled = journaled;
    }

    @Override
    public void init(Log log) {
        super.init(log);
        engine.addAttributeListener(this);
        engine.addElementAttributeListener(null, this);
        engine.addElementListener(null, this);
        engine.addQualifierListener(this);
        journaled.addJournalListener(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        engine.removeAttributeListener(this);
        engine.removeElementAttributeListener(null, this);
        engine.removeElementListener(null, this);
        engine.removeQualifierListener(this);
        journaled.removeJournalListener(this);
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public void qualifierCreated(QualifierEvent event) {
        log.fireUpdateEvent(new QualifierUpdateCallback(event) {
            @Override
            public void update(Event event) {
                super.update(event);
                event.setChangeType(Event.DATA_CREATED);
            }
        });
    }

    @Override
    public void qualifierDeleted(QualifierEvent event) {
        log.fireUpdateEvent(new QualifierUpdateCallback(event));
    }

    @Override
    public void qualifierUpdated(QualifierEvent event) {
        log.fireUpdateEvent(new QualifierUpdateCallback(event));
    }

    @Override
    public void beforeQualifierUpdated(QualifierEvent event) {
    }

    @Override
    public void attributeUpdated(AttributeEvent event) {
        log.fireUpdateEvent(new AttributeUpdateCallback(event));
    }

    @Override
    public void attributeCreated(AttributeEvent event) {
        log.fireUpdateEvent(new AttributeUpdateCallback(event));
    }

    @Override
    public void attributeDeleted(AttributeEvent event) {
        log.fireUpdateEvent(new AttributeUpdateCallback(event));
    }

    @Override
    public void beforeAttributeDeleted(AttributeEvent event) {
    }

    @Override
    public void attributeChanged(AttributeEvent event) {
        if (event.getAttribute().isSystem())
            return;
        log.fireUpdateEvent(new ElementEventCallback(event));
    }

    @Override
    public void elementCreated(ElementEvent event) {
    }

    @Override
    public void elementDeleted(final ElementEvent eevent) {
        log.fireUpdateEvent(new AbstractUpdateEventCallback() {

            @Override
            public void update(Event event) {
                event.setChangeType(Event.DATA_REMOVED);
                event.setAttribute(ELEMENT_ID, eevent.getOldElement().getId());
                event.setAttribute(QUALIFIER_ID, eevent.getOldElement().getQualifierId());
                event.setOldValue(eevent.getOldElement().getName());
            }

            @Override
            public String getType() {
                return NAME;
            }

            @Override
            public boolean canAddInfo(Event event) {
                return false;
            }
        });

    }

    @Override
    public void beforeElementDeleted(ElementEvent event) {
    }

    @Override
    public void beforeStore(JournalEvent event) {
    }

    @Override
    public void afterStore(JournalEvent event) {
        if (event.getCommand() instanceof EndUserTransactionCommand) {
            commitGot();
        }
    }

    @Override
    public void afterUndo(JournalEvent event) {
    }

    @Override
    public void afterRedo(JournalEvent event) {
        if (event.getCommand() instanceof EndUserTransactionCommand) {
            commitGot();
        }
    }

    private void commitGot() {
        log.applayEventAnyway();
    }

}
