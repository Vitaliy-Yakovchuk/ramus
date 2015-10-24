package com.ramussoft.pb;

/**
 * Інтерфейс призначений для роботи з потоком (прив’язується до стрілок IDEF0
 * моделі).
 *
 * @author ZDD
 */

public interface Stream extends Row {

    public String getName();

    /**
     * Повертає масив під’єднаних до потоку класифікаторів.
     *
     * @return Масив під’єднаних до потоку класифікаторів, не може бути
     * <code>null</code>.
     */

    Row[] getAdded();

    /**
     * Додає масив рядків до існуючих доданих рядків.
     *
     * @param rows
     *            Масив рядків, що ддається до існуючих доданих рядків, якщо вже
     *            якісь рядки існували раніше, то вони ще раз не додаються.
     */

    // void addRows(Row[] rows);

    /**
     * Видаляє набір доданих рядків.
     *
     * @param rows Масив елементів, які будуть видалені.
     */

    void removeRows(Row[] rows);

    boolean isEmptyName();

    void setRows(Row[] rows);

    // String getTitle();

    void setEmptyName(boolean b);

    /**
     * Додає масив рядків, до під’єднаних до потока.
     *
     * @param rows Масив, що буде доданий.
     */

    void addRows(Row[] rows);

}
