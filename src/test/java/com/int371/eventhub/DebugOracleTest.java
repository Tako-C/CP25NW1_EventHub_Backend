package com.int371.eventhub;
import org.junit.jupiter.api.Test;
import java.sql.*;

public class DebugOracleTest {
    @Test
    public void testConstraints() throws Exception {
        System.out.println("Starting DB Check...");
        try (Connection c = DriverManager.getConnection("jdbc:oracle:thin:@//cp25nw1.sit.kmutt.ac.th:1521/XEPDB1", "C##ADMIN", "admin123")) {
            try (Statement s = c.createStatement()) {
                ResultSet rs = s.executeQuery("SELECT constraint_name, search_condition_vc FROM user_constraints WHERE table_name = 'SUGGESTIONS_ANALYSIS'");
                while(rs.next()) {
                    System.out.println("DEBUG_CONSTRAINT: " + rs.getString(1) + " -> " + rs.getString(2));
                }
            }
        }
    }
}
