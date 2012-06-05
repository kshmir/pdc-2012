package org.chinux.pdc.http.impl.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPL33tEncoder implements HTTPReader {

	private Charset charset = Charset.forName("ISO-8859-1");
	private CharsetEncoder encoder = this.charset.newEncoder();
	private CharsetDecoder decoder = this.charset.newDecoder();
	private HTTPResponseHeader responseHeader;
	private ByteArrayOutputStream stream = new ByteArrayOutputStream();
	private boolean isFinished = false;

	public HTTPL33tEncoder(final HTTPResponseHeader responseHeader) {
		this.responseHeader = responseHeader;

		this.charset = (responseHeader.getCharset() == null) ? this.charset
				: responseHeader.getCharset();

		this.encoder = this.charset.newEncoder();
		this.decoder = this.charset.newDecoder();
	}

	private String buff = null;
	private int currentContentLenght = 0;
	private Integer bytesToRead = null;

	private int bytesRead = 0;

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		String aux;
		/* get the number of bytes of the original response */
		if (this.is300Response() || this.getBytesToRead() == 0) {
			if (data.array().length == 0) {
				this.isFinished = true;
			}
			return data;
		}
		/* get the number of bytes currently read */
		this.bytesRead += data.array().length;

		if ((aux = this.byteBufferToString(data)) == null) {
			this.isFinished = false;
			// return null;
		}
		if (this.buff != null) {
			aux = this.buff + aux;
		}
		final String out = translate(aux);

		final ByteBuffer ans;
		if ((ans = this.stringToByteBuffer(out)) == null) {
			this.isFinished = false;
			// return null;
		}

		if (ans != null) {
			try {
				this.stream.write(ans.array());
			} catch (final IOException e) {
			}
		}
		/*
		 * if the number of bytes read is greater or equal to the original
		 * number of bytes of the response, then the reader has finished
		 * processing
		 */
		if (this.bytesRead >= this.bytesToRead) {
			final byte[] array = this.stream.toByteArray();
			this.currentContentLenght = array.length;
			this.responseHeader.removeHeader("Content-length");
			this.responseHeader.addHeader("Content-length",
					String.valueOf(this.currentContentLenght));
			this.isFinished = true;
			return ByteBuffer.wrap(array);
		} else {

			return null;
		}

	}

	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

	@Override
	public boolean modifiesHeaders() {
		return true;
	}

	public static String translate(final String str) {
		String out = str;
		final String[] english = { "A", "a", "B", "C", "E", "G", "g", "H", "I",
				"i", "L", "O", "R", "S", "X", "Z" };
		final String[] leet = { "4", "@", "8", "(", "|)", "3", "6", "9", "#",
				"1", "!", "1", "0", "12", "5", "7", "x", "2" };

		for (int i = 0; i < english.length; i++) {
			out = out.replaceAll(english[i], leet[i]);
		}
		return out;
	}

	private ByteBuffer stringToByteBuffer(final String msg) {
		try {
			this.buff = null;
			return this.encoder.encode(CharBuffer.wrap(msg));
		} catch (final Exception e) {
			this.buff = msg;
		}
		return null;
	}

	private String byteBufferToString(final ByteBuffer buffer) {
		String data = "";
		try {
			final int old_position = buffer.position();
			data = this.decoder.decode(buffer).toString();
			// reset buffer's position to its original so it is not altered:
			buffer.position(old_position);
		} catch (final Exception e) {
			e.printStackTrace();
			return "";
		}
		return data;
	}

	private int getBytesToRead() {
		if (this.responseHeader.getHeader("content-length") != null
				&& this.bytesToRead == null) {
			this.bytesToRead = Integer.valueOf(this.responseHeader
					.getHeader("content-length"));
		}
		return this.bytesToRead;
	}

	private boolean is300Response() {
		return this.responseHeader.returnStatusCode() >= 300
				&& this.responseHeader.returnStatusCode() <= 399;
	}

}
