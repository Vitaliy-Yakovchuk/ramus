/*
 * Created on 19/10/2004
 */
package com.ramussoft.pb.types;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;

import com.dsoft.utils.DataLoader;
import com.dsoft.utils.DataSaver;
import com.ramussoft.pb.data.Indexed;

/**
 * Клас призначений для ведення унікальних індексів в межах моделі. Індекс
 * складений з двох змінних глобальна (змінна моделі), локальна. Клас
 * призначений для об’єднання моделей з різними глобальнимим зміннимми в одну.
 *
 * @author ZDD
 */
public class GlobalId implements Comparable, Indexed {


    /**
     * Глобалані змінні зарезервовані до числа GLOBAL_RESERVED включно.
     * Зарезервовано.
     */
    public static final int GLOBAL_RESERVED = 100;

    /**
     * Локальні змінні зарезервовані до числа LOCAL_RESERVED включно.
     * Зарезервовано.
     */
    public static final int LOCAL_RESERVED = 1000;

    /**
     * Локальна змінна, заноситься при створенні індексу або програмно.
     */
    private long localId = LOCAL_RESERVED + 1;

    /**
     * Копіює атрибути з переданого аргумента.
     *
     * @param id Змінна значення якої скопіюються в новостворений об’єкт.
     */
    public GlobalId(final GlobalId id) {
        super();
        localId = id.localId;
    }

    /**
     * Конструктор, який ініціалізує глобальну і локальну змінну класу.
     *
     * @param modelId Змінна моделі (глобальна змінна).
     * @param loaclId Локальна змінна.
     */

    public GlobalId(final int modelId, final int localId) {
        super();
        setLocalId(localId);
    }

    /**
     * Простий конструктор, глобальна змінна береться з моделі, локальна
     * ініціалізується мінімально можливим числом.
     */
    public GlobalId() {
        super();
    }

    /**
     * Конструктор який ініціалізує локальну змінну, глобальна береться з
     * моделі.
     *
     * @param i
     */

    public GlobalId(final int i) {
        setLocalId(i);
    }

    public GlobalId(final InputStream stream) throws IOException {
        this();
        loadFromStream(stream);
    }

    /**
     * @return Повертає локальну змінну.
     */
    public long getLocalId() {
        return localId;
    }

    /**
     * @param Заносить значення локальної змінної, при потрубі запам’ятовує
     *                 максимальну внесену локальну змінну.
     */
    public synchronized void setLocalId(final long localId) {
        this.localId = localId;
    }

    /**
     * Порівнює на відповідність з об’єктом. Якщо об’єкт не класу GlobalId або
     * null, виникає помилка.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Object o) {
        if (((GlobalId) o).localId > localId)
            return -1;
        if (((GlobalId) o).localId < localId)
            return 1;
        return 0;
    }

    /**
     * Збереження об’єкту в потік.
     *
     * @param stream Потік.
     * @throws IOException
     */
    public void saveToStream(final OutputStream stream) throws IOException {
        if (localId >= 0) {
            DataSaver.saveInteger(stream, 1);
            DataSaver.saveLong(stream, localId);
        } else
            DataSaver.saveInteger(stream, (int) localId);
    }

    /**
     * Зчитування властивосте об’єкту з потоку.
     *
     * @param stream
     * @throws IOException
     */
    public void loadFromStream(final InputStream stream) throws IOException {
        final int modelId = DataLoader.readInteger(stream);
        if (modelId < 0) {
            setLocalId(-1);
            return;
        }
        if (modelId == 0)
            setLocalId(DataLoader.readInteger(stream));
        if (modelId == 1)
            setLocalId(DataLoader.readLong(stream));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof GlobalId) {
            final GlobalId gi = (GlobalId) obj;
            return localId == gi.localId;
        }
        return false;
    }

    /**
     * Копіює властивості з переданого параметру.
     *
     * @param gi Об’єкт, параметри якого будуть скопійовані.
     */
    public void loadFrom(final GlobalId gi) {
        setLocalId(gi.localId);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Long.toString(localId);
    }

    public boolean isNullRow() {
        return localId == -1;
    }

    public static GlobalId convert(final String string) {
        try {
            final int localId = Integer.parseInt(string);
            final GlobalId res = new GlobalId();
            res.localId = localId;
            return res;
        } catch (final Exception e) {
            return null;
        }
    }

    public static GlobalId create(final int id) {
        return new GlobalId(id);
    }

    public GlobalId getGlobalId() {
        return this;
    }

    @Override
    public int hashCode() {
        return new Long(localId).hashCode();
    }

    public static String toString(final GlobalId id) {
        if (id == null)
            return "-1";
        return id.toString();
    }

    public static GlobalId create(long id) {
        return create((int) id);
    }
}
