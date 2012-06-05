package org.chinux.pdc.http.api;

import java.nio.ByteBuffer;

public interface HTTPDelimiterReader extends HTTPReader {
	public ByteBuffer getDataOffset();
}
