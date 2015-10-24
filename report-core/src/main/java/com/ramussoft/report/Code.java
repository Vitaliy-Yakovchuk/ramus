package com.ramussoft.report;

import java.io.Serializable;

import com.ramussoft.common.Element;

public class Code implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6720606866239200368L;

    private Element element;

    private String code;

    public Code(Element element, String code) {
        this.element = element;
        this.code = code;
    }

    public Element getElement() {
        return element;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

}
