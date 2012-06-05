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

import org.chinux.pdc.http.api.HTTPDelimiterReader;
import org.chinux.pdc.http.api.HTTPMessageHeader;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.impl.readers.HTTPImageResponseReader;
import org.chinux.pdc.http.impl.readers.HTTPL33tEncoder;
import org.chinux.pdc.server.MonitorObject;
import org.chinux.pdc.workers.impl.HTTPConnectionCloseReader;

public class HTTPBaseReader implements HTTPDelimiterReader,
		HTTPConnectionCloseReader {

	private MonitorObject monitorObject;
	private boolean finished;
	private boolean connectionClosed = false;
	private boolean mustConcatHeaders;
	private HTTPMessageHeader header;
	private ByteBuffer offsetByteBuffer = null;
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

	public HTTPBaseReader(final HTTPMessageHeader header,
			final MonitorObject monitorObject) {
		this.header = header;
		this.finished = true;
		this.monitorObject = monitorObject;
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

		this.finished = true;

		if (data.array().length == 0) {
			this.finished = this.connectionClosed;
			return ByteBuffer.allocate(0);
		}
		for (final HTTPReader reader : this.readers) {
			data = reader.processData(data);

			this.updateMonitorObject(reader);

			if (reader instanceof HTTPDelimiterReader && reader.isFinished()) {
				this.offsetByteBuffer = ((HTTPDelimiterReader) reader)
						.getDataOffset();
			}

			if (data == null) {
				this.finished = false;
				return null;
			} else {
				this.finished = this.finished && reader.isFinished();
			}
		}

		if (this.mustConcatHeaders && this.finished) {
			data = this.concatHeader(data);
		}
		return data;
	}

	private void updateMonitorObject(final HTTPReader reader) {
		if (reader instanceof HTTPL33tEncoder) {
			synchronized (this) {
				if (reader.isFinished()) {
					this.monitorObject.increaseText2L33tQuant();
				}
			}
		}
		if (reader instanceof HTTPImageResponseReader) {
			synchronized (this) {
				if (reader.isFinished()) {
					this.monitorObject.increaseImageFlipsQuant();
				}
			}
		}
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
		for (final HTTPReader reader : this.readers) {
			if (reader.modifiesHeaders()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ByteBuffer getDataOffset() {
		if (this.offsetByteBuffer == null) {
			return ByteBuffer.allocate(0);
		} else {
			return this.offsetByteBuffer;
		}
	}

	@Override
	public void setIsConnectionClosed(final boolean closed) {
		for (final HTTPReader reader : this.readers) {
			if (reader instanceof HTTPConnectionCloseReader) {
				((HTTPConnectionCloseReader) reader)
						.setIsConnectionClosed(closed);
			}
		}
		this.connectionClosed = closed;
	}
}
