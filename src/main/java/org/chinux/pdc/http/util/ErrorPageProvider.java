package org.chinux.pdc.http.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ErrorPageProvider {

	static byte[] page403 = null;

	public static byte[] get403() {
		if (page403 == null) {
			final File file = new File("src/main/resources/META-INF/403.html");
			page403 = new byte[(int) file.length()];
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				fis.read(page403);
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}
		return page403;
	}

}
