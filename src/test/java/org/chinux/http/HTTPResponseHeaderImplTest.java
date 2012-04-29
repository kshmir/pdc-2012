package org.chinux.http;

import junit.framework.Assert;

import org.chinux.pdc.http.api.HTTPResponseHeader;
import org.chinux.pdc.http.impl.HTTPResponseHeaderImpl;
import org.chinux.util.TestUtils;
import org.junit.Test;

public class HTTPResponseHeaderImplTest {

	@Test
	public void ResponseImplTest() {
		final HTTPResponseHeader req = new HTTPResponseHeaderImpl(
				TestUtils.stringFromFile("http/responses/response1.txt"));
		Assert.assertEquals("1354", req.getHeader("content-length"));
		Assert.assertEquals(200, req.returnStatusCode());
	}
}
