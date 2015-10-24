package com.ramussoft.pb.print.xml;

import java.util.Enumeration;

/**
 * Інтерфейст призначений для опису генераторів наступних елементів в дереві звіту.
 * Наступні елементи можуть формуватись на основі матричних проекцій, зв'язків IDEF0,
 * атрибутів, табличних атрибутів, дитячих елементів, тощо.
 *
 * @author zdd
 */

public interface TreeMatrixGenerator extends Enumeration<TreeMatrixNode> {

    /**
     * Ініціалізує новий обхід по матричній проекції чи аналогам.
     *
     * @param node Елемент на основі якого робиться обхід.
     */

    void init(TreeMatrixNode node);
}
