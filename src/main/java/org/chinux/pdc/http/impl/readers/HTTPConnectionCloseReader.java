package org.chinux.pdc.http.impl.readers;

import org.chinux.pdc.http.api.HTTPReader;

public interface HTTPConnectionCloseReader extends HTTPReader {
	public void setIsConnectionClosed(final boolean closed);
}
