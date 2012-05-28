package org.chinux.pdc.nio.util;

import java.nio.ByteBuffer;

import org.apache.commons.lang.ArrayUtils;

public class NIOUtil {
	public static ByteBuffer readBuffer(final ByteBuffer readBuffer,
			final int numRead) {
		if (numRead > 0) {
			return ByteBuffer.wrap(ArrayUtils.subarray(readBuffer.array(), 0,
					numRead));
		} else {
			return ByteBuffer.allocate(0);
		}
	}
}
