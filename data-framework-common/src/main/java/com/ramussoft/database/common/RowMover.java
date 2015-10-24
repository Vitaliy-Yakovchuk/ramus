package com.ramussoft.database.common;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Metadata;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;

/**
 * Клас призначений для зміни структури групи елементів класифікаторів
 * (переміщення, пониження/підвищення рівня, тощо).
 *
 * @author Яковчук В. В.
 */

public class RowMover {

    private final Row base;

    private final Row[] group;

    private int[] selected;

    private AccessRules accessor;

    private RowSet rowSet;

    class Setter {
        Row row;

        HierarchicalPersistent hp;

        void set() {
            row.setHierarchicalPersistent(hp);
        }
    }

    /**
     * Створення нового об’єкта.
     *
     * @param base     Базовий елемент для елементів (зазвичай класифікатор).
     * @param group    Група всіх елементів класифікатора в таблиці (так, як
     *                 відображаються). Тобто, якщо в таблиці елементи звернуті до
     *                 даний масив буде менший.
     * @param selected Масив індексів елементів класифікаторів, що виділені
     *                 (інденксів масиву group).
     */

    public RowMover(final Row base, final Row[] group, final int[] selected,
                    AccessRules accessor, RowSet rowSet) {
        this.group = group;
        this.selected = selected;
        this.base = base;
        this.accessor = accessor;
        this.rowSet = rowSet;
    }

    public void moveRight() {
        try {
            rowSet.startUserTransaction();
            remChilds();
            for (final int i : selected) {
                if (i >= 0) {
                    final Row row = group[i];
                    final Row par = row.getParent();
                    final int index = getIndex(par, row);
                    if (index > 0) {
                        final Row newParent = getChild(par, index - 1);
                        row.setParent(newParent);
                    }
                }
            }
        } finally {
            rowSet.commitUserTransaction();
        }
    }

    public void insert(final int position) {
        try {
            rowSet.startUserTransaction();
            Row parent = null;
            int pos;
            if (position == 0) {
                parent = base;
                pos = -1;
            } else {
                if (position < group.length - 1
                        && group[position - 1].equals(group[position]
                        .getParent())) {
                    parent = group[position - 1];
                    pos = -1;
                } else {
                    parent = group[position - 1].getParent();
                    pos = getIndex(parent, group[position - 1]);
                }
            }

            if (!isOk(parent))
                return;

            remChilds();

            int len = 0;
            for (int i : selected)
                if (i >= 0)
                    len++;

            int[] is = new int[len];

            len = 0;
            for (int i : selected)
                if (i >= 0) {
                    is[len] = i;
                    len++;
                }

            selected = is;

            if (selected.length == 0)
                return;

            for (int i : selected) {
                Row next = group[i];
                Setter s = copyUp(next);
                if (s != null)
                    s.set();
            }

            Row first = group[selected[0]];
            Row next = null;
            int p = pos + 1;
            while (p < parent.getChildCount()) {
                if (!inGroup(parent.getChildAt(p))) {
                    next = parent.getChildAt(p);
                    break;
                }
                p++;
            }
            HierarchicalPersistent old = first.getHierarchicalPersistent();
            HierarchicalPersistent hp = new HierarchicalPersistent(old);
            if ((parent.getChildCount() > 0) && (pos >= 0)) {
                Row prev = parent.getChildAt(pos);
                hp.setPreviousElementId(prev.getElementId());
            } else
                hp.setPreviousElementId(-1l);
            hp.setParentElementId(parent.getElementId());
            // if (!old.equals(hp))
            first.setHierarchicalPersistent(hp);
            Row last = group[selected[selected.length - 1]];
            if (next != null) {
                old = next.getHierarchicalPersistent();
                hp = new HierarchicalPersistent(old);
                hp.setPreviousElementId(last.getElementId());
                // if (!old.equals(hp))
                next.setHierarchicalPersistent(hp);
            }

            for (int i = 1; i < selected.length; i++) {
                Row s = group[selected[i]];
                Row f = group[selected[i - 1]];
                old = s.getHierarchicalPersistent();
                hp = new HierarchicalPersistent(old);
                hp.setPreviousElementId(f.getElementId());
                hp.setParentElementId(parent.getElementId());
                // if (!old.equals(hp))
                s.setHierarchicalPersistent(hp);
            }

            // for (Setter s : setters)
            // s.set();

        } finally {
            rowSet.commitUserTransaction();
        }
    }

    private Setter copyUp(Row next) {
        if (next.getHierarchicalPersistent() == null)
            return null;
        Row parent = next.getParent();
        int index = parent.getIndex(next);
        if (index + 1 < parent.getChildCount()) {
            Row nn = parent.getChildAt(index + 1);
            if (inGroup(nn))
                return null;
            long prev = -1l;
            for (int i = index - 1; i >= 0; i--) {
                Row childAt = parent.getChildAt(i);
                if (!inGroup(childAt)) {
                    prev = childAt.getElementId();
                    break;
                }
            }
            HierarchicalPersistent hp = nn.getHierarchicalPersistent();
            if (hp == null)
                hp = new HierarchicalPersistent();
            hp.setPreviousElementId(prev);
            Setter setter = new Setter();
            setter.hp = hp;
            setter.row = nn;
            return setter;
        }
        return null;
    }

    private boolean inGroup(Row row) {
        for (int i : selected) {
            if (group[i].equals(row))
                return true;
        }
        return false;
    }

    /**
     * Видаляємо з виділених елементів, всі дочірні елементи, так, як
     * переміщення відбувається гілкою.
     */

    private void remChilds() {
        for (int i = 0; i < selected.length - 1; i++) {
            if (selected[i] >= 0)
                for (int j = i + 1; j < selected.length; j++)
                    if (selected[j] >= 0) {
                        if (isParent(group[selected[j]], group[selected[i]]))
                            selected[j] = -1;
                    }
        }
    }

    private boolean isParent(Row row, final Row parent) {
        if (row == null)
            return false;
        if (parent == null)
            return true;
        while (row != null) {
            row = row.getParent();
            if (row == parent)
                return true;
        }
        return false;
    }

    /**
     * Перевірка чи е намагаємось ми зробити елементи дочірніми до дочірніх
     * елементів, а також чи можемо ми редагувати запис.
     */

    private boolean isOk(final Row parent) {
        for (final int index : selected) {
            final Row row = group[index];

            if (row.equals(parent) || isParent(parent, row))
                return false;
            if (!accessor.canUpdateElement(row.getElementId(), row
                    .getRowAttributes()[0].getId()))
                return false;

        }
        return parent.canAddChild();
    }

    public void on(final int position) {
        final Row parent = group[position];
        if (!isOk(parent))
            return;
        try {
            rowSet.startUserTransaction();
            remChilds();
            /*
             * for (int i = selected.length - 1; i >= 0; i--) if (selected[i] >=
			 * 0) { group[selected[i]].setParent(parent); }
			 */
            for (int i = 0; i < selected.length; i++)
                if (selected[i] >= 0) {
                    group[selected[i]].setParent(parent);
                }
        } finally {
            rowSet.commitUserTransaction();
        }
    }

    private int getIndex(final Row parent, final Row row) {
        return parent.getIndex(row);
    }

    private Row getChild(final Row row, final int index) {
        if (row != null)
            return row.getChildAt(index);
        return null;
    }

    public static boolean isEducational() {
        if (Metadata.EDUCATIONAL)
            return true;
        else
            return false;
    }
}
