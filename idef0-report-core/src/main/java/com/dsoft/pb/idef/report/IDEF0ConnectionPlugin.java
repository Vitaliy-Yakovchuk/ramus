package com.dsoft.pb.idef.report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.DFDSName;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.RowSet;
import com.ramussoft.report.data.Rows;
import com.ramussoft.report.data.plugin.AbstractConnectionPlugin;
import com.ramussoft.report.data.plugin.Connection;

public class IDEF0ConnectionPlugin extends AbstractConnectionPlugin {

    public static Keywords keywords = new Keywords();

    @Override
    public Connection getConnection(Data data, Qualifier qualifier, String name) {

        if (keywords.hasName("Mechanisms", name))
            return new FastIdef0Connection(1, false);
        if (keywords.hasName("Inputs", name))
            return new FastIdef0Connection(2, false);
        if (keywords.hasName("Outputs", name))
            return new FastIdef0Connection(0, false);
        if (keywords.hasName("Controls", name))
            return new FastIdef0Connection(3, false);
        if (keywords.hasName("AllMechanisms", name))
            return new FastIdef0Connection(1, true);
        if (keywords.hasName("AllInputs", name))
            return new FastIdef0Connection(2, true);
        if (keywords.hasName("AllOutputs", name))
            return new FastIdef0Connection(0, true);
        if (keywords.hasName("AllControls", name))
            return new FastIdef0Connection(3, true);
        if (keywords.hasName("Qualifiers", name))
            return new QualifiersConnection();

        if (qualifier != null) {
            if (qualifier.equals(IDEF0Plugin.getBaseStreamQualifier(data
                    .getEngine()))) {
                Qualifier qualifier2 = data.getQualifier(name);
                return new StreamConnection(qualifier2);
            }
        }
        if (keywords.hasName("Owners", name))
            return new OwnersConnection();
        if (keywords.hasName("Roles", name))
            return new RolesConnection();
        if (keywords.hasName("InputsControls", name))
            return new InputsControlsConnection(false);
        if (keywords.hasName("InputsMechanisms", name))
            return new InputsMechanismsConnection(false);
        if (keywords.hasName("InputsControlsMechanisms", name))
            return new InputsControlsMechanismsConnection(false);
        if (keywords.hasName("AllInputsControls", name))
            return new InputsControlsConnection(true);
        if (keywords.hasName("AllInputsMechanisms", name))
            return new InputsMechanismsConnection(true);
        if (keywords.hasName("AllInputsControlsMechanisms", name))
            return new InputsControlsMechanismsConnection(true);
        return null;
    }

    @Override
    public Rows getVirtualQualifier(Data data, String name) {
        if (keywords.hasName("Streams", name))
            return getStreams(data, name);
        if (keywords.hasName("Owners", name))
            return getOuners(data, name);
        if (keywords.hasName("Roles", name))
            return getOuners(data, name);
        return null;
    }

    private Rows getOuners(Data data, String name) {
        List<Long> ids = OwnersConnection.loadOunerIDs(data.getEngine());
        Rows rows = new Rows(null, data, false);
        rows.setQualifierName(name);

        for (Long long1 : ids) {
            Qualifier qualifier = data.getQualifier(long1.longValue());
            if (qualifier != null)
                rows.addAll(data.getRows(qualifier));
        }

        return rows;
    }

    private Rows getStreams(Data data, String name) {
        final RowSet rowSet = IDEF0Buffer.getStreamsRowSet(data);
        final Rows rows = new Rows(rowSet, data, false);

        rows.setQualifierName(name);

        data.getTemplate()
                .queryWithoutResults(
                        "SELECT DISTINCT ramus_attribute_any_to_any_elements.element_id,\n"
                                + "(SELECT element_id FROM ramus_elements WHERE ramus_attribute_any_to_any_elements.other_element= ramus_elements.element_id) as oelement_id,\n"
                                + "ramus_elements.element_name\n"
                                + "FROM ramus_attribute_any_to_any_elements, ramus_elements, ramus_attribute_sector_borders, ramus_attribute_other_elements\n"
                                + "WHERE ramus_attribute_any_to_any_elements.element_id= ramus_elements.element_id\n"
                                + "AND ramus_attribute_sector_borders.function_type>=0\n"
                                + "AND ramus_attribute_sector_borders.element_id = ramus_attribute_other_elements.element_id\n"
                                + "AND ramus_attribute_other_elements.other_element = ramus_elements.element_id\n",
                        new RowMapper() {

                            @Override
                            public Object mapRow(ResultSet rs, int rowNum)
                                    throws SQLException {
                                if ((rs.getString(3).length() > 0)
                                        || (rs.getObject(2) != null)) {
                                    rows.add((Row) rowSet.findRow(rs.getLong(1)));
                                }
                                return null;
                            }
                        }, true);

        return rows;
    }

    @Override
    public Object getAttribute(Data data, Row row, String name) {
        if (keywords.hasName("Text", name)) {
            Object object = row.getNameObject();
            if (object == null)
                return "";
            if (object instanceof DFDSName) {
                String longName = ((DFDSName) object).getLongName();
                return (longName == null) ? "" : longName;
            }
        }
        return null;
    }
}
