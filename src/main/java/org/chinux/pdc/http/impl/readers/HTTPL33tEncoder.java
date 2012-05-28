package org.chinux.pdc.http.impl.readers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPL33tEncoder implements HTTPReader {

	public static Charset charset = Charset.forName("ISO-8859-1");
	public static CharsetEncoder encoder = charset.newEncoder();
	public static CharsetDecoder decoder = charset.newDecoder();
	private HTTPResponseHeader responseHeader;
	private boolean isFinished = false;

	public HTTPL33tEncoder(final HTTPResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		final String aux = byteBufferToString(data);
		final String out = translate(aux);
		return stringToByteBuffer(out);

	}

	@Override
	public boolean isFinished() {
		return this.isFinished;
	}

	@Override
	public boolean modifiesHeaders() {
		return false;
	}

	public static String translate(final String str) {
		String out = str;
		final String[] english = { "A", "a", "B", "C", "E", "G", "g", "H", "I",
				"i", "L", "O", "R", "S", "T", "X", "Z" };
		final String[] leet = { "4", "@", "8", "(", "|)", "3", "6", "9", "#",
				"1", "!", "1", "0", "12", "5", "7", "†", "x", "2" };

		for (int i = 0; i < english.length; i++) {
			out = out.replaceAll(english[i], leet[i]);
		}
		return out;
	}

	public static ByteBuffer stringToByteBuffer(final String msg) {
		try {
			return encoder.encode(CharBuffer.wrap(msg));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String byteBufferToString(final ByteBuffer buffer) {
		String data = "";
		try {
			final int old_position = buffer.position();
			data = decoder.decode(buffer).toString();
			// reset buffer's position to its original so it is not altered:
			buffer.position(old_position);
		} catch (final Exception e) {
			e.printStackTrace();
			return "";
		}
		return data;
	}

}
