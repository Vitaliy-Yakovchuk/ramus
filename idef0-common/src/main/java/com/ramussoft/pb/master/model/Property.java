package com.ramussoft.pb.master.model;

public interface Property {
    public static final int INTEGER = 0;

    public static final int TEXT_FIELD = 1;

    public static final int TEXT = 2;

    public static final int HTML_TEXT = 3;

    public static final int OTHER = 4;

    int getMin();

    int getMax();

    int getType();

    String getName();

    String getKey();

    Object getValue();

    void setValue(Object value);

    boolean isReadOnly();
}
