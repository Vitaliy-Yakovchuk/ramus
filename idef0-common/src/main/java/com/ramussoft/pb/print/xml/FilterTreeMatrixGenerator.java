package com.ramussoft.pb.print.xml;

public class FilterTreeMatrixGenerator implements TreeMatrixGenerator {

    private TreeMatrixGenerator generator;

    private int filterType = 0;/* 0 - all, 1 - with_childs, 2 - with_no_childs */

    private TreeMatrixNode next = null;

    public FilterTreeMatrixGenerator(TreeMatrixGenerator generator,
                                     String pattern) {
        this.generator = generator;
        if ((pattern == null) || (pattern.equals("all")))
            filterType = 0;
        else if (pattern.equals("with_childs"))
            filterType = 1;
        else if (pattern.equals("with_no_childs"))
            filterType = 2;
        else
            throw new RuntimeException("Unknown filter type");
        initNext();
    }

    private void initNext() {
        this.next = null;
        TreeMatrixNode next = null;
        while (generator.hasMoreElements()) {
            next = generator.nextElement();
            if (isValid(next)) {
                this.next = next;
                break;
            }
        }
    }

    private boolean isValid(TreeMatrixNode node) {
        try {
            switch (filterType) {
                case 0:
                    return true;
                case 1:
                    return node.getRow().getChildCount() > 0;
                case 2:
                    return node.getRow().getChildCount() == 0;
            }
        } catch (NullPointerException e) {
            throw new RuntimeException(
                    "Can't create filter for not row type TreeMatrixGenerator");
        }
        return false;
    }

    @Override
    public void init(TreeMatrixNode node) {
        generator.init(node);
    }

    @Override
    public boolean hasMoreElements() {
        return next != null;
    }

    @Override
    public TreeMatrixNode nextElement() {
        TreeMatrixNode next = this.next;
        initNext();
        return next;
    }

}
