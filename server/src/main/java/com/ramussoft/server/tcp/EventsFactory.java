package com.ramussoft.server.tcp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.BranchListener;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.event.StreamListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.command.EndUserTransactionCommand;
import com.ramussoft.common.journal.command.StartUserTransactionCommand;
import com.ramussoft.common.journal.event.JournalEvent;
import com.ramussoft.common.journal.event.JournalListener;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.net.common.tcp.EventHolder;

public class EventsFactory {

    private EvenstHolder holder;

    public EventsFactory(Engine engine) {
        engine
                .addAttributeListener((AttributeListener) createEventbackListener(AttributeListener.class));
        engine
                .addElementAttributeListener(
                        null,
                        (ElementAttributeListener) createEventbackListener(ElementAttributeListener.class));
        engine
                .addQualifierListener((QualifierListener) createEventbackListener(QualifierListener.class));
        engine
                .addStreamListener((StreamListener) createEventbackListener(StreamListener.class));
        engine
                .addElementListener(
                        null,
                        (ElementListener) createEventbackListener(ElementListener.class));
        engine
                .addBranchListener(
                        (BranchListener) createEventbackListener(BranchListener.class));
        ((Journaled) engine).addJournalListener(new JournalListener() {

            private String className = JournalListener.class.getName();

            @Override
            public void beforeStore(JournalEvent event) {
                if ((event.getCommand() instanceof StartUserTransactionCommand)
                        || (event.getCommand() instanceof EndUserTransactionCommand)) {
                    holder.addEventHolder(new EventHolder(className,
                            "beforeStore", new Object[]{event}));
                }
            }

            @Override
            public void afterUndo(JournalEvent event) {
                if ((event.getCommand() instanceof StartUserTransactionCommand)
                        || (event.getCommand() instanceof EndUserTransactionCommand)) {
                    holder.addEventHolder(new EventHolder(className,
                            "afterUndo", new Object[]{event}));
                }
            }

            @Override
            public void afterStore(JournalEvent event) {
                if ((event.getCommand() instanceof StartUserTransactionCommand)
                        || (event.getCommand() instanceof EndUserTransactionCommand)) {
                    holder.addEventHolder(new EventHolder(className,
                            "afterStore", new Object[]{event}));
                }
            }

            @Override
            public void afterRedo(JournalEvent event) {
                if ((event.getCommand() instanceof StartUserTransactionCommand)
                        || (event.getCommand() instanceof EndUserTransactionCommand)) {
                    holder.addEventHolder(new EventHolder(className,
                            "afterRedo", new Object[]{event}));
                }
            }
        });

    }

    public void setEventHolder(EvenstHolder holder) {
        this.holder = holder;
    }

    /**
     * This method removes holder for current thread.
     */

    public EvenstHolder getEventHolder() {
        return holder;
    }

    private Object createEventbackListener(Class<?> clazz) {
        return Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{clazz}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {
                        holder.addEventHolder(new EventHolder(method
                                .getDeclaringClass().getName(), method
                                .getName(), args));
                        return null;
                    }
                });
    }

}
