package org.chinux.pdc;

import junit.framework.Assert;

import org.junit.Test;

public class HTTPRequestImplTest {

	@Test
	public void GetNoParamTest() {
		final HTTPRequest req = new HTTPRequestImpl(
				"GET /kshmir/pdc-2012/issues/3 HTTP/1.1\nHost: github.com\nConnection: keep-alive\nCache-Control: max-age=0");
		Assert.assertEquals("GET", req.getMethod());
		Assert.assertEquals("/kshmir/pdc-2012/issues/3", req.getRequestURI());
		Assert.assertEquals("keep-alive", req.getHeader("Connection"));
	}

	@Test
	public void GetParamTest() {
		final HTTPRequest req = new HTTPRequestImpl(
				"GET /kshmir/pdc-2012/issues/3?licenseID=string&content=string&paramsXML=string HTTP/1.1\nHost: github.com\nConnection: keep-alive\nCache-Control: max-age=0");
		Assert.assertEquals("GET", req.getMethod());
		Assert.assertEquals(
				"/kshmir/pdc-2012/issues/3?licenseID=string&content=string&paramsXML=string",
				req.getRequestURI());
		Assert.assertEquals("keep-alive", req.getHeader("Connection"));
		Assert.assertEquals("string", req.getParameter("content"));
	}

	@Test
	public void PostTest() {
		final HTTPRequest req = new HTTPRequestImpl(
				"POST /enlighten/calais.asmx/Enlighten HTTP/1.1\nHost: api.opencalais.com\nContent-Type: application/x-www-form-urlencoded\nContent-Length: 123\n\nlicenseID=string&content=string&paramsXML=string");
		Assert.assertEquals("POST", req.getMethod());
		Assert.assertEquals("/enlighten/calais.asmx/Enlighten",
				req.getRequestURI());
		Assert.assertEquals("123", req.getHeader("Content-Length"));
		Assert.assertEquals("string", req.getParameter("content"));
	}
}
