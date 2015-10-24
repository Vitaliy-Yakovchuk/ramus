package com.ramussoft.pb.web;

import java.util.Vector;

public class History {

    private final Vector<String> addresses = new Vector<String>();

    private int current = 0;

    public boolean isCanBack() {
        return current > 0;
    }

    public boolean isCanNext() {
        return current < addresses.size() - 1;
    }

    public String getCurrent() {
        if (addresses.size() <= 0)
            return "";
        return addresses.get(current);
    }

    public void go(final String url) {
        if (getCurrent().equals(url))
            return;
        if (isCanNext())
            addresses.setSize(current + 1);
        addresses.add(url);
        current = addresses.size() - 1;
    }

    public String back() {
        current--;
        return getCurrent();
    }

    public String next() {
        current++;
        return getCurrent();
    }
}
