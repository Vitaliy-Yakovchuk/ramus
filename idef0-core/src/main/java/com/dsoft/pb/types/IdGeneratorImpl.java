package com.dsoft.pb.types;

import java.util.Vector;

public class IdGeneratorImpl implements IdGenerator {

    private final Vector<Range> data = new Vector<Range>();

    private class Range implements Comparable<Long> {
        long a;
        long b;

        public Range(final long id) {
            a = id;
            b = id;
        }

        public Range(final long a, final long b) {
            this.a = a;
            this.b = b;
        }

        public int compareTo(final Long o) {
            if (isIn(o))
                return 0;
            if (a < o)
                return -1;
            return 1;
        }

        boolean isIn(final long i) {
            return a <= i && i <= b;
        }

        @Override
        public String toString() {
            return "(" + a + ", " + b + ")";
        }
    }

    private int binSeach(final long i) {
        return binSeach(0, data.size() - 1, i);
    }

    private int binSeach(final int start, final int end, final long i) {
        if (start >= end)
            return start;
        if (start + 1 == end) {
            if (data.get(start).isIn(i))
                return start;
            return end;
        }
        final int m = (start + end) / 2;
        final Range r = data.get(m);
        int c;
        if ((c = r.compareTo(i)) == 0)
            return m;
        if (c < 0)
            return binSeach(m, end, i);
        return binSeach(start, m, i);
    }

    public void addId(final long id) {
        if (id < 0)
            return;
        if (data.size() == 0) {
            data.add(new Range(id));
            return;
        }
        int index = binSeach(id);
        Range r = data.get(index);
        if (r.isIn(id))
            return;

        Range r1 = null;
        if (index + 1 < data.size())
            r1 = data.get(index + 1);
        if (r.compareTo(id) > 0) {
            index--;
            r1 = r;
            r = data.get(index);
        }
        if (r.b + 1 == id) {
            r.b = id;
            tryAdd(index);
        } else if (r1 != null && r1.a - 1 == id) {
            r1.a = id;
            tryAdd(index);
        } else {
            final Range add = new Range(id);
            data.insertElementAt(add, index + 1);
        }

    }

    private void tryAdd(final int index) {
        if (index + 1 == data.size())
            return;
        final Range a = data.get(index);
        final Range b = data.get(index + 1);
        if (a.b + 1 == b.a) {
            a.b = b.b;
            data.remove(index + 1);
        }
    }

    public long getNextId() {
        long res;
        if (data.size() == 0)
            res = 0;
        else {
            res = data.get(0).b + 1;
        }
        addId(res);
        return res;
    }

    public void clear() {
        data.clear();
    }

    public static void main(final String[] args) {
        new IdGeneratorImpl().test();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final Range r : data) {
            sb.append(r.toString());
            sb.append("; ");
        }
        return sb.toString();
    }

    private void test() {
        data.add(new Range(0, 20));
        data.add(new Range(40, 60));
        data.add(new Range(90, 120));
        data.add(new Range(250, 420));
        binSeachTest();
        data.add(new Range(700, 900));
        System.out.println("---");
        binSeachTest();
        addIdTest(10);
        addIdTest(20);
        addIdTest(21);
        addIdTest(25);
        addIdTest(23);
        addIdTest(24);
        addIdTest(22);
        addIdTest(901);
        long id;
        while ((id = getNextId()) <= 900) {
            System.out.println("Added " + id);
            System.out.println(this);
        }
    }

    private void addIdTest(final int i) {
        System.out.print("adding " + i);
        addId(i);
        System.out.println(toString());
    }

    private void binSeachTest() {
        System.out.println(binSeachTest(50));
        System.out.println(binSeachTest(0));
        System.out.println(binSeachTest(20));
        System.out.println(binSeachTest(10));
        System.out.println(binSeachTest(40));
        System.out.println(binSeachTest(60));
        System.out.println(binSeachTest(50));
        System.out.println(binSeachTest(25));
        System.out.println(binSeachTest(80));
        System.out.println(binSeachTest(90));
        System.out.println(binSeachTest(100));
        System.out.println(binSeachTest(120));
        System.out.println(binSeachTest(160));
        System.out.println(binSeachTest(250));
        System.out.println(binSeachTest(300));
        System.out.println(binSeachTest(420));
        System.out.println(binSeachTest(600));
        System.out.println(binSeachTest(700));
        System.out.println(binSeachTest(800));
        System.out.println(binSeachTest(900));
        System.out.println(binSeachTest(1000));
    }

    private String binSeachTest(final int i) {
        return i + ", index = " + binSeach(i);
    }

    public void addId(final long a, final long b) {
        data.add(new Range(a, b));
    }
}
