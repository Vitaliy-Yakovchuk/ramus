package com.ramussoft.idef0;

import java.util.List;

import com.ramussoft.common.Plugin;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.attribute.AttributePluginProvider;
import com.ramussoft.idef0.attribute.AnyToAnyPlugin;
import com.ramussoft.idef0.attribute.ColorPlugin;
import com.ramussoft.idef0.attribute.DFDSNamePlugin;
import com.ramussoft.idef0.attribute.DecompositionTypePlugin;
import com.ramussoft.idef0.attribute.FRectanglePlugin;
import com.ramussoft.idef0.attribute.FontPlugin;
import com.ramussoft.idef0.attribute.FunctionOwnerPlugin;
import com.ramussoft.idef0.attribute.FunctionTypePlugin;
import com.ramussoft.idef0.attribute.ProjectPreferencesPlugin;
import com.ramussoft.idef0.attribute.SectorBorderPlugin;
import com.ramussoft.idef0.attribute.SectorPlugin;
import com.ramussoft.idef0.attribute.SectorPointPlugin;
import com.ramussoft.idef0.attribute.SectorPropertiesPlugin;
import com.ramussoft.idef0.attribute.StatusPlugin;
import com.ramussoft.idef0.attribute.VisualDataPlugin;

public class IDEF0PluginProvider extends AttributePluginProvider {

    @Override
    public AttributePlugin[] getAttributePlugins() {
        return new AttributePlugin[]{};
    }

    @Override
    public AttributePlugin[] getSystemAttributePlugins() {
        return new AttributePlugin[]{new FRectanglePlugin(),
                new StatusPlugin(), new FontPlugin(),
                new FunctionOwnerPlugin(), new FunctionTypePlugin(),
                new VisualDataPlugin(), new ColorPlugin(),
                new AnyToAnyPlugin(), new SectorPlugin(),
                new SectorBorderPlugin(), new ProjectPreferencesPlugin(),
                new DecompositionTypePlugin(), new SectorPointPlugin(),
                new SectorPropertiesPlugin(), new DFDSNamePlugin()};
    }

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> res = super.getPlugins();
        res.add(new IDEF0Plugin());
        return res;
    }

}
