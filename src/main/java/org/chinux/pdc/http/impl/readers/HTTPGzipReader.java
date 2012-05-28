package org.chinux.pdc.http.impl.readers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPGzipReader implements HTTPReader {

	private HTTPResponseHeader responseHeader;
	private boolean isFinished = false;
	private ByteArrayInputStream stream;
	GZIPInputStream gzipInputStream = null;

	public HTTPGzipReader(final HTTPResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				data.array());
		GZIPInputStream gzipInputStream = null;
		try {
			gzipInputStream = new GZIPInputStream(byteArrayInputStream);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final byte[] out = new byte[2048];
		try {
			gzipInputStream.read(out);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		this.isFinished = true;
		return ByteBuffer.wrap(out);
	}

	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

	@Override
	public boolean modifiesHeaders() {
		return false;
	}

}
