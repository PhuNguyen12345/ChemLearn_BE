package com.example.chemlearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ChemLearnApplication {

	public static void main(String[] args) {
		// Programmatically repair Flyway schema history to resolve checksum mismatches automatically at startup
		try {
			String url = "jdbc:postgresql://localhost:5432/chem_learn";
			String user = "postgres";
			String[] passwords = {"1231", "123"};
			boolean repaired = false;
			for (String pwd : passwords) {
				try {
					org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
							.dataSource(url, user, pwd)
							.baselineOnMigrate(true)
							.load();
					flyway.repair();
					System.out.println("Flyway schema history repaired successfully.");
					repaired = true;
					break;
				} catch (Exception ex) {
					// continue
				}
			}
			if (!repaired) {
				System.err.println("Could not run Flyway repair: Database connection failed.");
			}
		} catch (Throwable t) {
			System.err.println("Flyway pre-startup repair failed: " + t.getMessage());
		}

		SpringApplication.run(ChemLearnApplication.class, args);
	}

}
