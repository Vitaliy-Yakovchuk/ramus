package com.ramussoft.common.cached;

import java.util.concurrent.BlockingQueue;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;

public class AsyncCachedEngine extends CachedEngine {

    private BlockingQueue<Runnable> queue;

    @SuppressWarnings("unused")
    private CachedData cachedData;

    public AsyncCachedEngine(Engine deligate, BlockingQueue<Runnable> queue) {
        super(deligate, true, true);
        this.queue = queue;
    }

    public void syncCall(final Runnable runnable) {
        final Object wtLock = new Object();
        final Object[] wt = new Object[]{wtLock};
        try {
            queue.put(new Runnable() {

                @Override
                public void run() {
                    runnable.run();
                    synchronized (wtLock) {
                        wtLock.notifyAll();
                        wt[0] = null;
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (wtLock) {
            if (wt[0] != null)
                try {
                    wtLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    public void asyncCall(Runnable runnable) {
        try {
            queue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCache() {
        syncCall(new Runnable() {
            @Override
            public void run() {
                AsyncCachedEngine.super.clearCache();
            }
        });
    }

    @Override
    public void replaceElements(final Element[] oldElements,
                                final Element newElement) {
        syncCall(new Runnable() {
            @Override
            public void run() {
                AsyncCachedEngine.super
                        .replaceElements(oldElements, newElement);
            }
        });
    }

    @Override
    protected CachedAttributeData getCachedAttribute(final Attribute attribute) {
        final CachedAttributeData[] res = new CachedAttributeData[]{null};
        syncCall(new Runnable() {

            @Override
            public void run() {
                res[0] = AsyncCachedEngine.super.getCachedAttribute(attribute);
            }
        });
        return res[0];
    }

    @Override
    protected void aSetAttribute(final Element element,
                                 final Attribute attribute, final Object object) {
        asyncCall(new Runnable() {

            @Override
            public void run() {
                AsyncCachedEngine.super.aSetAttribute(element, attribute,
                        object);
            }
        });
    }

    @Override
    protected void aSetElementQualifier(final long elementId,
                                        final long qualifierId) {
        asyncCall(new Runnable() {

            @Override
            public void run() {
                AsyncCachedEngine.super.aSetElementQualifier(elementId,
                        qualifierId);
            }
        });
    }

    @Override
    protected void aUpdateQualifier(final Qualifier qualifier) {
        asyncCall(new Runnable() {

            @Override
            public void run() {
                AsyncCachedEngine.super.aUpdateQualifier(qualifier);
            }
        });
    }
}
