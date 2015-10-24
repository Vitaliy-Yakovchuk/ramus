package com.ramussoft.pb.print.xml;

import java.util.ArrayList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Клас для аналізу запиту, на основі запиту будується дерево.
 *
 * @author zdd
 */

public class Query {

    private ArrayList<Element> elements = new ArrayList<Element>();

    /**
     * Створює новий об'єкт. При створенні даного об'єкту відбувається аналіз
     * XML документа до закриваючого тега query (включно).
     *
     * @throws XMLStreamException
     */

    public Query(XMLEventReader eventReader, XMLEvent firstQueryElementEvent)
            throws XMLStreamException {
        XMLEvent element = firstQueryElementEvent;

        while (eventReader.hasNext()) {
            elements.add(new Element(eventReader, element));
            element = eventReader.nextEvent();
            if ((element.isEndElement())
                    && (element.asEndElement().getName()
                    .equals(ReportGenerator.QUERY)))
                break;
            if (!element.isStartElement()) {
                throw new XMLStreamException(
                        "Error in XML waighted for '<element>' or '</quary>'");
            }
        }
    }
}
