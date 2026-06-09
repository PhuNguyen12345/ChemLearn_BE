package com.example.chemlearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ChemLearnApplication {

	public static void main(String[] args) {
		loadDotenv();

		// Programmatically repair Flyway schema history to resolve checksum mismatches automatically at startup
		try {
			String url = "jdbc:postgresql://localhost:5432/chemlearn_db";
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

	private static void loadDotenv() {
		Path dotenvPath = Path.of(".env");
		if (!Files.exists(dotenvPath)) {
			return;
		}

		try {
			List<String> lines = Files.readAllLines(dotenvPath);
			for (String rawLine : lines) {
				String line = rawLine.trim();
				if (line.isBlank() || line.startsWith("#")) {
					continue;
				}
				if (line.startsWith("export ")) {
					line = line.substring("export ".length()).trim();
				}

				int equalsIndex = line.indexOf('=');
				if (equalsIndex <= 0) {
					continue;
				}

				String key = line.substring(0, equalsIndex).trim();
				String value = stripInlineComment(line.substring(equalsIndex + 1).trim());
				value = stripWrappingQuotes(value);

				if (key.isBlank() || System.getenv(key) != null || System.getProperty(key) != null) {
					continue;
				}
				System.setProperty(key, value);
			}
		} catch (IOException ex) {
			System.err.println("Could not load .env file: " + ex.getMessage());
		}
	}

	private static String stripInlineComment(String value) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (current == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (current == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			} else if (current == '#' && !inSingleQuote && !inDoubleQuote) {
				return value.substring(0, i).trim();
			}
		}
		return value;
	}

	private static String stripWrappingQuotes(String value) {
		if (value.length() >= 2) {
			char first = value.charAt(0);
			char last = value.charAt(value.length() - 1);
			if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
				return value.substring(1, value.length() - 1);
			}
		}
		return value;
	}
}
