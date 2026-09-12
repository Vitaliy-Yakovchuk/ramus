package com.ramussoft.idef0.attribute;

import java.util.Collections;
import java.util.List;

import com.ramussoft.common.IEngine;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.persistent.Persistent;

/**
 * Атрибут зі списком вільних текстових підписів діаграми.
 *
 * @see TextLabelPersistent
 */
public class TextLabelPlugin extends AbstractAttributePlugin {

    @Override
    public boolean isLight() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId,
                                   IEngine engine) {
                List list = persistents[0];
                Collections.sort(list);
                return list;
            }

            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId,
                                                   long attributeId,
                                                   IEngine engine) {
                List<TextLabelPersistent> list = (List) object;
                // Порядок задаємо тут, щоб він не залежав від СУБД.
                for (int i = 0; i < list.size(); i++)
                    list.get(i).setPosition(i);
                return new List[]{list};
            }
        };
    }

    @Override
    public String getTypeName() {
        return "TextLabel";
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{TextLabelPersistent.class};
    }
}
