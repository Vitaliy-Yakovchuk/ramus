package com.ramussoft.pb.data;

import java.util.Arrays;
import java.util.Vector;

import com.ramussoft.pb.types.GlobalId;
import com.ramussoft.pb.types.GlobalIdComparator;

public class Indexer<E extends Indexed> {

    private Object[] nBuff;

    private int nBuffSize = 0;

    private Vector<E> elements;

    protected long[] orded = null;

    protected Object[] buff = null;

    public Indexer() {
        this(10);
    }

    public Indexer(final int lenght) {
        nBuff = new Object[lenght];
        elements = new Vector<E>(lenght);
    }

    private void flush() {
        if (nBuffSize == 0)
            return;
        for (int i = 0; i < nBuffSize; i++) {
            elements.add((E) nBuff[i]);
        }
        nBuffSize = 0;
        orded = null;
    }

    public synchronized E add(final E e) {
        if (nBuffSize == nBuff.length) {
            for (final Object o : nBuff)
                elements.add((E) o);
            elements.add(e);
            nBuffSize = 0;
            orded = null;
        } else {
            nBuff[nBuffSize] = e;
            nBuffSize++;
        }
        return e;
    }

    public synchronized void remove(final E e) {
        flush();
        elements.remove(e);
        orded = null;
    }

    private void sortMe() {
        if (orded == null) {
            orded = new long[elements.size()];
            buff = new Object[orded.length];
            for (int i = 0; i < orded.length; i++) {
                final E e = elements.get(i);
                orded[i] = e.getGlobalId().getLocalId();
                buff[i] = e;
            }
            Arrays.sort(orded);
            Arrays.sort(buff, new GlobalIdComparator());
        }
    }

    public synchronized E findByGloabalId(final long id) {
        for (int i = 0; i < nBuffSize; i++) {
            final E e = (E) nBuff[i];
            if (e.getGlobalId().getLocalId() == id)
                return e;
        }
        final int r = findInBuff(id);
        if (r < 0)
            return null;
        return (E) buff[r];
    }

    public int findInBuff(final long id) {
        sortMe();
        final int r = Arrays.binarySearch(orded, id);
        if (r < 0 || r >= orded.length)
            return -1;
        return r;
    }

    public synchronized E findByGloabalId(final GlobalId id) {
        return findByGloabalId(id.getLocalId());
    }

    public int size() {
        return elements.size() + nBuffSize;
    }

    public E get(final int i) {
        if (i >= elements.size())
            return (E) nBuff[i - elements.size()];
        return elements.get(i);
    }

    public void clear() {
        elements.clear();
        nBuffSize = 0;
        orded = null;
    }

    public Vector<E> getElements() {
        flush();
        return elements;
    }

    public Object[] toArray() {
        flush();
        return elements.toArray();
    }

    public void sort() {
        flush();
        sortMe();
        elements = new Vector();
        for (final Object o : buff)
            elements.add((E) o);
    }

    public void addFirst(final E o) {
        flush();
        elements.insertElementAt(o, 0);
        orded = null;
    }

}
