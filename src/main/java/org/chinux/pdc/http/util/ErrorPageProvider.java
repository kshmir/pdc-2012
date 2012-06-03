package org.chinux.pdc.http.util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ErrorPageProvider {

	private static byte[] page403 = null;

	public synchronized static byte[] get403() {
		if (page403 == null) {
			final InputStream in = ErrorPageProvider.class
					.getResourceAsStream("/403.html");
			final ByteArrayOutputStream inStream = new ByteArrayOutputStream();
			try {
				int i = 0;
				while ((i = in.read()) != -1) {
					inStream.write(i);
				}
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			page403 = inStream.toByteArray();
		}
		return page403;
	}
}
