package org.chinux.http;

import junit.framework.Assert;

import org.chinux.pdc.http.api.HTTPResponse;
import org.chinux.pdc.http.impl.HTTPResponseImpl;
import org.chinux.util.TestUtils;
import org.junit.Test;

public class HTTPResponseImplTest {

	@Test
	public void ResponseImplTest() {
		final HTTPResponse req = new HTTPResponseImpl(
				TestUtils.stringFromFile("http/responses/response1.txt"));
		Assert.assertEquals("1354", req.getHeader("content-length"));
		Assert.assertEquals(200, req.returnStatusCode());
	}
}
