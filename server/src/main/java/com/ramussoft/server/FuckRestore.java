package com.ramussoft.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FuckRestore {

    /**
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection pub = DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1/ramus_public", "postgres",
                "postgres");

        Connection dev = DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1/ramus_public_dev", "postgres",
                "postgres");

        Statement st = dev.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM ramus_cattle_tasks");
        PreparedStatement ps = pub.prepareStatement("UPDATE ramus_cattle_tasks SET name=? WHERE task_id=?");
        int i = 0;
        while (rs.next()) {
            ps.setString(1, rs.getString("name"));
            ps.setLong(2, rs.getLong("task_id"));
            ps.execute();
            i++;
            if (i % 1000 == 0)
                System.out.println(i);
        }
        rs.close();
        st.close();
        ps.close();
        pub.commit();

    }

}
