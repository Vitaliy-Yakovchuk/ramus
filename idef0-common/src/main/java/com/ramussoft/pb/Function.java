package com.ramussoft.pb;

import java.awt.Color;
import java.awt.Font;
import java.util.Date;
import java.util.Vector;

import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.idef.elements.Status;
import com.dsoft.pb.types.FRectangle;

/**
 * Інтерфейс призначений для роботи функціональними блоками IDEF0.
 *
 * @author ZDD
 */

public interface Function extends Row {

    public static final int TYPE_PROCESS_KOMPLEX = 0;

    public static final int TYPE_PROCESS = 1;

    public static final int TYPE_PROCESS_PART = 2;

    public static final int TYPE_OPERATION = 3;

    public static final int TYPE_ACTION = 4;

    public static final int TYPE_EXTERNAL_REFERENCE = 1001;

    public static final int TYPE_DATA_STORE = 1002;

    public static final int TYPE_DFDS_ROLE = 1003;

    public static String IMAGES_DIR = "images";

    /**
     * Повертає значення розмірів функціонального блоку.
     *
     * @return
     */

    FRectangle getBounds();

    /**
     * Повертає колір фону.
     *
     * @return Колір фону
     */

    Color getBackground();

    /**
     * Повертає колір тексту.
     *
     * @return Колір тексту.
     */

    Color getForeground();

    /**
     * Повертає статус.
     *
     * @return Статус.
     */

    Status getStatus();

    /**
     * Повертає шрифт тексту.
     *
     * @return Шрифт тексту.
     */

    Font getFont();

    /**
     * Повертає тип функціонального блоку.
     *
     * @return TYPE_PROCESS_KOMPLEX, TYPE_PROCESS, TYPE_PROCESS_PART = 2
     * TYPE_OPERATION, TYPE_ACTION = 4, TYPE_EXTERNAL = 5,
     * TYPE_DATA_STORE = 6.
     */

    int getType();

    /**
     * Повертає власника для функціонального блоку якщо для цього він
     * <code>null</code>, тоді повертається власник батьківсько функ. блоку.
     *
     * @return Власника для функціонального блоку якщо для цього він
     * <code>null</code>, тоді повертається власник батьківсько функ.
     * блоку.
     */

    Row getOwner();

    /**
     * Задає координати функ. блоку.
     *
     * @param rectangle Нові координати функ. блоку.
     */

    void setBounds(FRectangle rectangle);

    /**
     * Задає колір фону.
     *
     * @param color Новий колір фону.
     */

    void setBackground(Color color);

    /**
     * Задає колір тексту.
     *
     * @param color Новий колір тексту.
     */

    void setForeground(Color color);

    /**
     * Задає статус для функціонального блоку.
     *
     * @param status Новий статус.
     */

    void setStatus(Status status);

    /**
     * Задає шрифт для функціонального блоку.
     *
     * @param font Новий шрифт.
     */

    void setFont(Font font);

    /**
     * Задає тип функціонального блоку.
     *
     * @param type Тип функціонального блоку.
     */

    void setType(int type);

    /**
     * Задає власника для функціонального блоку.
     *
     * @param ouner Власник функціонального блоку.
     */

    void setOwner(Row ouner);

    Row[] getOwners();

    void setLookForChildrens();

    void setLookForChildrensBackground();

    void setLookForChildrensForeground();

    void setLookForChildrensFont();

    public byte[] getSectorData();

    public void setSectorData(byte[] data);

    /**
     * Повертає набір секторів, які розташовані на функціональному блоці.
     *
     * @return Набір секторів, які розташовані на функціональному блоці.
     */

    Vector<Sector> getSectors();

    /**
     * Метод блокує роботу з контекстною діаграмою функціонального блоку.
     */

    public boolean lock();

    /**
     * Метод розблоковує роботу з контекстною діаграмою функціонального блоку.
     */

    public void unlock();

    /**
     * Метод перевіряє чи заблокований функціональний блок.
     *
     * @return <code>true</code> - функціональний блок в даний момент
     * заблокований, <code>false</code> - функціональний блок не
     * заблокований в даний момент.
     */

    public boolean isLocked();

    // void addSector(Sector sector);

    // void removeSector(Sector sector);

    public int getDecompositionType();

    public void setDecompositionType(int type);

    ProjectOptions getProjectOptions();

    void setProjectOptions(ProjectOptions project);

    String getAuthor();

    Date getCreateDate();

    Date getRevDate();

    Date getSystemRevDate();

    String getPageSize();

    void setPageSize(String pageSize);

    void setAuthor(String author);

    void setCreateDate(Date date);

    void setRevDate(Date date);

    void setSystemRevDate(Date date);

    long getLink();

    void setLink(long link);

    String getNativeName();

    void setNativeName(String name);

    boolean isHaveRealChilds();

    int getRealChildCount();

    Function getRealChildAt(int i);

    /**
     * Can be <code>null</code>
     */
    String getTerm();
}
