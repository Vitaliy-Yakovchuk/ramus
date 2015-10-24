package com.ramussoft.pb.print.web;

import java.io.IOException;

import javax.swing.JOptionPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Main;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.Request;
import com.ramussoft.web.Server;
import com.ramussoft.web.Servlet;

/**
 * Клас, який відкриває порт і чекає звернення на нього web браузера, клас
 * обробляє запити в нових потоках, виділяє змінні відкриває необхідні потоки.
 *
 * @author ZDD
 */

public class HTTPServer extends Server {

    protected boolean error = false;

    protected final DataPlugin dataPlugin;

    protected GUIFramework framework;

    public boolean isError() {
        return error;
    }

    private final String s;

    /**
     * Конструктор створює сервер на необхідному порту, та запускає підпроцес,
     * який чекає звернення клієнтів в нормальному пріорітеті.
     *
     * @param aPort Номер порту, на якому буде запущений сектор.
     * @throws IOException
     */

    public HTTPServer(final String aPort, DataPlugin dataPlugin,
                      GUIFramework framework) {
        super(Integer.parseInt(aPort));
        this.dataPlugin = dataPlugin;
        this.framework = framework;
        s = aPort;
        setPriority(MIN_PRIORITY);
        start();
    }

    @Override
    public void run() {
        try {
            runServer();
        } catch (final IOException e) {
            error = true;
            if (Main.getMainFrame() != null)
                JOptionPane.showMessageDialog(Main.getMainFrame(),
                        ResourceLoader.getString(
                                "error_to_start_server_on_port").replace("{0}",
                                s));
            e.printStackTrace();
        }
    }

    @Override
    protected Servlet getServlet(final Request request) {
        return new HTTPParser(dataPlugin, framework);
    }
}
