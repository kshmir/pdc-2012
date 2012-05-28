package org.chinux.pdc.http.impl.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.util.ChunkedInputTransformer;

public class HTTPChunkedResponseReader implements HTTPReader {

	private HTTPResponseHeader responseHeader;
	private boolean isFinished = false;

	private ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private ChunkedInputTransformer chunkedTransformer = new ChunkedInputTransformer();
	private Logger log = Logger.getLogger(this.getClass());

	public HTTPChunkedResponseReader(final HTTPResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {

		this.log.info("Getting data...");
		this.chunkedTransformer.write(data.array());
		ByteBuffer answer;

		while ((answer = this.chunkedTransformer.read()) != null) {
			if (answer.array().length == 0) {
				return null;
			}
			try {
				this.stream.write(answer.array());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		this.isFinished = true;

		final byte[] bytea = this.stream.toByteArray();
		this.responseHeader.removeHeader("transfer-encoding");
		this.responseHeader.addHeader("content-length",
				String.valueOf(bytea.length));
		return ByteBuffer.wrap(bytea);
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
