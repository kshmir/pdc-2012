package org.chinux.pdc;

import java.nio.ByteBuffer;

@SuppressWarnings("serial")
public class FilterException extends Exception {
	private ByteBuffer response;

	public FilterException(final ByteBuffer response) {
		super();
		this.response = response;
	}

	public ByteBuffer getResponse() {
		return this.response;
	}
}