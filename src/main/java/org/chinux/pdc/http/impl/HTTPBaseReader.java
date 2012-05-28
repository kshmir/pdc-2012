package org.chinux.pdc.http.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPMessageHeader;
import org.chinux.pdc.http.api.HTTPReader;

public class HTTPBaseReader implements HTTPReader {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean finished;
	private boolean mustConcatHeaders;
	private HTTPMessageHeader header;
	private Map<HTTPReader, Integer> priorityMap = new HashMap<HTTPReader, Integer>();
	private Set<HTTPReader> readers = new TreeSet<HTTPReader>(
			new Comparator<HTTPReader>() {
				@Override
				public int compare(final HTTPReader arg0, final HTTPReader arg1) {
					if (arg0 == null && arg1 == null) {
						return 0;
					}

					if (arg1 == null) {
						return 1;
					}
					if (arg0 == null) {
						return -1;
					}
					return HTTPBaseReader.this.priorityMap.get(arg0).compareTo(
							HTTPBaseReader.this.priorityMap.get(arg1));
				}
			});

	public HTTPBaseReader(final HTTPMessageHeader header) {
		this.header = header;
		this.finished = true;
	}

	public void addResponseReader(final HTTPReader reader, final int priority) {
		this.priorityMap.put(reader, priority);
		this.readers.add(reader);
		if (reader.modifiesHeaders()) {
			this.mustConcatHeaders = true;
		}
	}

	@Override
	public ByteBuffer processData(ByteBuffer data) {
		for (final HTTPReader reader : this.readers) {
			this.log.info("Logging with " + reader);
			data = reader.processData(data);

			if (data == null) {
				this.log.info("Stuck on " + reader);
				this.finished = false;
				return null;
			} else {
				this.finished = this.finished && reader.isFinished();
			}
		}

		if (this.mustConcatHeaders) {
			data = this.concatHeader(data);
		}
		return data;
	}

	private ByteBuffer concatHeader(final ByteBuffer data) {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final byte[] headerbytea = Charset.forName("ISO-8859-1")
				.encode(this.header.toString()).array();
		try {
			stream.write(headerbytea);
			stream.write(data.array());
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return ByteBuffer.wrap(stream.toByteArray());
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

	@Override
	public boolean modifiesHeaders() {
		return false;
	}
}
