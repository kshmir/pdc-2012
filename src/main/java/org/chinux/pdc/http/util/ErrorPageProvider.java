package org.chinux.pdc.http.util;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ErrorPageProvider {

	private static byte[] page403 = null;
	private static byte[] page501 = null;
	private static byte[] page502 = null;

	public synchronized static byte[] get403() {
		return page403 = extractPage(page403, "/403.html");
	}

	private static byte[] extractPage(byte[] buff, final String fileName) {
		if (buff == null) {
			final InputStream in = ErrorPageProvider.class
					.getResourceAsStream(fileName);
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

			buff = inStream.toByteArray();
		}
		return buff;
	}

	public synchronized static byte[] get501() {
		return page501 = extractPage(page501, "/501.html");
	}

	public synchronized static byte[] get502() {
		return page502 = extractPage(page502, "/502.html");
	}
}
