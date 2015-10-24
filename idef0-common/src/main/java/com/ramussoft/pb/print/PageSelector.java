package com.ramussoft.pb.print;

/**
 * Інтерфейс, яки може використовуватись для вибору набору сторінок, для друку.
 *
 * @author ZDD
 */

public interface PageSelector {

    /**
     * Метод, який повертає масив з номерами сторінок, які необхідно
     * надрукувати.
     *
     * @return Масив номерів сторінок, які будуть друкуватись.
     */

    public int[] getSelected();
}
