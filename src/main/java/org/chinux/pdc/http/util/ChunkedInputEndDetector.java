package org.chinux.pdc.http.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

public class ChunkedInputEndDetector {
	private static final int DETECTING_LEN = 0;
	private static final int READING_CHUNK = 1;
	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private static Logger log = Logger.getLogger(ChunkedInputTransformer.class);

	private int state = DETECTING_LEN;
	private int sizeToRead = 0;
	private int sizeRead = 0;
	private boolean isOver = false;

	private ByteArrayOutputStream inStream = new ByteArrayOutputStream();
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	/**
	 * Write the chunked data to the stream.
	 */
	public void write(final byte[] data) {
		try {
			this.inStream.write(data);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read all the available data from the stream.
	 */
	public ByteBuffer read() {

		if (this.inStream.size() == 0) {
			return ByteBuffer.allocate(0);
		}
		final String str = isoCharset.decode(
				ByteBuffer.wrap(this.inStream.toByteArray())).toString();
		try {

			int readOffset = 0;

			if (this.state == DETECTING_LEN) {
				if (str.indexOf("\r\n") == -1) {
					throw new Exception("No number yet in the buffer");
				}

				final String line = str.substring(0, str.indexOf("\r\n"));
				final Integer size = Integer.valueOf(line.split(";")[0], 16);

				if (size == 0) {
					this.isOver = true;
					return ByteBuffer.wrap("0\r\n\r\n".getBytes(isoCharset));
				} else {
					this.state = READING_CHUNK;
					this.sizeToRead = size;
					this.sizeRead = 0;
					this.outputStream.reset();

					final byte[] header = isoCharset.encode(line + "\r\n")
							.array();
					readOffset = header.length;
					this.outputStream.write(header);

				}
			}

			if (this.state == READING_CHUNK) {
				int chunkToRemoveSize = 0;
				if (this.inStream.size() - this.sizeRead > this.sizeToRead) {
					chunkToRemoveSize = this.sizeToRead - this.sizeRead;
				} else {
					chunkToRemoveSize = this.inStream.size() - this.sizeRead;
				}

				this.sizeRead += chunkToRemoveSize;

				if (this.sizeRead == this.sizeToRead) {
					this.state = DETECTING_LEN;
				}

				final byte[] response = new byte[chunkToRemoveSize];

				final ByteArrayInputStream tmpStream = new ByteArrayInputStream(
						this.inStream.toByteArray());

				for (int i = 0; i < readOffset; i++) {
					tmpStream.read();
				}

				tmpStream.read(response);

				tmpStream.read(); // CR
				tmpStream.read(); // LF

				this.inStream.reset();

				int b = 0;
				while ((b = tmpStream.read()) != -1) {
					this.inStream.write(b);
				}

				this.outputStream.write(response);
				this.outputStream.write("\r\n".getBytes(isoCharset));
				return ByteBuffer.wrap(this.outputStream.toByteArray());

			}
		} catch (final BufferUnderflowException e) {
			log.debug("Buffer underrun in chunked input");
		} catch (final NumberFormatException e) {
			log.error("Invalid chunked transfer?", e);
			return null;
		} catch (final Exception e) {
			log.error("Some kind of error we didn't expect...", e);
		}
		this.inStream.reset();
		try {
			this.inStream.write(isoCharset.encode(str).array());
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		return ByteBuffer.allocate(0);

	}

	public boolean chunkedInputOver() {
		return this.isOver;
	}
}
