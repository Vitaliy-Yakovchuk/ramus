/*
 * Created on 11/8/2005
 */
package com.ramussoft.pb;

import java.util.Vector;

import com.ramussoft.pb.types.GlobalId;

/**
 * @author ZDD
 */
public interface MatrixProjection {

    public static int TYPE_ARM = 0;

    public static int TYPE_ADDER = 1;

    public static int TYPE_CHILD = 2;

    public static int TYPE_IDEF0 = 3;

    public static int TYPE_LOGIC = 4;

    public static int TYPE_OWNER = 5;

    public static int TYPE_PARENT = 6;

    public static int TYPE_ROTATER = 7;

    public static int TYPE_SAME = 8;

    public static int TYPE_STREAMS = 9;

    /**
     * @param row Лівий елемент
     * @return Зв’язки всіх батківських елементів (без переданого)
     */

    public Vector<Row[]> getLeftParent(Row row);

    /**
     * @param row Правий елемент
     * @return Зв’язки всіх батківських елементів (без переданого)
     */

    public Vector<Row[]> getRightParent(Row row);

    /**
     * @param row лівий елемент
     * @return всі прив’язані до лівого елементу зв’язки
     */

    public Vector<Row> getLeft(Row row);

    /**
     * @param row Правий елемент
     * @return всі прив’язані до правого елементу зв’язки
     */

    public Vector<Row> getRight(Row row);

    /**
     * Встановлює або знищує з’єднання між правим і лівим елементом
     *
     * @param left  Лівий елемент
     * @param right Правий елемент
     * @return true, якщо з’єднання було встановлено false, якщо розірване
     */

    public boolean setJoin(Row left, Row right);

    /**
     * Перевіряє чи є зв’язок між елементами
     *
     * @param left  Лівий елемент
     * @param right Правий елемент
     * @return true, якщо з’єднання є false, якщо з’єднання не має
     */

    public boolean isJoined(Row left, Row right);

    /**
     * @return Значення лівого клисифікаотра
     */

    public Row getRow1();

    /**
     * @return Значення правого класифікатора
     */

    public Row getRow2();

    /**
     * Перевіряє присутність зв’язків у рядка
     *
     * @param row
     * @return
     */

    public boolean isHere(Row row);

    public GlobalId getGlobalId();

    public String getName();

    public Row[] getLeftRows();

    public boolean isStatic();
}
