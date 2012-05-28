package org.chinux.pdc.http.impl.readers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPGzipReader implements HTTPReader {

	private HTTPResponseHeader responseHeader;
	private boolean isFinished = false;
	private ByteArrayOutputStream stream = new ByteArrayOutputStream();
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
		final byte[] buffer = new byte[1024];
		try {
			while (gzipInputStream.read(buffer) != -1) {
				this.stream.write(buffer);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.isFinished = true;
		final byte[] out = this.stream.toByteArray();
		this.responseHeader.removeHeader("content-encoding");
		this.responseHeader.addHeader("content-length",
				String.valueOf(out.length));
		return ByteBuffer.wrap(out);
	}

	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

	@Override
	public boolean modifiesHeaders() {
		return true;
	}

}
