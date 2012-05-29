package org.chinux.pdc.http.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

public class ChunkedInputTransformer {

	private static Charset isoCharset = Charset.forName("ISO-8859-1");
	private static Logger log = Logger.getLogger(ChunkedInputTransformer.class);
	private ByteArrayOutputStream stream = new ByteArrayOutputStream();

	/**
	 * Write the chunked data to the stream.
	 */
	public void write(final byte[] data) {
		try {
			this.stream.write(data);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read all the available data from the stream.
	 */
	public ByteBuffer read() {

		if (this.stream.size() == 0) {
			return ByteBuffer.allocate(0);
		}

		final String str = isoCharset.decode(
				ByteBuffer.wrap(this.stream.toByteArray())).toString();

		try {
			if (str.indexOf("\r\n") == -1) {
				throw new Exception("No number yet in the buffer");
			}

			final String line = str.substring(0, str.indexOf("\r\n"));
			final Integer size = Integer.valueOf(line.split(";")[0], 16);

			if (size == 0) {
				return null;
			}

			final String data = str.substring(str.indexOf("\r\n") + 2);

			final ByteBuffer dataBuffer = isoCharset.encode(data);

			final byte[] byteData = new byte[size];

			dataBuffer.get(byteData);

			dataBuffer.position(dataBuffer.position() + 2);

			this.stream.reset();
			this.stream.write(ArrayUtils.subarray(dataBuffer.array(),
					dataBuffer.position(), dataBuffer.array().length));

			return ByteBuffer.wrap(byteData);
		} catch (final BufferUnderflowException e) {
			log.debug("Buffer underrun in chunked input");
		} catch (final NumberFormatException e) {
			log.error("Invalid chunked transfer?", e);
			return null;
		} catch (final Exception e) {
			log.error("Some kind of error we didn't expect...", e);
		}
		this.stream.reset();
		try {
			this.stream.write(isoCharset.encode(str).array());
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		return ByteBuffer.allocate(0);

	}
}
