package com.ramussoft.common.test;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import com.ramussoft.common.Test;

public class TestImpl implements Test {

    @Override
    public void test() {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() != null)
            System.out.println(sc.getAuthentication().getName()
                    + " logged by test");

    }

}
