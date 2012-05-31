package org.chinux.pdc.http.impl.readers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPL33tEncoder implements HTTPReader {

	public static Charset isoCharset = Charset.forName("ISO-8859-1");
	private CharsetEncoder encoder = null;
	private CharsetDecoder decoder = null;
	private HTTPResponseHeader responseHeader;
	private Charset charset;
	private boolean isFinished = false;

	public HTTPL33tEncoder(final HTTPResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
		this.charset = (responseHeader.getCharset() == null) ? isoCharset
				: responseHeader.getCharset();

		this.encoder = this.charset.newEncoder();
		this.decoder = this.charset.newDecoder();
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		final String aux = this.byteBufferToString(data);
		final String out = translate(aux);
		return this.stringToByteBuffer(out);
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

	private ByteBuffer stringToByteBuffer(final String msg) {
		try {
			// TODO: Esto tira exception, hay que arreglarlo, si no se puede
			// encodear hay que esperar a que se pueda!!!
			return this.encoder.encode(CharBuffer.wrap(msg));
		} catch (final Exception e) {
			e.printStackTrace();
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

}
