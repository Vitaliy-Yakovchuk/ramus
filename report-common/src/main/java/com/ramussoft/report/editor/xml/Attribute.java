package com.ramussoft.report.editor.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.report.data.Data;

import static com.ramussoft.report.ReportResourceManager.getString;

public class Attribute {

    public static final int TEXT = 0;

    public static final int FONT = 1;

    public static final int INTEGER = 2;

    public static final int FONT_TYPE = 3;

    public static final int TEXT_ALIGMENT = 4;

    public static final int BOOLEAN = 5;

    public static final int PRINT_FOR = 6;

    public static final int BORDER_SIZE = 7;

    public static final int BASE_QUALIFIER = 8;

    public static final int MODEL = 9;

    public static final String BOLD = getString("Bold");

    public static final String SIMPLE = getString("Simple");

    public static final String ITALIC = getString("Italic");

    public static final String BOLD_ITALIC = getString("Bold") + " "
            + getString("Italic");

    public static final String LEFT = getString("Left");

    public static final String CENTER = getString("Center");

    public static final String RIGHT = getString("Right");

    public static final String JUSTIFY = getString("Justify");

    public static final String YES = getString("Yes");

    public static final String NO = getString("No");

    public static final String PRINT_FOR_ALL = getString("printFor.allElements");

    public static final String PRINT_FOR_HAVE_CHILDS = getString("printFor.haveChilds");

    public static final String PRINT_FOR_HAVE_NO_CHILDS = getString("printFor.haveNoChilds");

    public static final String QUALIFIER_DELIMETER = Data.QUALIFIER_DELIMETER;

    private int type;

    private String name;

    private Object value;

    private String title;

    public Attribute(int type, String name, String title) {
        this.type = type;
        this.name = name;
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void storeToXML(ReportSaveXMLReader reader) throws SAXException {
        if (value == null)
            return;
        String valueAsString = getValueAsString();
        if ((valueAsString == null) || (valueAsString.length() == 0))
            return;
        AttributesImpl impl = new AttributesImpl();
        impl.addAttribute("", "name", "name", "CDATA", name);
        reader.startElement("attribute", impl);
        reader.characters(valueAsString);
        reader.endElement("attribute");
    }

    private String getValueAsString() {
        switch (type) {
            case FONT_TYPE:
                if (SIMPLE.equals(value))
                    return "0";
                if (BOLD.equals(value))
                    return "1";
                if (ITALIC.equals(value))
                    return "2";
                if (BOLD_ITALIC.equals(value))
                    return "3";

            case BASE_QUALIFIER:
                return Long.toString(((Qualifier) value).getId());

            case TEXT_ALIGMENT:
                if (LEFT.equals(value))
                    return "left";
                if (CENTER.equals(value))
                    return "center";
                if (RIGHT.equals(value))
                    return "right";
                if (JUSTIFY.equals(value))
                    return "justify";
            case BOOLEAN:
                if (YES.equals(value))
                    return "true";
                if (NO.equals(value))
                    return "false";
            case PRINT_FOR:
                if (PRINT_FOR_ALL.equals(value))
                    return "forAll";
                if (PRINT_FOR_HAVE_CHILDS.equals(value))
                    return "haveChilds";
                if (PRINT_FOR_HAVE_NO_CHILDS.equals(value))
                    return "haveNoChilds";
            default:
                break;
        }
        return value.toString();
    }

    public Object convertXmlAttributeToObject(String string, Engine engine) {
        switch (type) {
            case FONT_TYPE:
                if ("0".equals(string))
                    return SIMPLE;
                if ("1".equals(string))
                    return BOLD;
                if ("2".equals(string))
                    return ITALIC;
                if ("3".equals(string))
                    return BOLD_ITALIC;
            case TEXT_ALIGMENT:
                if ("left".equals(string))
                    return LEFT;
                if ("center".equals(string))
                    return CENTER;
                if ("right".equals(string))
                    return RIGHT;
                if ("justify".equals(string))
                    return JUSTIFY;
            case BOOLEAN:
                if ("true".equals(string))
                    return YES;
                if ("false".equals(string))
                    return NO;
            case PRINT_FOR:
                if ("forAll".equals(string))
                    return PRINT_FOR_ALL;
                if ("haveChilds".equals(string))
                    return PRINT_FOR_HAVE_CHILDS;
                if ("haveNoChilds".equals(string))
                    return PRINT_FOR_HAVE_NO_CHILDS;
                //case MODEL:
                //	return engine.getQualifierByName(string);
            case BASE_QUALIFIER:
                return engine.getQualifier(Long.parseLong(string));

            default:
                break;
        }
        return string;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + type;
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Attribute))
            return false;
        Attribute other = (Attribute) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
