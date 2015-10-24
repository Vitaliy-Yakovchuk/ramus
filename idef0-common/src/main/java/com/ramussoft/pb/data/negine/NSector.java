package com.ramussoft.pb.data.negine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.idef0.attribute.SectorBorderPersistent;
import com.ramussoft.idef0.attribute.SectorPersistent;
import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.idef0.attribute.SectorPropertiesPersistent;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.AbstractCrosspoint;
import com.ramussoft.pb.data.AbstractSector;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.idef.elements.ReplaceStreamType;
import com.ramussoft.pb.types.GlobalId;

public class NSector extends AbstractSector {

    protected NSectorBorder start;

    protected NSectorBorder end;

    NStream stream = null;

    private SectorBorderPersistent startP;

    private SectorBorderPersistent endP;

    private Function function;

    protected NSectorBorder createSectorBorder(final boolean start) {
        return new NSectorBorder(dataPlugin, start, this);
    }

    public NSector(final NDataPlugin dataPlugin, Element element) {
        super(dataPlugin, element);
        start = createSectorBorder(true);
        end = createSectorBorder(false);
    }

    public NSectorBorder getStart() {
        return start;
    }

    public NSectorBorder getEnd() {
        return end;
    }

    public Stream getStream() {
        return getAStream();
    }

    public void storeData() {
        // ((NSectorBorder)getStart()).setSBP();
        // ((NSectorBorder)getEnd()).setSBP();
    }

    @Override
    public void remove() {
        /*
         * { Crosspoint sc = getStart().getCrosspoint(); if (sc != null &&
		 * sc.isDLevel()) { Function function = getStart().getFunction(); if
		 * (function != null && function.getType() == Function.TYPE_DFDS_ROLE)
		 * ((NFunction) function).remove(); } }
		 * 
		 * { Crosspoint sc = getEnd().getCrosspoint(); if (sc != null &&
		 * sc.isDLevel()) { Function function = getEnd().getFunction(); if
		 * (function != null && function.getType() == Function.TYPE_DFDS_ROLE)
		 * ((NFunction) function).remove(); } }
		 */
        if (dataPlugin.getEngine().getElement(getElementId()) != null)
            super.remove();
    }

    public void clearMe() {
        synchronized (dataPlugin) {
            rMe(getStart());
            rMe(getEnd());
            clearFunction();
        }
    }

    public void clearFunction() {
        Function function;
        if ((function = getFunction()) != null)
            function.getSectors().remove(this);
    }

    private void rMe(final SectorBorder sb) {
        Crosspoint c = sb.getCrosspoint();
        if (c == null) {
            c = ((NSectorBorder) sb).crosspoint;
            ((NSectorBorder) sb).crosspoint = null;
        }
        if (c == null)
            return;
        ((AbstractCrosspoint) c).remove1(this);
    }

    public GlobalId getGlobalId() {
        return GlobalId.create(getElementId());
    }

    public Function getFunction() {
        if (function == null) {
            function = getAFunction();
        }
        return function;
    }

    @Override
    public void setThisStream(final Stream stream) {
        if (this.getAStream() != null && !this.getAStream().equals(stream)) {
            if (this.getAStream().isEmptyName()) {
                dataPlugin.removeRow(this.getAStream());
            }
        }
        this.setAStream(stream);
    }

    @Override
    public void setStream(final Stream stream, ReplaceStreamType type) {
        synchronized (dataPlugin) {
            if (dataPlugin.isLoading()) {
                setThisStream(stream);
                return;
            } else
                super.setStream(stream, type);
        }
    }

    public void setFunction(final Function function) {
        synchronized (dataPlugin) {
            if (this.getAFunction() != null)
                this.getAFunction().getSectors().remove(this);
            function.getSectors().add(this);
            this.setAFunction(function);
            this.function = function;
        }
    }

    public Row[] getRows() {
        if (getStream() != null)
            return getStream().getAdded();
        return new Row[0];
    }

    public int getCreateState() {
        return getSp().getCreateState();
    }

    public void setCreateState(final int createState, final double pos) {
        synchronized (dataPlugin) {
            getSp().setCreateState(createState);
            getSp().setCreatePos(pos);
            setAttribute(dataPlugin.sectorAttribute, getSp());
        }
    }

    public void setAttribute(Attribute attribute, Object object) {
        getEngine().setAttribute(element, attribute, object);
    }

    public double getCreatePos() {
        return getSp().getCreatePos();
    }

    public byte[] getVisualAttributes() {
        return getSp().getVisualAttributes();
    }

    public void setVisualAttributes(final byte[] visualData) {
        synchronized (dataPlugin) {
            if (Arrays.equals(getSp().getVisualAttributes(), visualData))
                return;
            getSp().setVisualAttributes(visualData);
            setAttribute(dataPlugin.sectorAttribute, getSp());
        }
    }

    @Override
    public String toString() {
        if (getAStream() == null)
            return "No stream";
        return getAStream().toString();
    }

    public String getAlternativeText() {
        String alternativeText = getSp().getAlternativeText();
        if (alternativeText == null)
            return "";
        return alternativeText;
    }

    public boolean isShowText() {
        return getSp().getShowText() != 0;
    }

    public void setAlternativeText(final String alternativeText) {
        getSp().setAlternativeText(alternativeText);
        setAttribute(dataPlugin.sectorAttribute, getSp());
    }

    public void setShowText(final boolean showText) {
        getSp().setShowText((showText) ? 1 : 0);
        setAttribute(dataPlugin.sectorAttribute, getSp());
    }

    @Override
    public void setTextAligment(final int textAligment) {
        getSp().setTextAligment(textAligment);
        setAttribute(dataPlugin.sectorAttribute, getSp());
    }

    @Override
    public int getTextAligment() {
        return getSp().getTextAligment();
    }

    /**
     * @return the sp
     */
    protected SectorPersistent getSp() {
        SectorPersistent res = (SectorPersistent) getAttribute(dataPlugin.sectorAttribute);
        if (res == null)
            return new SectorPersistent();
        return res;
    }

    public Object getAttribute(Attribute attribute) {
        return getEngine().getAttribute(element, attribute);
    }

    /**
     * @param stream the stream to set
     */
    protected void setAStream(Stream stream) {
        if (stream == null) {
            setAttribute(dataPlugin.sectorStream, null);
            this.stream = null;
        } else {
            setAttribute(dataPlugin.sectorStream,
                    ((com.ramussoft.database.common.Row) stream).getElementId());
            this.stream = (NStream) stream;
        }
    }

    public void freeStreamValue() {
        this.stream = null;
    }

    /**
     * @return the stream
     */
    protected Stream getAStream() {
        Long object = (Long) getAttribute(dataPlugin.sectorStream);
        if (object == null)
            return null;
        if (stream != null) {
            if (stream.getElementId() == object.longValue())
                return stream;
            stream = null;
            return getAStream();
        }
        stream = (NStream) dataPlugin.findRowByGlobalId(object);
        return stream;
    }

    /**
     * @param function the function to set
     */
    protected void setAFunction(Function function) {
        setAttribute(dataPlugin.sectorFunction,
                ((com.ramussoft.database.common.Row) function).getElementId());
    }

    /**
     * @return the function
     */
    Function getAFunction() {
        Long object = (Long) getAttribute(dataPlugin.sectorFunction);
        if (object == null)
            return null;
        NFunction function = (NFunction) dataPlugin.findRowByGlobalId(object);
        return function;
    }

    public void updateFunction() {
        Function f = getFunction();
        if ((f != null) && (f.getSectors().indexOf(this) < 0))
            f.getSectors().add(this);
    }

    @Override
    public List<SectorPointPersistent> getSectorPointPersistents() {
        List spps = (List) getAttribute(dataPlugin.sectorPointsAttribute);
        if (spps == null)
            return new ArrayList<SectorPointPersistent>();
        return spps;
    }

    public void setSectorPointPersistents(
            java.util.List<SectorPointPersistent> points) {
        setAttribute(dataPlugin.sectorPointsAttribute, points);
    }

    @Override
    public void setSectorProperties(SectorPropertiesPersistent spp) {
        setAttribute(dataPlugin.sectorPropertiesAttribute, spp);
    }

    @Override
    public SectorPropertiesPersistent getSectorProperties() {
        SectorPropertiesPersistent attribute = (SectorPropertiesPersistent) getAttribute(dataPlugin.sectorPropertiesAttribute);
        if (attribute == null)
            return new SectorPropertiesPersistent();
        return attribute;
    }

    public SectorBorderPersistent getStartP() {
        if (startP == null) {
            startP = (SectorBorderPersistent) getAttribute(dataPlugin.sectorBorderStart);
            if (startP == null)
                startP = new SectorBorderPersistent();
        }
        return startP;
    }

    public SectorBorderPersistent getEndP() {
        if (endP == null) {
            endP = (SectorBorderPersistent) getAttribute(dataPlugin.sectorBorderEnd);
            if (endP == null)
                endP = new SectorBorderPersistent();
        }
        return endP;
    }

    public void setStartP(SectorBorderPersistent startP) {
        this.startP = startP;
    }

    public void setEndP(SectorBorderPersistent endP) {
        this.endP = endP;
    }

}
