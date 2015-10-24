package com.ramussoft.pb.idef.visual.event;

import com.ramussoft.pb.Function;

public class ActiveFunctionEvent {

    private Function function;

    public ActiveFunctionEvent(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
