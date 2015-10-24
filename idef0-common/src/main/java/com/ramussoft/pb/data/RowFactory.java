package com.ramussoft.pb.data;

import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.types.GlobalId;

/**
 * Клас приначений для оброки масивів класифікаторів.
 *
 * @author ZDD
 */
public class RowFactory {

    /**
     * Медод, який об’днує два масива класифікаторів в один, всі елементи, які
     * повторються, виклучаються.
     *
     * @param a Масив А.
     * @param b Масив Б.
     * @return Об’єднані масиви А і Б.
     */

    public static Row[] addRows(final Row[] a, final Row[] b) {
        for (Row r1 : a) {
            for (Row r2 : b)
                if (r1 != null && r2 != null) {
                    if (r1.equals(r2)) {
                        r1.setAttachedStatus(r2.getAttachedStatus());
                    }
                }
        }
        int n = a.length + b.length;
        Row[] res;
        int i, j = 0;
        for (i = 0; i < b.length; i++)
            if (isPresent(a, b[i]))
                n--;
        res = new Row[n];
        for (i = 0; i < a.length; i++) {
            res[i] = a[i];
            j++;
        }
        for (i = 0; i < b.length; i++)
            if (!isPresent(a, b[i])) {
                res[j] = b[i];
                j++;
            }
        return res;
    }

    /**
     * Медод, який перевіряє присутність елемента row в масиві a.
     *
     * @param a   Масив елементів.
     * @param row Елемент, наявність необхідно перевірити.
     * @return true, якщо елемент в масиві наявний, false, якщо елемента в
     * масиві не має.
     */
    public static boolean isPresent(final Row[] a, final Row row) {
        for (final Row element : a)
            if (element == row)
                return true;
        return false;
    }

    /**
     * Метод видаляє други масив з першого і повертає перший масив з видаленими
     * елементами першого елементами.
     *
     * @param a Перший масив.
     * @param b Другий масив.
     * @return Перший масив без елементів другого масиву.
     */

    public static Row[] removeRows(final Row[] a, final Row[] b) {
        int length = a.length;
        int i;
        for (i = 0; i < a.length; i++)
            if (isPresent(b, a[i]))
                length--;
        final Row[] res = new Row[length];
        int j = 0;
        for (i = 0; i < a.length; i++)
            if (!isPresent(b, a[i])) {
                res[j] = a[i];
                j++;
            }
        return res;
    }

    /**
     * Медод, який сортує елементи класифікатора за назвою, сортування не
     * залежить від ієрархії елементів.
     *
     * @param rows Елементи, які будуть сортуватись.
     */

    public static void sortByName(final Object[] rows) {
        Arrays.sort(rows, new RowNameComparator<Object>());
    }

    public static void sortByTitle(final Stream[] streams) {
        Arrays.sort(streams, new Comparator<Object>() {

            private final RuleBasedCollator collator = RowNameComparator
                    .getRowSorter();

            public int compare(final Object arg0, final Object arg1) {
                final Stream a = (Stream) arg0;
                final Stream b = (Stream) arg1;
                return collator.compare(a.getName(), b.getName());
            }

        });
    }

    public static Row[] toRows(final Vector v) {
        final Row[] res = new Row[v.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = (Row) v.get(i);
        return res;
    }

    public static long[] toRowIndexes(final Vector v) {
        final long[] res = new long[v.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = ((Row) v.get(i)).getGlobalId().getLocalId();
        return res;
    }

    public static GlobalId[] toGlobalIds(final Vector v) {
        final GlobalId[] res = new GlobalId[v.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = ((Row) v.get(i)).getGlobalId();
        return res;
    }

    public static Object[] toObjectGlobalIds(final Vector v) {
        final Object[] res = new Object[v.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = ((Row) v.get(i)).getGlobalId();
        return res;
    }
}
