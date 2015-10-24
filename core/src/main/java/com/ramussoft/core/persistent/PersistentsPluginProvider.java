package com.ramussoft.core.persistent;

import java.util.List;

import com.ramussoft.common.PluginProvider;

/**
 * Посередник для додавання власних таблиць, шляхом реалізації даного інтерфейсу
 * і додавання до нього відповідних класів JavaBeans. Для більшості випадків
 * треба помістити в папку META-INF/services jar файлу текстовий файл з назвою
 * com.ramussoft.core.persistent.PersistentsPluginProvider і в ньому з кожної
 * наступної стрічки передати повний шлях до класу з конструктором без
 * аргументів реалізацією даного інтерфейсу. Метод завантаження може також
 * відрізнятись, наприклад для сервера може використовуватись поле
 * PersistentPluginsProvider, де через кому перераховані усі аналогічні класи.
 * <p/>
 * Також може використовуватись вже існуюча система з {@link PluginProvider}
 * (пріоритетний метод, де це можливо) для цього просто реалізація
 * {@link PluginProvider} має також реалізувати даний інтерфейс.
 */

public interface PersistentsPluginProvider {
    public List<PersistentsPlugin> getPersistentsPlugins();
}
