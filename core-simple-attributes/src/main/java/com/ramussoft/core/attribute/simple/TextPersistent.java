package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;
import com.ramussoft.common.persistent.Text;

/**
 * Persistent object for string (text) type.
 *
 * @author zdd
 */

@Table(name = "texts", type = TableType.ONE_TO_ONE)
public class TextPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 9175249618708459472L;
    private String value;

    public TextPersistent(String value) {
        this.value = value;
    }

    public TextPersistent() {
    }

    @Text(id = 2)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
