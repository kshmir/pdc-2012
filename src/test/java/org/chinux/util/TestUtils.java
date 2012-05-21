package org.chinux.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;

@Ignore
public class TestUtils {
	private static Map<String, String> fileCache = new HashMap<String, String>();

	private static String testPath = "src/test/resources/";

	public static String stringFromFile(final String path) {

		final File file = new File(testPath + path).getAbsoluteFile();
		if (!file.exists()) {
			throw new RuntimeException("Wrong path00! "
					+ file.getAbsolutePath());
		}

		if (fileCache.containsKey(file.getAbsolutePath())) {
			return fileCache.get(file.getAbsolutePath());
		}

		final StringBuilder builder = new StringBuilder();
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(
					file));

			String line;
			while ((line = reader.readLine()) != null) {

				builder.append(line).append("\r\n");
			}

		} catch (final FileNotFoundException e) {
			throw new RuntimeException("Wrong path!");
		} catch (final IOException e) {
			throw new RuntimeException("IO ERROR!");
		}

		fileCache.put(file.getAbsolutePath(), builder.toString());

		return builder.toString();
	}
}
