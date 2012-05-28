package org.chinux.http.readers;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.pdc.http.impl.readers.HTTPL33tEncoder;
import org.chinux.util.TestUtils;
import org.junit.Test;

public class HTTPL33tEncoderTest {

	@Test
	public void test() throws UnsupportedEncodingException {

		final HTTPResponseHeader responseheader = new HTTPResponseHeaderImpl(
				TestUtils.stringFromFile("http/responses/response6.txt"));

		final String data = TestUtils
				.stringFromFile("http/data/plaindata1.txt");

		final HTTPL33tEncoder reader = new HTTPL33tEncoder(responseheader);

		final byte[] out = reader.processData(ByteBuffer.wrap(data.getBytes()))
				.array();

		System.out.println(new String(out, "UTF-8"));

	}

}
