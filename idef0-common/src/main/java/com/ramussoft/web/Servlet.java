package com.ramussoft.web;

import java.io.IOException;

public class Servlet {

    public void accept(final Request request, final Response response) throws IOException {
        if (request.isGet())
            doGet(request, response);
        else
            doPost(request, response);
    }

    protected void doPost(final Request request, final Response response) throws IOException {
    }

    protected void doGet(final Request request, final Response response) throws IOException {
    }
}
