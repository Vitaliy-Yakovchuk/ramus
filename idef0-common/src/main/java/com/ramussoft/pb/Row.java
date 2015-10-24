package com.ramussoft.pb;

import javax.swing.tree.MutableTreeNode;

import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import com.ramussoft.pb.data.Indexed;
import com.ramussoft.pb.types.GlobalId;

/**
 * Інтерфейс - запис в дереві.
 *
 * @author ZDD
 */

public interface Row extends MutableTreeNode, Comparable, Indexed {

    public static String ROW_HTML_DIR = "rows";

    public static final int TYPE_ROW = 0;

    public static final int TYPE_FUNCTION = 1;

    public static final int TYPE_STREAM = 2;

    /**
     * Метод повертає назву запису.
     *
     * @return Назва запису.
     */

    String getName();

    /**
     * Метод задає назву для запису.
     *
     * @param name Назва запису, якщо така назва вже існує, то може генеруватись
     *             помилка.
     */

    void setName(String name);

    /**
     * Метод перевіряє чи може бути задане сначення назви елемента класифікатора
     * текстовим записом.
     *
     * @return
     */

    boolean isCanSetName();

    /**
     * Повертає номер запису на свому рівні.
     *
     * @return Номер запису на свому рівні (Починається з 1).
     */

    int getId();

    /**
     * Перевіряє, чи являється запис елементом класифікатора.
     *
     * @return <code>true</code>, якщо запис елемент класифікатора,
     * <code>false</code>, якщо запис - класифікатор.
     */

    boolean isElement();

    /**
     * Метод, який повертає html текст (безПочатвоваго і кінцевого тегів
     * сторінки), підказки до запису.
     *
     * @param big <code>true</code>, напис класифікатора не поміщається в
     *            поле виводу, <code>false</code>, напис поміщається в поле
     *            виводу.
     * @return Може повертати <code>null</code>, якщо підказка відсутня.
     */

    String getToolTipText(boolean big);

    /**
     * Перевіряє, чи може в записа бути змінений батьківський елемент.
     *
     * @return <code>true</code>, батьківський елемент може бути змінений,
     * <code>false</code>, батьківський елемент не може бути
     * змінений.
     */

    boolean isMoveable();

    /**
     * Перевіряє, чи може бути рядок знищений.
     *
     * @return <code>true</code>, рядок може бути знищений,
     * <code>false</code>, рядок не може бути знищений.
     */

    boolean isRemoveable();

    /**
     * Перевіряє, чи може рядок мати дітей при чому, це діє тільки для
     * класифікаторів.
     *
     * @return <code>true</code> - може, <code>false</code> - не може.
     */

    boolean isCanHaveChilds();

    /**
     * Повертає код запису.
     *
     * @return Код запису.
     */

    String getKod();

    /**
     * Перевіряє, чи може бути змінений запис.
     *
     * @return <code>true</code>, запис може бути змінений.
     * <code>false</code> запис не може бути змінений.
     */

    boolean isEditable();

    /**
     * Метод повертає посилання на батьківський запис.
     *
     * @return Батьківський запис (те ж саме ще і getParent(), тільки
     * повертається тип Row).
     */

    Row getParentRow();

    /**
     * Задає значення нового батьківського елемента.
     *
     * @param parent Новий батьківський елемент.
     */

    void setParentRow(Row parent);

    /**
     * Задає значення нового батьківського елемента.
     *
     * @param parent
     *            Значення нового батьківського елемента.
     * @param pos
     *            Позиція (номер id) серед дочірніх елементів до нового
     *            батьківського (починаючи з нуля) метод getId() визначається як
     *            pos+1.
     */

    //void setParent(Row parent, int pos);

    /**
     * Повертає кількість дочірніх елементів.
     *
     * @param element <code>true</code> - елементи зліва, <code>false</code> -
     *                елементи зправа.
     * @return
     */

    int getChildCount(boolean element);

    GlobalId getGlobalId();

    boolean isHaveChilds();

    /**
     * Повертає тип запису.
     *
     * @return TYPE_ROW - запис простий елемент,<br>
     * TYPE_FUNCTION - функція,<br>
     * TYPE_STREAM - потік.
     */

    int getRowType();

    void sortByName(boolean element);

    Row getRecParent();

    /**
     * Метод задає алгоритм за яким буде формуватись назва елемента з атрибутів.
     *
     * @param version         Версія алгоритма. Найпростіший спосіб (версія 1) просто
     *                        зазначається ід таблиці текстового атрибута.
     * @param extractAlgoritm
     */

    void setNameExtractor(int version, String extractAlgoritm);

    Qualifier getQualifier();

    Element getElement();

    String getAttachedStatus();

    void setAttachedStatus(String status);
}
