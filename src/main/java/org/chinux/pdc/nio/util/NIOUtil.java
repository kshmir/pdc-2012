package org.chinux.pdc.nio.util;

import java.nio.ByteBuffer;

import org.apache.commons.lang.ArrayUtils;

public class NIOUtil {
	public static byte[] readBuffer(final ByteBuffer readBuffer,
			final int numRead) {
		final byte[] data;
		if (numRead > 0) {
			data = ArrayUtils.subarray(readBuffer.array(), 0, numRead);
		} else {
			data = new byte[] {};
		}
		return data;
	}
}
