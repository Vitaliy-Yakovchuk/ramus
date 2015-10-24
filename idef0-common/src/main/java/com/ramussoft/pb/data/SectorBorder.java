package com.ramussoft.pb.data;

import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Function;

/**
 * Інтерфейс в, якому міститься інформація про границю сектора, тільки одного
 * сектора.
 *
 * @author ZDD
 */

public interface SectorBorder {
    /**
     * Тип точки, означає, що точка розташована на границі області.
     */

    public static final int TYPE_BORDER = 0;

    /**
     * Тип точки, означає, що точка розташована на краю функціонального блоку.
     */

    public static final int TYPE_FUNCTION = 1;

    /**
     * Тип точки, означає, що точка являється розв’язкою об’єднання декількох
     * стрілок.
     */

    public static final int TYPE_SPOT = 2;

    /**
     * Повертає тип границі сектора.
     *
     * @return TYPE_BORDER - точка розташована на границі функціонального блока,
     * TYPE_SPOT - точка являється вузловою для декількох секторів.
     * TYPE_FUNCTION - точка розташована на границі внутрішнього
     * функціонального блоку.
     */

    public int getType();

    /**
     * Повертає посилання на елемент перетину секторів.
     *
     * @return Вузол секторів.
     */

    public Crosspoint getCrosspoint();

    /**
     * Повертає функцію, на якій знаходиться границя сетора.
     *
     * @return Функціональний блок куди входить/виходить функціональний блок або
     * <code>null</code>, якщо край не на функціональному блоці.
     */

    public Function getFunction();

    /**
     * Повертає сторону, до якої під’єднаний сектор.
     *
     * @return MovingPanel.LEFT...
     */

    public int getFunctionType();

    @Deprecated
    public void setCrosspoint(Crosspoint crosspoint);

    @Deprecated
    public void setFunction(Function function);

    @Deprecated
    public void setFunctionType(int functionType);

    public int getBorderType();

    @Deprecated
    public void setBorderType(int borderType);
}
