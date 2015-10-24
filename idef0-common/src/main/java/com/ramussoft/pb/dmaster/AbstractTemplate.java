package com.ramussoft.pb.dmaster;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.IdGenerator;
import com.dsoft.pb.types.IdGeneratorImpl;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.print.PIDEF0painter;
import com.ramussoft.pb.types.GlobalId;

public abstract class AbstractTemplate implements Template {

    protected DataPlugin dataPlugin = null;

    protected PIDEF0painter painter = null;

    protected Rectangle rect = null;

    protected Function base;

    private Function diagram;

    private boolean refresh = false;

    private int decompositionType;

    private final IdGenerator generator = new IdGeneratorImpl();

    {
        generator.addId(0, GlobalId.LOCAL_RESERVED);
    }

    public void paint(final Graphics2D graphics2D, final Rectangle rectangle) {
        if (dataPlugin == null) {
            createChilds(rectangle);
            createPainter(rectangle);
        } else if ((!rect.equals(rectangle)) || refresh) {
            createPainter(rectangle);
        }

        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        /*
         * graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
		 * RenderingHints.VALUE_STROKE_PURE);
		 * graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
		 * RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		 * graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		 * RenderingHints.VALUE_ANTIALIAS_ON);
		 * 
		 * graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
		 * RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		 * 
		 * graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING,
		 * RenderingHints.VALUE_DITHER_ENABLE);
		 */
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        painter.paint(graphics2D, rectangle.getX(), rectangle.getY(), true);
    }

    private synchronized void createChilds(final Rectangle rectangle) {
        //final IdGenerator g = GlobalId.getGenerator();
        //GlobalId.setGenerator(generator);
        dataPlugin = NDataPluginFactory.getTemplateDataPlugin();
        Function f = dataPlugin.getBaseFunction();
        final ProjectOptions po = f.getProjectOptions();
        po.setChangeDate(po.getDateChangeDate());
        po.setProjectAutor(po.getProjectAutor());
        po.setProjectName(po.getProjectName());
        po.setUsedAt(po.getUsedAt());
        f.setProjectOptions(po);
        final Attribute attribute = dataPlugin.getEngine().createAttribute(
                new com.ramussoft.common.AttributeType("Core", "Text", true));
        Qualifier q = dataPlugin.getBaseFunctionQualifier();
        q.getAttributes().add(attribute);
        q.setAttributeForName(attribute.getId());
        dataPlugin.getEngine().updateQualifier(q);
        f = (Function) dataPlugin.createRow(f, true);
        diagram = f;
        f.setDecompositionType(decompositionType);
        final FRectangle rect = f.getBounds();
        rect.setX(rectangle.getWidth() / 2 - rect.getWidth() / 2);
        rect.setY(rectangle.getHeight() / 2 - rect.getHeight() / 2);
        f.setBounds(rect);
        createChilds(f, dataPlugin);
        base = f;
        //GlobalId.setGenerator(g);
    }

    private void createPainter(final Rectangle rectangle) {
        painter = new PIDEF0painter(base, rectangle.getSize(), dataPlugin);
        rect = rectangle;
        refresh = false;
    }

    @Override
    public void close() {
        if (dataPlugin != null) {
            IEngine i = dataPlugin.getEngine().getDeligate();
            ((FileIEngineImpl) i).setClearSessionPath(false);
            try {
                ((FileIEngineImpl) i).close();
                ((FileIEngineImpl) i).getConnection().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataPlugin = null;
        }
    }

    @Override
    public Function getDiagram() {
        return diagram;
    }

    @Override
    public void refresh() {
        this.refresh = true;
    }

    @Override
    public int getDecompositionType() {
        return decompositionType;
    }

    @Override
    public void setDecompositionType(int diagramType) {
        this.decompositionType = diagramType;
    }
}
