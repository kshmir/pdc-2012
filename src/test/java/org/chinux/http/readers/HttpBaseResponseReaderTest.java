package org.chinux.http.readers;

import java.io.File;

import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPBaseResponseReader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class HttpBaseResponseReaderTest {

	private File imageFile = new File(
			"src/test/resources/http/images/image1.jpg");

	final String data = "Hallo Welt";

	@Test
	public void processDataHTMLTest() {
		final HTTPResponseHeader requestheader1 = new HTTPResponseHeaderImpl(
				TestUtils.stringFromFile("http/responses/response2.txt"));
		final HTTPBaseResponseReader requestreader1 = new HTTPBaseResponseReader(
				requestheader1, false);
		Assert.assertFalse(requestreader1.isFinished());
		byte[] ans1 = null;
		ans1 = requestreader1.processData(this.data.getBytes());
		Assert.assertTrue(String.valueOf(new String(ans1)).equals(this.data));
		Assert.assertTrue(requestreader1.isFinished());
	}

}
