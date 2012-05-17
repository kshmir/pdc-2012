package org.chinux.pdc.http.impl;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPChunkedResponseReader implements HTTPReader {

	private HTTPResponseHeader responseHeader;
	private boolean isFinished = false;

	private int buffer_i;
	private byte[] buffer;

	private final Integer currentLen = null;

	public HTTPChunkedResponseReader(final HTTPResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	@Override
	public byte[] processData(final byte[] data) {
		final String parsed = new String(data);

		final int i = 0;

		String lenNumber = "";
		while (i < parsed.length()) {

			if (this.currentLen == null || this.currentLen == 0) {
				final char nextChar = parsed.charAt(i);
				if (Character.isDigit(nextChar)) {
					lenNumber = lenNumber + nextChar;
				} else if (this.currentLen == null) {
					throw new RuntimeException(
							"Expected a number at the beggining");
				}

			} else {
				// this.buffer[buffer_i]
			}
		}

		return data;
	}

	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

}
