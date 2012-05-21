package org.chinux.http.utils;

import junit.framework.Assert;

import org.chinux.pdc.http.util.ChunkedInputStream;
import org.junit.Test;

public class ChunkedInputStreamTest {
	@Test
	public void basicTest() {
		final String chunked = "a\r\n0123456789\r\n0\r\n\r\n";

		final ChunkedInputStream stream = new ChunkedInputStream();

		stream.write(chunked.getBytes());

		Assert.assertEquals(new String(stream.read()), "0123456789");
		Assert.assertEquals(stream.read(), null);
	}
}
