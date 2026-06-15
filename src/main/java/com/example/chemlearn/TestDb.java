package com.example.chemlearn;

import java.sql.*;
import java.util.UUID;

public class TestDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/chem_learn";
        String user = "postgres";
        String password = "1231";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to PostgreSQL database!");
            
            // Check account_link_requests
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM account_link_requests")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("Request ID: " + rs.getObject("id") + 
                        ", Initiator: " + rs.getObject("initiator_id") + 
                        ", Target: " + rs.getString("target_email") + 
                        ", Status: " + rs.getString("status"));
                }
                System.out.println("Total requests: " + count);
            }

            // Check users
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id, username, email FROM users WHERE username IN ('student', 'thor')")) {
                while (rs.next()) {
                    System.out.println("User: " + rs.getString("username") + 
                        ", Email: " + rs.getString("email") + 
                        ", ID: " + rs.getObject("user_id"));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
