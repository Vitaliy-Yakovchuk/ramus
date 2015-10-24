package com.ramussoft.pb;

import java.util.Vector;

/**
 * Інтерфейс для роботи з вузлами секторів.
 *
 * @author ZDD
 */

public interface Crosspoint {

    /**
     * Тип вузла один вхід, багато виходів.
     */
    public static final int TYPE_ONE_IN = 0;

    /**
     * Тип вузла один вихід, багато входів.
     */
    public static final int TYPE_ONE_OUT = 1;

    /**
     * Тунулювання сектора відсутнє.
     */

    public static final int TUNNEL_NONE = 2;

    /**
     * Тунулювання сектора прямими дужками.
     */

    public static final int TUNNEL_HARD = 0;

    /**
     * Тунулювання сектора заокругленими дужками.
     */

    public static final int TUNNEL_SOFT = 1;

    /**
     * Тунулювання сектора заокругленими дужками, але при цьому управлінські
     * фактори не розповсюджуються на дочірні звіти.
     */

    public static final int TUNNEL_SIMPLE_SOFT = 3;

    /**
     * Повертає масив усіх секторів, які додаються до точки перетину секторів.
     *
     * @return Масив секторів.
     */

    public void getSectors(Vector<Sector> v);

    /**
     * Повертає тип вузла.
     *
     * @return Тип вузла (TYPE_ONE_IN - один вхід, багато виходів, чи
     * TYPE_ONE_OUT - один вихід, багато виходів).
     */
    public int getType();

    /**
     * Перевіряє, чи сектор являється єдиним входом чи виходом.
     *
     * @param sector сектор, який входить або виходить з вузла.
     * @return true, якщо сектор єдиний, false, якщо сектор не єдиний.
     */

    public boolean isOne(Sector sector);

    /**
     * Видаляє сектор з під’єднаних до вузла, якщо сектор під’єднаний до
     * функціонального блока і на ішому краю сектор має продовження у вигляді
     * обрубка то обрубок також видаляється.
     *
     * @param sector
     *            Сектор який необхідно видалити з вузла.
     */

    //public void remove(Sector sector);

    /**
     * Метод, який додає вхідний сектор в точку.
     *
     * @param sector
     *            Вхідний сектор.
     */

    //public void addIn(Sector sector);

    /**
     * Метод додає вихідний сектор в точку.
     *
     * @param sector
     *            Вихідний сектор.
     */

    //public void addOut(Sector sector);

    /**
     * Перевірка на те чи являється тока перехрестям секторів на різних рівнях.
     *
     * @return <b>true</b> - точка перехрестя, <br>
     * <b>false</b> - точка не перехрехрестя.
     */

    public boolean isDLevel();

    /**
     * Метод перевіряє чи може бути під’єднаний до точки сектор, який виходить з
     * неї.
     *
     * @return <code>true</code> доданий сектор може виходити з точки <br>
     * <code>false</code> доданий сектор не може виходити з точки.
     */

    public boolean isCanAddOut();

    /**
     * Метод перевіряє чи може бути під’єднаний до точки сектор, який входить з
     * неї.
     *
     * @return <code>true</code> доданий сектор може входити в точку <br>
     * <code>false</code> доданий сектор не може входити в точку.
     */

    public boolean isCanAddIn();

    /**
     * Перевіряє чи являється сектор дочірнім сектором точки.
     *
     * @param sector Сам сектор.
     * @return true, якщо сектор дочірній, false, якщо сектор не дочірній.
     */

    public boolean isChild(Sector sector);

    /**
     * Певертає значення протилежного сектора.
     *
     * @param sector Поточний сектор.
     * @return Протилежний сектор, може бути null, якщо протилежного не існує.
     */

    public Sector[] getOppozite(Sector sector);

    /**
     * Метод, перевіряє чи потрібна ще точка, вона не потрібна, якщо просто
     * являється з’єднанням для двох секторів, які розташовані на одному
     * функціональному блоці.
     *
     * @return <code>true</code>, якщо точка не потрібна, <code>false</code>,
     * якщо точка потрібна.
     */

    public boolean isRemoveable();

    /**
     * Метод перевіряє чи являється сектор входом в точку.
     *
     * @param sector Сектор, який може бути входом.
     * @return <code>true</code>, якщо сектор являється входом,
     * <code>false</code>, якщо сектор являється виходом.
     */

    public boolean isIn(Sector sector);

    /**
     * Повертає посилання на глобальне посилання на точку перетину.
     *
     * @return Посилання на точку перетину се
     */

    public long getGlobalId();

    public Sector[] getIns();

    public Sector[] getOuts();
}
