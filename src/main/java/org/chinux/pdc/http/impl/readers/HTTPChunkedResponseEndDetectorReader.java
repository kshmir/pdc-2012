package org.chinux.pdc.http.impl.readers;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.util.ChunkedInputEndDetector;

public class HTTPChunkedResponseEndDetectorReader implements HTTPReader {

	private ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private ChunkedInputEndDetector chunkedEndDetector = new ChunkedInputEndDetector();
	private Logger log = Logger.getLogger(this.getClass());

	private boolean isFinished = false;

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		this.chunkedEndDetector.write(data.array());
		final ByteBuffer answer = this.chunkedEndDetector.read();

		if (this.chunkedEndDetector.chunkedInputOver()) {
			this.isFinished = true;
		}

		return answer;
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
