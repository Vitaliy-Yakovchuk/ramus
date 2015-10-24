package com.ramussoft.pb.idef.visual.event;

import java.util.EventListener;

public interface ActiveFunctionListener extends EventListener {

    void activeFunctionChanged(ActiveFunctionEvent event);

}
