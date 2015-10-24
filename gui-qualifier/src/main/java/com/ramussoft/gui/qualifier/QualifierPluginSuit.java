package com.ramussoft.gui.qualifier;

import java.util.List;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.gui.StandardFilePlugin;
import com.ramussoft.gui.attribute.BooleanAttributePlugin;
import com.ramussoft.gui.attribute.CurrencyAttributePlugin;
import com.ramussoft.gui.attribute.DateAttributePlugin;
import com.ramussoft.gui.attribute.DoubleAttributePlugin;
import com.ramussoft.gui.attribute.ElementListPlugin;
import com.ramussoft.gui.attribute.FileAttributePlugin;
import com.ramussoft.gui.attribute.HTMLTextAttributePlugin;
import com.ramussoft.gui.attribute.LongAttributePlugin;
import com.ramussoft.gui.attribute.OtherElementPlugin;
import com.ramussoft.gui.attribute.PriceAttributePlugin;
import com.ramussoft.gui.attribute.TablePlugin;
import com.ramussoft.gui.attribute.TextAttributePlugin;
import com.ramussoft.gui.attribute.VariantAttributePlugin;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.elist.ElistPlugin;
import com.ramussoft.gui.eval.EvalGUIPlugin;

public class QualifierPluginSuit {

    public static void addPlugins(List<GUIPlugin> list, Engine engine,
                                  AccessRules rules) {
        list.add(new QualifierPlugin());
        if (!Metadata.EDUCATIONAL)
            list.add(new ElistPlugin());
        list.add(new TextAttributePlugin());
        list.add(new ElementListPlugin());
        list.add(new VariantAttributePlugin());
        list.add(new OtherElementPlugin());
        list.add(new DoubleAttributePlugin());
        list.add(new LongAttributePlugin());
        list.add(new DateAttributePlugin());
        list.add(new CurrencyAttributePlugin());
        list.add(new BooleanAttributePlugin());
        list.add(new PriceAttributePlugin());
        if (!Metadata.EDUCATIONAL) {
            list.add(new FileAttributePlugin());
            list.add(new TablePlugin());
            list.add(new HTMLTextAttributePlugin());
            list.add(new EvalGUIPlugin());
            list.add(new QualifierHistoryPlugin());
        }
        list.add(new StandardFilePlugin());
    }

}
