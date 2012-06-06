package org.chinux.pdc.http.impl.readers;

import java.nio.ByteBuffer;

public class HTTPOneDotOneReader implements HTTPConnectionCloseReader {

	private boolean isfinished;

	@Override
	public void setIsConnectionClosed(final boolean closed) {
		this.isfinished = closed;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		return data;
	}

	@Override
	public boolean isFinished() {
		return this.isfinished;
	}

	@Override
	public boolean modifiesHeaders() {
		return false;
	}

}
