package com.ramussoft.client.log;

import java.util.List;

import javax.swing.table.TableModel;

import com.ramussoft.common.Engine;
import com.ramussoft.common.logger.Event;
import com.ramussoft.gui.common.BaseDialog;

public class ElementLogDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -2823151350220703350L;

    public ElementLogDialog(List<Event> events, final Engine engine) {
        LogPanel logPanel = new LogPanel(events) {
            /**
             *
             */
            private static final long serialVersionUID = -130693379417595664L;

            @Override
            protected TableModel createEventModel(List<Event> events) {
                return new ElementEventTableModel(events) {

                    /**
                     *
                     */
                    private static final long serialVersionUID = 4163881336020396740L;

                    @Override
                    protected Engine getEngine() {
                        return engine;
                    }

                };
            }
        };
        setMainPane(logPanel);
    }
}
