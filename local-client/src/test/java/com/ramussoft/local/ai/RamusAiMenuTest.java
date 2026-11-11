package com.ramussoft.local.ai;

import javax.swing.Action;
import org.junit.Test;
import static org.junit.Assert.*;

public class RamusAiMenuTest {
    @Test public void menuCommandResolvesToVisibleName() {
        RamusAiPlugin plugin = new RamusAiPlugin();
        Action action = plugin.getActionDescriptors()[0].getAction();
        String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        assertNotNull(command);
        assertEquals("AI Assistant", plugin.getString(command));
    }
}
