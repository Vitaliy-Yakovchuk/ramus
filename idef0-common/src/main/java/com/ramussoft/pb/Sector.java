package com.ramussoft.pb;

import java.util.List;

import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.idef0.attribute.SectorPropertiesPersistent;
import com.ramussoft.pb.data.Indexed;
import com.ramussoft.pb.data.negine.NSectorBorder;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.types.GlobalId;

/**
 * Ітерфейс для роботи з секторами.
 *
 * @author ZDD
 */

public interface Sector extends Indexed {

    public static final int VISUAL_COPY_ONE = 0;

    public static final int VISUAL_COPY_STREAM = 1;

    public static final int VISUAL_COPY_ADDED = 2;

    /**
     * Стан створення, коли нічого не треба робити з відображенням (відображення
     * існує і все цілком нормально).
     */

    public static int STATE_NONE = 0;

    /**
     * Стан, коли сектор був створений в результаті продовження стрілки на
     * верхньому рівні.
     */

    public static int STATE_IN = 1;

    /**
     * Стан, коли сектор був створений в результаті виносу стрілки верхній
     * рівень.
     */

    public static int STATE_OUT = 2;

    public void joinSector(Sector sector);

    /**
     * Розбиває сектор на два.
     *
     * @return Повертається новостворений сектор (продовження поточного),
     * створюється відповідна DataCrosspoint...
     */

    public Sector splitSector();

    /**
     * Повертає початок сектора.
     *
     * @return Інформація про поч. сектора.
     */

    public NSectorBorder getStart();

    /**
     * Повертає інф. про кінець сектора.
     *
     * @return Інформація про кінець сектора.
     */

    public NSectorBorder getEnd();

    /**
     * Повертає під’єднаний до сектора потік.
     *
     * @return Потік, який переносить сектор.
     */

    public Stream getStream();

    /**
     * Зазначає, який саме потік переноситься сектором.
     *
     * @param stream Потік, який переносить сектор.
     */

    public void setStream(Stream stream, ReplaceStreamType type);

    /**
     * Зазначає, які саме записи пов’язані з сектором.
     *
     * @param rows Сектори, які пов’язані з сектором.
     */

    public void setRows(Row[] rows);

    /**
     * Метод, який видаляє сектор.
     */

    public void remove();

    /**
     * Повертає глобальний ункальний індекс сектора.
     *
     * @return Індекс сектора.
     */

    public GlobalId getGlobalId();

    /**
     * Повертає елемент класифікатора робіт, на якому розташований сектор.
     *
     * @return Елемент класифікатора робіт.
     */

    public Function getFunction();

    /**
     * Зазначеє елемент класифікатора робіт, на якому розташований сектор.
     *
     * @param function Класифікатор робіт, на якому розташований сектор.
     */

    public void setFunction(Function function);

    /**
     * Метод повертає стан в якому знаходиться сектор. Стан використовується для
     * оперативного створення відображення сектора на діаграмі IDEF0. Наприклад,
     * якщо було здійснено тунулювання, але паралельно, хтось працював з
     * функціональним блоком на нищому рівні, тоді після активізації
     * функціонального блока, автоматично буде створено відображення сектора.
     *
     * @return STATE_NONE, якщо нічого створювати не треба,<br>
     * STATE_IN, якщо необхідно створити по верхньому продовженю (якщо
     * таке є, а якщо не має, то сектор створюється рівно по центру).<br>
     * STATE_OUT, якщо сектор необхідно створити по внутрішньму
     * продовженню, а ні то сектор виходить рівно з краю функціонального
     * блоку.
     */

    public int getCreateState();

    /**
     * Метод, який заносить значення типу необхідного створення відображення.
     *
     * @param createState STATE_NONE, якщо нічого створювати не треба,<br>
     *                    STATE_IN, якщо необхідно створити по верхньому продовженю
     *                    (якщо таке є, а якщо не має, то сектор створюється рівно по
     *                    центру).<br>
     *                    STATE_OUT, якщо сектор необхідно створити по внутрішньму
     *                    продовженню, а ні то сектор виходить рівно з краю
     *                    функціонального блоку. По замовчуванню ставиться стан
     *                    STATE_NONE.
     * @param pos         Позиція для відображення, що буде створене.
     */

    public void setCreateState(int createState, double pos);

    public double getCreatePos();

    /**
     * Перезавантажує налаштування для сектора, при оновленні екрану, необхідно
     * для мережевих версій.
     */

    public void reload();

    /**
     * Повертає масив байт, в якому записана інформація про вигляд сектора (тип
     * стрілки...).
     *
     * @return Мачив в довільному форматі, в залежності від системи
     * відображення.
     */

    public byte[] getVisualAttributes();

    /**
     * Записує в сектор масив байт з відображенням, для подальшого його
     * завантаження.
     *
     * @param visualData Дані з візуальним відображенням.
     */

    public void setVisualAttributes(byte[] visualData);

    /**
     * Метод копіює візуальні параментри для до під’єднаних секторів.
     *
     * @param type <ol>
     *             <li>VISUAL_COPY_ONE - параменри застосовуються лише до
     *             поточного сектора,</li>
     *             <li>VISUAL_COPY_STREAM - параметри застосовуються для сектора
     *             з таким самим потоком,</li>
     *             <li>VISUAL_COPY_ADDED - параметри застосовуються до всіх
     *             під’єднаних секторів.</li>
     *             </ol>
     */

    public void copyVisual(int type);

    /**
     * Метод завантажує перелік атрибутів для свіх і всіх необхідних приєднаних
     * елементів класифікаторів.
     *
     * @param sector Сектор, з якого береться перелік атрибутів.
     * @param start  <code>true</code> - якщо сектор виходить з переданого сектора.<br>
     *               <code>false</code>, якщо сектор приходить в переданий сектор.
     */

    public void loadRowAttributes(Sector sector, boolean start);

    /**
     * Повертає назву сектора на діаграмі.
     *
     * @return Назва сектора.
     */

    public String getName();

    public boolean isConnectedOnFunction(Sector sector);

    public boolean isShowText();

    public void setShowText(boolean showText);

    public void setAlternativeText(String alternativeText);

    public String getAlternativeText();

    public void setSectorPointPersistents(List<SectorPointPersistent> points);

    public List<SectorPointPersistent> getSectorPointPersistents();

    public void setSectorProperties(SectorPropertiesPersistent spp);

    public SectorPropertiesPersistent getSectorProperties();

    void setTextAligment(int textAligment);

    int getTextAligment();
}
