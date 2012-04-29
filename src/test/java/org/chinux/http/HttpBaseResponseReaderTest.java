package org.chinux.http;

import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.http.impl.HTTPBaseResponseReader;
import org.chinux.pdc.http.impl.HTTPResponseImpl;
import org.chinux.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class HttpBaseResponseReaderTest {

	private byte[] image = TestUtils.stringFromFile("http/images/image1.jpg")
			.getBytes();
	final String data = "Hallo Welt";

	@Test
	public void processDataHTMLTest() {
		final HTTPResponse requestheader1 = new HTTPResponseImpl(
				TestUtils.stringFromFile("http/responses/response2.txt"));
		final HTTPBaseResponseReader requestreader1 = new HTTPBaseResponseReader(
				requestheader1);
		Assert.assertFalse(requestreader1.isFinished());
		final byte[] ans1 = requestreader1.processData(this.data.getBytes());
		Assert.assertTrue(String.valueOf(new String(ans1)).equals(this.data));
		Assert.assertTrue(requestreader1.isFinished());
	}

	@Test
	public void processDataImageLTest() {
		final HTTPResponse requestheader2 = new HTTPResponseImpl(
				TestUtils.stringFromFile("http/responses/response3.txt"));
		final HTTPBaseResponseReader requestreader2 = new HTTPBaseResponseReader(
				requestheader2);
		Assert.assertFalse(requestreader2.isFinished());
		final byte[] ans1 = requestreader2.processData(this.image);
		Assert.assertTrue(ans1 == this.image);
		Assert.assertTrue(requestreader2.isFinished());
	}
}
