package org.chinux.http;

import org.chinux.pdc.http.api.HTTPRequest;
import org.chinux.pdc.http.impl.HTTPBaseRequestReader;
import org.chinux.pdc.http.impl.HTTPRequestImpl;
import org.chinux.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class HTTPBaseRequestReaderTest {

	final String data = "Hallo Welt";

	@Test
	public void processDataGETTest() {

		final HTTPRequest requestheader1 = new HTTPRequestImpl(
				TestUtils.stringFromFile("http/requests/request1.txt"));
		final HTTPBaseRequestReader requestreader1 = new HTTPBaseRequestReader(
				requestheader1);
		Assert.assertFalse(requestreader1.isFinished());
		final byte[] ans1 = requestreader1.processData(this.data.getBytes());
		Assert.assertTrue(String.valueOf(new String(ans1)).equals(this.data));
		Assert.assertTrue(requestreader1.isFinished());
	}

	@Test
	public void processDataHEADTest() {

		final String request2 = "HEAD / HTTP/1.1\r\n";
		final HTTPRequestImpl requestheader2 = new HTTPRequestImpl(request2);
		final HTTPBaseRequestReader requestreader2 = new HTTPBaseRequestReader(
				requestheader2);
		Assert.assertFalse(requestreader2.isFinished());
		final byte[] ans2 = requestreader2.processData(this.data.getBytes());
		Assert.assertTrue(String.valueOf(new String(ans2)).equals(this.data));
		Assert.assertTrue(requestreader2.isFinished());
	}

	@Test
	public void processDataPOSTTest() {
		final HTTPRequest requestheader3 = new HTTPRequestImpl(
				TestUtils.stringFromFile("http/requests/request4.txt"));
		final HTTPBaseRequestReader requestreader3 = new HTTPBaseRequestReader(
				requestheader3);
		Assert.assertFalse(requestreader3.isFinished());
		final byte[] ans3 = requestreader3.processData(this.data.getBytes());
		Assert.assertTrue(String.valueOf(new String(ans3)).equals(this.data));
		Assert.assertFalse(requestreader3.isFinished());
		final byte[] ans4 = requestreader3.processData(this.data.getBytes());
		Assert.assertTrue(String.valueOf(new String(ans4)).equals(this.data));
		Assert.assertTrue(requestreader3.isFinished());

	}
}
