package org.chinux.http.headers;

import junit.framework.Assert;

import org.chinux.pdc.FilterException;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.impl.HTTPRequestHeaderImpl;
import org.chinux.util.TestUtils;
import org.junit.Test;

public class HTTPRequestHeaderImplTest {

	@Test
	public void GetNoParamTest() throws FilterException {
		final HTTPRequestHeader req = new HTTPRequestHeaderImpl(
				TestUtils.stringFromFile("http/requests/request1.txt"));
		Assert.assertEquals("GET", req.getMethod());
		Assert.assertEquals("/kshmir/pdc-2012/issues/3", req.getRequestURI());
		Assert.assertEquals("keep-alive", req.getHeader("Connection"));
	}

	@Test
	public void GetParamTest() throws FilterException {
		final HTTPRequestHeader req = new HTTPRequestHeaderImpl(
				TestUtils.stringFromFile("http/requests/request2.txt"));
		Assert.assertEquals("GET", req.getMethod());
		Assert.assertEquals(
				"/kshmir/pdc-2012/issues/3?licenseID=string&content=string&paramsXML=string",
				req.getRequestURI());
		Assert.assertEquals("keep-alive", req.getHeader("Connection"));
	}

	@Test
	public void PostTest() throws FilterException {
		final HTTPRequestHeader req = new HTTPRequestHeaderImpl(
				TestUtils.stringFromFile("http/requests/request3.txt"));
		Assert.assertEquals("POST", req.getMethod());
		Assert.assertEquals("/enlighten/calais.asmx/Enlighten",
				req.getRequestURI());
		Assert.assertEquals("123", req.getHeader("Content-Length"));
	}
}
