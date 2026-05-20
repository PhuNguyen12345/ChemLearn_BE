package com.example.chemlearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChemLearnApplication {

	public static void main(String[] args) {
		// Clean up Flyway version 16 mismatch from local development before startup
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost:5432/chem_learn";
			String user = "postgres";
			try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, user, "1231");
				 java.sql.Statement stmt = conn.createStatement()) {
				stmt.executeUpdate("DELETE FROM flyway_schema_history WHERE version = '16'");
				System.out.println("Flyway migration version 16 history cleared successfully.");
			} catch (Exception e) {
				try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, user, "123");
					 java.sql.Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("DELETE FROM flyway_schema_history WHERE version = '16'");
					System.out.println("Flyway migration version 16 history cleared successfully with fallback password.");
				} catch (Exception ex) {
					System.err.println("Could not clear Flyway history via JDBC: " + ex.getMessage());
				}
			}
		} catch (Exception e) {
			System.err.println("Pre-startup database cleanup failed: " + e.getMessage());
		}

		SpringApplication.run(ChemLearnApplication.class, args);
	}

}
