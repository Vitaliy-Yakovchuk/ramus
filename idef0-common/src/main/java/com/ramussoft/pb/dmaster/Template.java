package com.ramussoft.pb.dmaster;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;

/**
 * Інтерфейс призначений для створення контексту діаграми з шаблону.
 *
 * @author Яковчук В. В.
 */

public interface Template {

    /**
     * Малює ескіз діаграми.
     *
     * @param graphics2D
     * @param rectangle
     */

    void paint(Graphics2D graphics2D, Rectangle rectangle);

    /**
     * Створює ескіз діаграми.
     *
     * @param function
     * @param dataPlugin
     */

    void createChilds(Function function, DataPlugin dataPlugin);

    void close();

    Function getDiagram();

    void refresh();

    int getDecompositionType();

    void setDecompositionType(int diagramType);
}
