package com.dsoft.pb.idef.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.AnyToAnyPersistent;
import com.ramussoft.idef0.attribute.SectorBorderPersistent;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.RowSet;
import com.ramussoft.report.data.Rows;

public class FastIdef0Connection extends IDEF0Connection {

    private int type;

    private boolean all;

    @SuppressWarnings("unused")
    private long branchId;

    private Attribute end;

    private Attribute start;

    public FastIdef0Connection(int type, boolean all) {
        this.type = type;
        this.all = all;
    }

    @Override
    public Rows getConnected(Data data, Row row) {
        branchId = data.getBranchId();
        String modelName = data.getQuery().getAttribute("ReportFunction");
        if (modelName == null)
            throw new DataException("Error.noModelProperty",
                    "Set model attribute for report first!!!");
        List<Qualifier> models = getQualifiers(data, modelName);
        Rows res = null;
        for (Qualifier model : models) {
            Key key = new Key(all, model);
            IDEF0Buffer[] buffer = getIDEF0Buffers(key, data);
            Rows rows;
            if (row.getElement().getQualifierId() == model.getId()) {
                rows = buffer[type].getStreams(row);
            } else {
                rows = buffer[type].getFunctions(row);
            }
            if (res == null)
                res = rows;
            else
                res.addAll(rows);
        }
        if (res == null)
            throw new DataException("Error.noModelProperty",
                    "Set model attribute for report first!!!");
        return res;
    }

    public List<Qualifier> getQualifiers(Data data, String modelName) {
        Engine engine = data.getEngine();
        List<Qualifier> qualifiers = new ArrayList<Qualifier>();
        if ("[ALL MODELS]".equals(modelName)) {
            for (Qualifier qualifier : engine.getQualifiers())
                if (IDEF0Plugin.isFunction(qualifier))
                    qualifiers.add(qualifier);
        } else {
            com.ramussoft.database.common.RowSet rowSet = new com.ramussoft.database.common.RowSet(
                    engine, IDEF0Plugin.getModelTree(engine),
                    new Attribute[]{StandardAttributesPlugin
                            .getAttributeNameAttribute(engine)}, null, true);

            StringTokenizer st = new StringTokenizer(modelName,
                    Data.QUALIFIER_DELIMETER);
            while (st.hasMoreElements()) {
                com.ramussoft.database.common.Row row = rowSet.findRow(st
                        .nextToken());
                if (row != null)
                    addQualifiers(data.getEngine(), qualifiers, row);

            }
            Qualifier qualifier = engine.getQualifierByName(modelName);
            if (qualifier != null && !qualifiers.contains(qualifier))
                qualifiers.add(qualifier);
        }
        return qualifiers;
    }

    private void addQualifiers(Engine engine, List<Qualifier> qualifiers,
                               com.ramussoft.database.common.Row row) {
        Long qualifierId = (Long) row.getAttribute(StandardAttributesPlugin
                .getAttributeQualifierId(engine));
        if (qualifierId != null) {
            Qualifier qualifier = engine.getQualifier(qualifierId);
            if (qualifier != null && !qualifiers.contains(qualifier))
                qualifiers.add(qualifier);
        }
        for (com.ramussoft.database.common.Row r : row.getChildren())
            addQualifiers(engine, qualifiers, r);
    }

    private IDEF0Buffer[] getIDEF0Buffers(Key key, Data data) {
        IDEF0Buffer[] buffers = (IDEF0Buffer[]) data.get(key);
        if (buffers == null) {
            buffers = createBuffers(key, data);
            data.put(key, buffers);
        }
        return buffers;
    }

    private IDEF0Buffer[] createBuffers(final Key key, final Data data) {
        final IDEF0Buffer[] buffers = new IDEF0Buffer[4];
        for (int i = 0; i < 4; i++)
            buffers[i] = new IDEF0Buffer(data, key.model);

        RowSet model = data.getRowSet(key.model);

        RowSet base = data.getRowSet(IDEF0Plugin.getBaseFunctions(data
                .getEngine()));

        end = IDEF0Plugin.getSectorBorderEndAttribute(data.getEngine());
        start = IDEF0Plugin.getSectorBorderStartAttribute(data.getEngine());

        Attribute fun = IDEF0Plugin
                .getSectorFunctionAttribute(data.getEngine());

        Attribute st = IDEF0Plugin.getSectorStreamAttribute(data.getEngine());

        Attribute dType = IDEF0Plugin.getDecompositionTypeAttribute(data
                .getEngine());

        RowSet sectors = data.getRowSet(IDEF0Plugin.getBaseSectorQualifier(data
                .getEngine()));

        RowSet streams = data.getRowSet(IDEF0Plugin.getBaseStreamQualifier(data
                .getEngine()));

        Attribute anyToAnyAttribute = IDEF0Plugin.getStreamAddedAttribute(data
                .getEngine());
        for (com.ramussoft.database.common.Row sector : sectors.getAllRows()) {
            Long f = (Long) sector.getAttribute(fun);
            Long s = (Long) sector.getAttribute(st);
            if (f != null && s != null) {
                com.ramussoft.database.common.Row parentFunction = model
                        .findRow(f);
                com.ramussoft.database.common.Row pFunction = null;

                if (parentFunction == null) {
                    Qualifier q1 = base.getEngine().getQualifier(f);
                    if (q1 != null)
                        System.out.println(q1);
                    pFunction = base.findRow(f);
                    if (pFunction != null
                            && pFunction.getAttribute(
                            IDEF0Plugin.getBaseFunctionQualifierId(data
                                    .getEngine())).equals(
                            model.getQualifier().getId()))
                        parentFunction = pFunction;
                }

                Row stream = (Row) streams.findRow(s);
                if (parentFunction != null && stream != null) {

                    Integer decompositionType = (Integer) parentFunction
                            .getAttribute(dType);

                    if (pFunction != null)
                        parentFunction = null;
                    else if (parentFunction.getChildCount() == 0)
                        continue;

                    List<AnyToAnyPersistent> list = (List) stream
                            .getAttribute(anyToAnyAttribute);

                    if (list == null)
                        list = Collections.emptyList();
                    SectorBorderPersistent sb = (SectorBorderPersistent) sector
                            .getAttribute(start);
                    int type;
                    if (sb != null && (type = sb.getFunctionType()) >= 0) {
                        if (decompositionType != null && decompositionType == 2) {// DFDS
                            type = 0;
                        }
                        IDEF0Buffer buffer = buffers[type];

                        Row function = buffer.addFunctionStream(
                                sb.getFunction(), stream.getElementId(),
                                parentFunction);
                        for (AnyToAnyPersistent p : list)
                            if (p != null) {
                                buffer.addRowFunction(p.getOtherElement(),
                                        sb.getFunction(), p.getElementStatus(),
                                        parentFunction);
                                if (sb.getTunnelSoft() == 1 && function != null
                                        && function.getChildCount() > 0) {
                                    addAll(function, buffer);
                                }
                            }

                    }
                    sb = (SectorBorderPersistent) sector.getAttribute(end);
                    if (sb != null && (type = sb.getFunctionType()) >= 0) {
                        if (decompositionType != null && decompositionType == 2) {// DFDS
                            type = 2;
                        }
                        IDEF0Buffer buffer = buffers[type];

                        Row function = buffer.addFunctionStream(
                                sb.getFunction(), stream.getElementId(),
                                parentFunction);
                        for (AnyToAnyPersistent p : list)
                            if (p != null) {
                                buffer.addRowFunction(p.getOtherElement(),
                                        sb.getFunction(), p.getElementStatus(),
                                        parentFunction);
                                if (sb.getTunnelSoft() == 1 && function != null
                                        && function.getChildCount() > 0) {
                                    addAll(function, buffer);
                                }
                            }

                    }
                }
            }
        }

        for (IDEF0Buffer buffer : buffers)
            buffer.commit(all);

        return buffers;
    }

    private void addAll(Row function, IDEF0Buffer buffer) {
        for (Object child : function.getChildren()) {
            Row r = (Row) child;
            buffer.addFunctionStream(r);
            buffer.addRowFunction(r);
            addAll(r, buffer);
        }
    }

    public class Key {
        private boolean all;

        private Qualifier model;

        public Key(boolean all, Qualifier model) {
            this.all = all;
            this.model = model;
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
            result = prime * result + (all ? 1231 : 1237);
            result = prime * result + ((model == null) ? 0 : model.hashCode());
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
            if (!(obj instanceof Key))
                return false;
            Key other = (Key) obj;
            if (all != other.all)
                return false;
            if (model == null) {
                if (other.model != null)
                    return false;
            } else if (!model.equals(other.model))
                return false;
            return true;
        }

    }

}
