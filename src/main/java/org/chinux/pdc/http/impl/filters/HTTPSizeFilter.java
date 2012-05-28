package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;

import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPSizeFilter implements HTTPFilter {

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

}
