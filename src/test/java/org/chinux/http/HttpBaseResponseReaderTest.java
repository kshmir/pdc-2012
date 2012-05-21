package org.chinux.http;

import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPBaseResponseReader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.util.TestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class HttpBaseResponseReaderTest {

	private byte[] image = TestUtils.stringFromFile("http/images/image1.jpg")
			.getBytes();
	final String data = "Hallo Welt";

	@Test
	public void processDataHTMLTest() {
		final HTTPResponseHeader requestheader1 = new HTTPResponseHeaderImpl(
				TestUtils.stringFromFile("http/responses/response2.txt"));
		final HTTPBaseResponseReader requestreader1 = new HTTPBaseResponseReader(
				requestheader1, false);
		Assert.assertFalse(requestreader1.isFinished());
		final byte[] ans1 = requestreader1.processData(this.data.getBytes());
		Assert.assertTrue(String.valueOf(new String(ans1)).equals(this.data));
		Assert.assertTrue(requestreader1.isFinished());
	}

	// Esto está siendo dependiente de la implementación, conviene comparar
	// contra imágenes ya guardadas.
	// Y no hardcodear el content-length
	@Ignore
	@Test
	public void processDataImageLTest() {
		final HTTPResponseHeader requestheader2 = new HTTPResponseHeaderImpl(
				TestUtils.stringFromFile("http/responses/response3.txt"));
		final HTTPBaseResponseReader requestreader2 = new HTTPBaseResponseReader(
				requestheader2, false);
		Assert.assertFalse(requestreader2.isFinished());
		final byte[] ans1 = requestreader2.processData(this.image);
		Assert.assertTrue(ans1 == this.image);
		Assert.assertTrue(requestreader2.isFinished());
	}
}
