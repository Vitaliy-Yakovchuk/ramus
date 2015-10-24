package com.ramussoft.server;

import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "hello_world")
public class TestTable {

    String s1;
    String s2;
    String s3;

    long l1;

    @Text(primary = true)
    public String getStringValue1() {
        return s1;
    }

    @Text(primary = true)
    public String getStringValue2() {
        return s2;
    }

    @Text()
    public String getStringValue3() {
        return s3;
    }

    @com.ramussoft.common.persistent.Long()
    public long getLongValue1() {
        return l1;
    }

    public void setStringValue1(String v) {
        s1 = v;
    }

    public void setStringValue2(String v) {
        s2 = v;
    }

    public void setStringValue3(String v) {
        s3 = v;
    }

    public void setLongValue1(long v) {
        l1 = v;
    }

    @Override
    public String toString() {
        return "s1 = " + s1 + ", s2 = " + s2 + ", s3 = " + s3 + ", l1 = " + l1;
    }

}
