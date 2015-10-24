package com.ramussoft.core.attribute.simple;

import java.util.List;

import com.ramussoft.common.Plugin;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.attribute.AttributePluginProvider;
import com.ramussoft.core.attribute.standard.AutochangePlugin;
import com.ramussoft.core.attribute.standard.EvalPlugin;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.eval.FunctionPlugin;

public class SimpleAttributePluginSuit extends AttributePluginProvider {

    @Override
    public AttributePlugin[] getAttributePlugins() {
        return new AttributePlugin[]{new TextPlugin(), new DoublePlugin(),
                new LongPlugin(), new DatePlugin(), new BooleanPlugin(),
                new OtherElementPlugin(), new ElementListPlugin(),
                new VariantPlugin(), new FilePlugin(), new HTMLTextPlugin(),
                new TablePlugin(), new CurrencyPlugin(), new PricePlugin()};
    }

    @Override
    public AttributePlugin[] getSystemAttributePlugins() {
        return new AttributePlugin[]{new HierarchicalPlugin(),
                new IconPlugin(), new FunctionPlugin()};
    }

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> res = super.getPlugins();
        res.add(1, new StandardAttributesPlugin());
        res.add(new AutochangePlugin());
        res.add(new EvalPlugin());
        return res;
    }
}
