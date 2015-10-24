package com.ramussoft.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LightServer {

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"/com/ramussoft/server/base-content.xml"});

    }

}
