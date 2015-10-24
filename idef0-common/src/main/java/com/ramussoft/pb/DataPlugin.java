package com.ramussoft.pb;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.idef0.attribute.DFDSName;
import com.ramussoft.pb.data.negine.NCrosspoint;
import com.ramussoft.pb.types.GlobalId;

/**
 * Інтерфейс, який відповідає за збереження (завантаження) даних з певного
 * середовища. Призначений для того, щоб дані могли завантажуватись з різних
 * середовищ (файл, СУБД...).
 *
 * @author ZDD
 */

public interface DataPlugin {

    /**
     * Індекс для базового класифікатора робіт.
     */
    public static final int STATIC_FUNCTION_LOCAL_ID = 0;

    /**
     * Індекс для падового класифікатори потоків "Потоки".
     */

    public static final int STATIC_STREAM_LOCAL_ID = 1;

    /**
     * Індекс для таблиці, в якій зберігаються назви класифікаторів.
     */

    public static final int CLASIFICALOTOR_NAME_TABLE_ID = 2;

    /**
     * Індек для таблиці, в якій зберігається інформація щодо того, як саме (з
     * яких атрибутів і як) формувати та заносити назви елементів
     * класифікаторів.
     */

    public static final int ROW_NAME_EXTRACTOR_TABLE_ID = 3;

    /**
     * Глобальна константа індексів для статичних елементів класифікатора.
     */

    public static final int STATIC_ROW_GLOBAL_ID = 0;

    public static final String PROPERTY_OUNERS = "OUNERS_IDS";

    /**
     * Метод, який повертає набір записів, дочірніх до запису parent.
     *
     * @param parent  Батьківський запис, якщо параметр <code>null</code>,
     *                повертається набір класифікаторів (елементів класифікатора)
     *                найвищого рівня.
     * @param element <code>true</code>, якщо записи - елементи класифікатора,
     *                <code>false</code>, якщо записи - класифікатора.
     * @return Вектор, в якому зберігаються записи.
     */

    Vector<Row> getChilds(Row parent, boolean element);

    /**
     * Метод рекурсивно повертає набір записів.
     *
     * @param parent  Батьківський запис, якщо параметр <code>null</code>,
     *                повертається набір класифікаторів (елементів класифікатора)
     *                верхнього рівня.
     * @param element <code>true</code>, якщо записи - елементи класифікатора,
     *                <code>false</code>, якщо записи - класифікатора.
     * @return Вектор, в якому зберігаються записи.
     */

    Vector<Row> getRecChilds(Row parent, boolean element);

    Vector<Row> getRecChildren(Qualifier qualifier);

    /**
     * Метод створює навий запис.
     *
     * @param parent  Батьківський елемен запису.
     * @param element <code>true</code>, якщо це елемент класифікатора,
     *                <code>false</code>, якщо це класифікатор.
     * @return Новостворений запис.
     */

    Row createRow(Row parent, boolean element);

    /**
     * Метод видаляє запис.
     *
     * @param row Запис, який буде видалено.
     * @return <code>true</code>, якщо видалення пройшло успішно,
     * <code>false</code>, якщо запис не був видалений.
     */

    boolean removeRow(Row row);

    /**
     * Змінює батьківський запис.
     *
     * @param row    Запис, батьківський, елемент якого буде змінено.
     * @param parent Новий батьківський елемент.
     */

    void setParent(Row row, Row parent);

    /**
     * Метод, який перевіряє, чи являється запис батьківським для запису або
     * батьківським для батьківського запису і т. д.
     *
     * @param row    Запис для якого відбувається перевірка.
     * @param parent Можливий батьківський елемент.
     * @return <code>true</code>, якщо parent батьківськи для row,
     * <code>false</code> інакше.
     */

    boolean isParent(Row row, Row parent);

    /**
     * Повертає рівень запису.
     *
     * @param row Щапис, для якого буде визначено рівень
     * @return Рівень запису в дереві.
     */

    int getLevel(Row row);

    /**
     * Повертає посилання на базовий класифікатор потоків.
     *
     * @return Базовий класифікатор потоків.
     */

    Stream getBaseStream();

    /**
     * Повертає базовий класифікатор робіт.
     *
     * @return Базовий класифікатор робіт.
     */

    Function getBaseFunction();

    Qualifier getBaseFunctionQualifier();

    /**
     * Метод перевіряє, чи являється запис статичним.
     *
     * @param row
     * @return
     */

    // boolean isStatic(Row row);

    /**
     * Метод сортує дочірни елементи по назві.
     *
     * @param row Елементи дочіні якого будуть відсортовані.
     */

    void sortByName(Row row, boolean element);

    /**
     * Повертає рівень елемента класифікатора, без врахування рівня
     * класифікатора елемента
     *
     * @param row Елемент класифікатора, ядл якого буде визначений рівень.
     * @return Рівень елемента класифікатора, без рівня його класифікатора.
     */

    int getElementLevel(Row row);

    /**
     * Метод здійснює пошук запису за назвою
     *
     * @param rowName Назва запису.
     * @return
     */

    Row findRowByName(String rowName);

    Row findRowByGlobalId(GlobalId rowId);

    Row findRowByGlobalId(long rowId);

    Crosspoint findCrosspointByGlobalId(long globalId);

    Sector findSectorByGlobalId(GlobalId globalId);

    void clearAll();

    Crosspoint createCrosspoint();

    /**
     * Повертає потік, в який мають бути занесені двійкові дані. Після запису
     * даних в потік, він обов’язково має бути закритим.
     *
     * @param name Назва двійкових даних.
     * @return Потік для запису даних.
     */

    OutputStream setNamedData(String name);

    /**
     * Повертає потік, з якого можуть бути зчитані двійкові дані.
     *
     * @param name Назва даних.
     * @return Потік, з якого можуть бути зчитані дані. Якщо дані відсутні, може
     * повертатись null.
     */

    InputStream getNamedData(String name);

    /**
     * Видаляє двійкові дані.
     *
     * @param name Назва даних.
     */

    void removeNamedData(String name);

    /**
     * Зберігає дані записані в двійковій системі в файл.
     *
     * @param name Назва даних.
     * @param file Файл в який буде збережена інформація.
     * @return <code>true</code>, якщо дані були скопійовані, <code>false</code>
     * . якщо не були.
     */

    boolean saveNamedDataToFile(String name, File file) throws IOException;

    /**
     * Пов’язує двійкові дані з файлом, з якого вони мають бути завантажені.
     *
     * @param name
     *            Назвіа даних.
     * @param file
     *            Файл, з якого мають бути завантажені дані.
     */

    // void loadNamedDataFromFile(String name, File file);
    /**
     * Повертає масив назв усіх бінарних даних з іменем.
     *
     * @return Масив усіх назв бінарних даних.
     */

    // String[] getNameDataNames();

    /**
     * Повертає права, доступу до запису
     *
     * @param row Посилання на запис.
     * @return Локалізаваний запис, який каже чи заборонений доступ до елемента.
     * <code>null</code> означає, чо доступ дозволений.
     */

    String getRowPerrmision(Row row);

    String getNamedDataPerrmision(String name);

    /**
     * Мотод створює новий статичний запис.
     *
     * @param id Має бути унікальним і < GlobalId.LOCAL_RESERVED
     * @return Новостворений запис.
     */

    Row createStaticRow(int id);

    void saveToFile(File file) throws IOException;

    void loadFromFile(File file) throws IOException;

    /**
     * Повертає шлях дро тимчасової папки, з якою працює дане середрвище.
     *
     * @return Шлях до тимчасової папки.
     */

    String getTmpPath();

    /**
     * Повертає налаштування для проекту (хто автор і т.д. для даної моделі).
     *
     * @return
     */

    Crosspoint createCrosspoint(long id);

    Sector createSector();

    Vector<Sector> getAllSectors();

    /**
     * Метод викликається для того, щоб повідомити про оновлення даних в файлі.
     *
     * @param file
     */

    void updateFile(File file);

    /**
     * Створює нового користувача.
     *
     * @return Інтерфейс для роботи з користувачем.
     */

    User createUser();

    /**
     * Видаляє користувача.
     *
     * @param userId Код користувача.
     */

    void removeUser(int userId);

    User[] getUsers();

    void setUser(User user);

    /**
     * Повідомлення про те, що необхідно створити відмінювач останьої дії.
     *
     * @param sectordata Дані з налаштуванням розміщення секторів на діаграмі.
     */

    String[] getNameDataNames();

    void commit();

    /**
     * Метод створює розпаралену копію моделі.
     *
     * @param base          Базова функція розпаралелення.
     * @param copyAllRows   Чи копіювати всі елементи класифікаторів.
     * @param clearElements Чи очистити функціональний блок після розпаралення.
     * @param file          Файл в який буде створена розпаралена копія моделі.
     * @return <code>false</code>, якщо необхыдно було очитити вміст
     * функціонального блока, але очищення не відбулось (по причині
     * заблокованості відповідних функцій).
     * @throws IOException
     */

    boolean createParalel(Function base, boolean copyAllRows,
                          boolean clearElements, File file, GUIFramework framework,
                          DataPlugin dataPlugin) throws IOException;

    /**
     * Мотод завантажує з файлу частину декомпозицію.
     *
     * @param base          Функціональний блок в який має вставитись функціональнийблок з
     *                      файлу.
     * @param globalIdTable Таблиця взаємозамін елементів, де ключ посилання з фалу,
     *                      значення існуюче посилання на запис (може бути
     *                      <code>null</code>).
     * @param importAll     <code>true</code>, будуть імпортовані всі елементи
     *                      класифікаторів. <code>false</code>, будуть імпортовані лише ті
     *                      елементи класифікакторів, які використовуються на стрілках.
     */

    void loadFromParalel(DataPlugin dataPlugin, Function base, File file,
                         GUIFramework framework) throws IOException;

    public void loadFromParalel(final File file, final GUIFramework framework)
            throws IOException;

    /**
     * Поветає порядковий номер діаграми (так, як вона буде друкуватись).
     */

    public int indexOfFunction(Function function);

    public boolean isStatic(GlobalId id);

    public void registerStatic(GlobalId id);

    public void setProperty(String key, String value);

    public String getProperty(String key);

    public Engine getEngine();

    AccessRules getAccessRules();

    public void clear();

    boolean isReadOnly();

    Hashtable<Long, Sector> getSectorHash();

    RowSet getRowSet(final long id);

    void refresh(GUIFramework framework);

    void exportToIDL(Function baseFunction, OutputStream fileOutputStream,
                     String encoding) throws IOException;

    void importFromIDL(DataPlugin plugin, String encoding,
                       InputStream inputStream) throws IOException;

    public MatrixProjection getFastMatrixProjectionIDEF0(int type,
                                                         Function function);

    Hashtable<Long, NCrosspoint> getCrosspoints();

    Function createFunction(Function activeFunction, int type);

    void compileDFDSName(DFDSName name, Function function);
}
