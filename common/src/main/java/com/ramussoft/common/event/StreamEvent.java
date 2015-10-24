package com.ramussoft.common.event;

import com.ramussoft.common.Engine;

public class StreamEvent extends Event {

    /**
     *
     */
    private static final long serialVersionUID = 6303926710729945170L;

    private String path;

    private byte[] data;

    public StreamEvent(Engine engine, String path, byte[] data, boolean journaled) {
        super(engine, journaled);
        this.path = path;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }

}
