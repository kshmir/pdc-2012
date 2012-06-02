package org.chinux.pdc.http.impl.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.util.ChunkedInputEndDetector;

public class HTTPChunkedResponseEndDetectorReader implements HTTPReader {

	private ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private ChunkedInputEndDetector chunkedEndDetector = new ChunkedInputEndDetector();

	private boolean isFinished = false;

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		this.chunkedEndDetector.write(data.array());

		this.stream.reset();
		ByteBuffer answer;

		while ((answer = this.chunkedEndDetector.read()) != null) {
			if (answer.array().length == 0) {
				return ByteBuffer.wrap(this.stream.toByteArray());
			}
			try {
				this.stream.write(answer.array());
			} catch (final IOException e) {

			}

			if (this.chunkedEndDetector.chunkedInputOver()) {
				this.isFinished = true;
				return ByteBuffer.wrap(this.stream.toByteArray());
			}
		}

		if (this.chunkedEndDetector.chunkedInputOver()) {
			this.isFinished = true;
		}

		return ByteBuffer.wrap(this.stream.toByteArray());
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
