package org.chinux.http.utils;

import junit.framework.Assert;

import org.chinux.pdc.http.util.ChunkedInputEndDetector;
import org.junit.Test;

public class ChunkedInputEndDetectorTest {
	@Test
	public void basicTest() {
		final String chunked = "a\r\n0123456789\r\n0\r\n\r\n";

		final ChunkedInputEndDetector stream = new ChunkedInputEndDetector();

		stream.write(chunked.getBytes());

		Assert.assertEquals("a\r\n0123456789\r\n", new String(stream.read()
				.array()));
		Assert.assertEquals("0\r\n\r\n", new String(stream.read().array()));
		Assert.assertEquals(true, stream.chunkedInputOver());
	}
}
