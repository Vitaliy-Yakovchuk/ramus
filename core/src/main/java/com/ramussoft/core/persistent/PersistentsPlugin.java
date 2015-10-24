package com.ramussoft.core.persistent;

import java.util.List;


public interface PersistentsPlugin {

    /**
     * Даний метод має додавати до параметру list класи всіх необхідних
     * JavaBeans які будуть перетворені в таблиці. Атрибут persistentFactory
     * далі може використовуватись модулем для простого завантаження/зберігання
     * даних в об’єкти. Атрибут persistentFactory не може використовуватись в
     * даному методі, так як метод виконується ще до ініціалізації відповідної
     * "фабрики" в даному методі можна лише зберегти переданий атрибут
     * persistentFactory для подальшого використання.
     */
    void addPersistents(List<Class> list, UniversalPersistentFactory persistentFactory);
}
