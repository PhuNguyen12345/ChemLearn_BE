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
		SpringApplication.run(ChemLearnApplication.class, args);
	}

	private static void loadDotenv() {
		Path dotenvPath = List.of(Path.of(".env"), Path.of("ChemLearn_BE", ".env")).stream()
				.filter(Files::exists)
				.findFirst()
				.orElse(null);
		if (dotenvPath == null) {
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
