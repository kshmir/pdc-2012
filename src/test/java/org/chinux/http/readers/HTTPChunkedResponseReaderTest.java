package org.chinux.http.readers;

import java.nio.ByteBuffer;

import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPChunkedResponseReader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.util.TestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class HTTPChunkedResponseReaderTest {
	@Ignore
	@Test
	public void chunkedDataTest() {

		final String response = TestUtils
				.stringFromFile("http/responses/response5.txt");
		final String responseHeader = response.split("\\r\\n\\r\\n", 2)[0];
		final String responseBody = response.split("\\r\\n\\r\\n", 2)[1];
		final HTTPResponseHeader responseheader1 = new HTTPResponseHeaderImpl(
				responseHeader);
		final byte[] dataNonChunked = TestUtils.stringFromFile(
				"http/responses/response5_unchunked.txt").getBytes();
		final HTTPChunkedResponseReader requestreader1 = new HTTPChunkedResponseReader(
				responseheader1);
		// Assert.assertFalse(requestreader1.isFinished());
		final byte[] ans1 = requestreader1.processData(
				ByteBuffer.wrap(responseBody.getBytes())).array();

		final String stringNonChunked = new String(dataNonChunked);

		Assert.assertEquals(stringNonChunked.trim(), new String(ans1).trim());

		// Assert.assertTrue(requestreader1.isFinished());
	}
}
