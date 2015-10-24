package com.ramussoft.gui.common;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;

public interface QualifierImporter {

    Qualifier getDestination(Qualifier source);

    Attribute getDestination(Attribute source);

    Element getDestination(Element source);

    Object getSourceValue(Element element, Attribute attribute);

    Qualifier[] getSourceQualifiers();

    Element getDestinationElement(long elementId);

    Attribute getDestinationAttribute(long attributeId);

    Engine getSource();

}
