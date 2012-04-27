package org.chinux;

import junit.framework.Assert;

import org.chinux.pdc.HTTPResponse;
import org.chinux.pdc.HTTPResponseImpl;
import org.junit.Test;

public class HTTPResponseImplTest {

	@Test
	public void ResponseImplTest() {
		final HTTPResponse req = new HTTPResponseImpl(
				"HTTP/1.0 200 OK\nDate: Fri, 31 Dec 1999 23:59:59 GMT\nContent-Type: text/html\nContent-Length: 1354\n\n<html>\n<body>\n<h1>Hello World!</h1>\n</body>\n</html>");
		Assert.assertEquals("1354", req.getHeader("content-length"));
		Assert.assertEquals(200, req.returnStatusCode());
	}
}
