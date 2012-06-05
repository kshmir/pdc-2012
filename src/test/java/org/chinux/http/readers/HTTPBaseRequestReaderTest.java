package org.chinux.http.readers;

import java.nio.ByteBuffer;

import org.chinux.pdc.FilterException;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.impl.HTTPBaseReader;
import org.chinux.pdc.http.impl.HTTPRequestHeaderImpl;
import org.chinux.pdc.server.MonitorObject;
import org.chinux.util.TestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class HTTPBaseRequestReaderTest {

	final String data = "Hallo Welt";

	@Ignore
	@Test
	public void processDataGETTest() throws FilterException {
		final MonitorObject monitorObject = new MonitorObject();

		final HTTPRequestHeader requestheader1 = new HTTPRequestHeaderImpl(
				TestUtils.stringFromFile("http/requests/request1.txt"));
		final HTTPReader requestreader1 = new HTTPBaseReader(requestheader1,
				monitorObject);
		Assert.assertFalse(requestreader1.isFinished());
		final byte[] ans1 = requestreader1.processData(
				ByteBuffer.wrap(this.data.getBytes())).array();
		Assert.assertTrue(String.valueOf(new String(ans1)).equals(this.data));
		Assert.assertTrue(requestreader1.isFinished());
	}

	@Ignore
	@Test
	public void processDataHEADTest() throws FilterException {
		final MonitorObject monitorObject = new MonitorObject();

		final String request2 = "HEAD / HTTP/1.1\r\n";
		final HTTPRequestHeader requestheader2 = new HTTPRequestHeaderImpl(
				request2);
		final HTTPReader requestreader2 = new HTTPBaseReader(requestheader2,
				monitorObject);
		Assert.assertFalse(requestreader2.isFinished());
		final byte[] ans2 = requestreader2.processData(
				ByteBuffer.wrap(this.data.getBytes())).array();
		Assert.assertTrue(String.valueOf(new String(ans2)).equals(this.data));
		Assert.assertTrue(requestreader2.isFinished());
	}

	@Ignore
	@Test
	public void processDataPOSTTest() throws FilterException {
		final MonitorObject monitorObject = new MonitorObject();
		final HTTPRequestHeader requestheader3 = new HTTPRequestHeaderImpl(
				TestUtils.stringFromFile("http/requests/request4.txt"));
		final HTTPReader requestreader3 = new HTTPBaseReader(requestheader3,
				monitorObject);
		Assert.assertFalse(requestreader3.isFinished());
		final byte[] ans3 = requestreader3.processData(
				ByteBuffer.wrap(this.data.getBytes())).array();
		Assert.assertTrue(String.valueOf(new String(ans3)).equals(this.data));
		Assert.assertFalse(requestreader3.isFinished());
		final byte[] ans4 = requestreader3.processData(
				ByteBuffer.wrap(this.data.getBytes())).array();
		Assert.assertTrue(String.valueOf(new String(ans4)).equals(this.data));
		Assert.assertTrue(requestreader3.isFinished());

	}
}
