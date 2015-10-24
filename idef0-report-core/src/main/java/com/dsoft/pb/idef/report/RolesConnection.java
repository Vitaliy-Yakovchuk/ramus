package com.dsoft.pb.idef.report;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.AnyToAnyPersistent;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.RowSet;
import com.ramussoft.report.data.Rows;
import com.ramussoft.report.data.plugin.AbstractConnection;

/**
 * Зв'язок ролі визначається з виконавців (для DFDS це всі виконавці, для IDEF0
 * приєднані елементи механізмів, що є відносяться до класифікаторів ролей).
 *
 * @author vitaliy.yakovchuk
 */
public class RolesConnection extends AbstractConnection {

    private Hashtable<Row, List<Row>> roles;

    public RolesConnection() {

    }

    @Override
    public Rows getConnected(final Data data, Row row) {

        Integer type = (Integer) row.getAttribute(IDEF0Plugin
                .getFunctionTypeAttribute(row.getEngine()));

        if (type != null && type.intValue() == 1003) {
            Long id = (Long) row.getAttribute(IDEF0Plugin.getLinkAttribute(row
                    .getEngine()));
            Rows rows = new Rows(row.getRowSet(), data, false);
            if (id != null) {
                Row row2 = data.findRow(id);
                if (row2 != null) {
                    List<AnyToAnyPersistent> list = (List) row2
                            .getAttribute(IDEF0Plugin
                                    .getStreamAddedAttribute(row2.getEngine()));
                    for (AnyToAnyPersistent anyPersistent : list) {
                        Row row3 = data
                                .findRow(anyPersistent.getOtherElement());
                        row3.setElementStatus(anyPersistent.getElementStatus());
                        rows.add(row3);
                    }
                }
            }
            return rows;
        }

        RolesConnection connection = (RolesConnection) data
                .get("RolesConnection");
        if (connection != null)
            return connection.getRoles(data, row);

        final Engine engine = data.getEngine();

        ArrayList<Long> ounerQualifierIds = loadOunerIDs(engine);

        roles = new Hashtable<Row, List<Row>>();

        final Hashtable<Row, List<Row>> mech = new Hashtable<Row, List<Row>>();

        RowMapper mapper = new RowMapper() {

            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                RowSet rowSet = data.getRowSet(rs.getLong(1));
                Row row = (Row) rowSet.findRow(rs.getLong(2));
                RowSet ounerRowSet = data.getRowSet(rs.getLong(3));
                Row role = (Row) ounerRowSet.findRow(rs.getLong(4));
                if (row == null || role == null)
                    return null;
                List<Row> rows = mech.get(row);
                if (rows == null) {
                    rows = new ArrayList<Row>(5);
                    mech.put(row, rows);
                }
                if (!rows.contains(role))
                    rows.add(role);
                return null;
            }
        };
        if (ounerQualifierIds.size() > 0) {

            data.getTemplate()
                    .queryWithoutResults(
                            "SELECT b.qualifier_id, function, ramus_elements.qualifier_id, ramus_elements.element_id FROM\n"
                                    + "ramus_attribute_sector_borders, ramus_attribute_other_elements, ramus_attribute_any_to_any_elements, ramus_elements, ramus_elements b\n"
                                    + "WHERE ramus_attribute_any_to_any_elements.element_id=ramus_attribute_other_elements.other_element\n"
                                    + "AND ramus_elements.element_id=ramus_attribute_any_to_any_elements.other_element\n"
                                    + "AND ramus_attribute_sector_borders.element_id = ramus_attribute_other_elements.element_id\n"
                                    + "AND function_type=1 AND ramus_attribute_sector_borders.attribute_id in\n"
                                    + "(SELECT attribute_id FROM ramus_attributes WHERE attribute_system=true AND attribute_name='F_SECTOR_BORDER_END')\n"
                                    + "AND b.element_id=function\n"
                                    + "AND ramus_elements.qualifier_id in "
                                    + toIns(ounerQualifierIds), mapper, true);
        }
        data.getTemplate()
                .queryWithoutResults(
                        "SELECT (SELECT qualifier_id FROM ramus_elements WHERE element_id=ouner_id) as function_qualifier_id, "
                                + "ouner_id AS function,  "
                                + "(SELECT qualifier_id FROM ramus_elements WHERE element_id=other_element) as ouner_qualifier_id, "
                                + "other_element as ouner_id FROM ramus_attribute_any_to_any_elements, ramus_attribute_function_ouners "
                                + "WHERE ramus_attribute_any_to_any_elements.element_id IN "
                                + "(SELECT value FROM ramus_attribute_longs WHERE ramus_attribute_longs.element_id=ramus_attribute_function_ouners.element_id)",
                        mapper, true);

        data.getTemplate()
                .queryWithoutResults(
                        "SELECT ramus_attribute_function_ouners.element_id,\n"
                                + "(SELECT MAX(ramus_elements.qualifier_id) FROM ramus_elements WHERE ramus_elements.element_id=ramus_attribute_function_ouners.element_id),\n"
                                + "ramus_elements.qualifier_id,\n"
                                + " ouner_id FROM ramus_elements, ramus_attribute_function_ouners WHERE ramus_elements.element_id=ouner_id "
                                + "AND ramus_attribute_function_ouners.ouner_id IN (SELECT element_id FROM ramus_attribute_function_types WHERE type<1001)",
                        new RowMapper() {

                            @Override
                            public Object mapRow(ResultSet rs, int rowNum)
                                    throws SQLException {
                                RowSet rowSet = data.getRowSet(rs.getLong(2));
                                Row row = (Row) rowSet.findRow(rs.getLong(1));
                                RowSet ounerRowSet = data.getRowSet(rs
                                        .getLong(3));
                                Row ouner = (Row) ounerRowSet.findRow(rs
                                        .getLong(4));
                                if (ouner != null && row != null) {
                                    List<Row> rows = roles.get(row);
                                    if (rows == null) {
                                        rows = new ArrayList<Row>();
                                        roles.put(row, rows);
                                    }
                                    if (!rows.contains(ouner))
                                        rows.add(ouner);
                                }

                                return null;
                            }
                        }, true);

        RowSet rowSet = data.getRowSet(row.getQualifier());
        setRecOuners(rowSet.getRoot(), mech, null);

        data.put("RolesConnection", this);
        return getRoles(data, row);
    }

    private void setRecOuners(com.ramussoft.database.common.Row root,
                              Hashtable<Row, List<Row>> mech, List<Row> ouner) {
        List<Row> o;
        for (Object obj : root.getChildren()) {
            Row row = (Row) obj;
            o = roles.get(row);
            if (o == null) {
                if (ouner != null)
                    roles.put(row, ouner);
                else {
                    List<Row> value = mech.get(row);
                    if (value != null)
                        roles.put(row, value);
                }
            }
            setRecOuners(row, mech, (o == null) ? ouner : o);
        }
    }

    public static ArrayList<Long> loadOunerIDs(Engine engine) {
        ArrayList<Long> ounerQualifierIds = new ArrayList<Long>();
        Properties properties = new Properties();
        byte[] bs = engine.getStream("/properties/idef0.xml");
        if ((bs != null) && (bs.length > 0)) {
            try {
                properties.loadFromXML(new ByteArrayInputStream(bs));
            } catch (InvalidPropertiesFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String value = properties.getProperty("OUNERS_IDS");
        if (value != null) {
            StringTokenizer st = new StringTokenizer(value);
            while (st.hasMoreElements()) {
                ounerQualifierIds.add(new Long(st.nextToken()));
            }
        }
        return ounerQualifierIds;
    }

    private String toIns(ArrayList<Long> ounerQualifierIds) {
        StringBuffer sb = new StringBuffer("(");
        boolean first = true;
        for (Long long1 : ounerQualifierIds) {
            if (first)
                first = false;
            else
                sb.append(", ");
            sb.append(long1);
        }
        sb.append(")");
        return sb.toString();
    }

    private Rows getRoles(Data data, Row row) {
        List<Row> rRows = roles.get(row);
        if (rRows != null && rRows.size() > 0) {
            Rows rows = new Rows(rRows.get(0).getRowSet(), data, false, 1);
            rows.addAll(rRows);
            return rows;
        }
        return new Rows(null, data, false, 0);
    }

    public Qualifier getOpposite(Data data, Qualifier qualifier) {
        if (IDEF0Plugin.isFunction(qualifier))
            return null;
        String modelName = data.getQuery().getAttribute("ReportFunction");
        if (modelName == null)
            throw new DataException("Error.noModelProperty",
                    "Set model attribute for report first!!!");
        return data.getQualifier(modelName);
    }

}
