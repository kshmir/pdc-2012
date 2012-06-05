package org.chinux.pdc.http.impl.filters;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.impl.HTTPBaseFilter;
import org.chinux.pdc.http.util.ErrorPageProvider;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPMediaTypesFilter implements HTTPFilter {

	private static Logger log = Logger.getLogger(HTTPBaseFilter.class);

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		if (event.getResponse() == null) {
			return true;
		}
		if (!event.getResponse().getHeaders().containsHeader("Content-Type")) {
			return true;
		}
		final String reqTypes = event.getResponse().getHeaders()
				.getHeader("Content-Type");
		final String[] reqTypesList = reqTypes.split(";");
		final List<String> bloquedMediaTypes = event.getEventConfiguration()
				.getBlockedMediaTypes();
		for (final String str : reqTypesList) {
			if (bloquedMediaTypes.contains(str)) {
				log.info("Blocked media type: "
						+ event.getResponse().getHeaders()
								.containsHeader("Content-Type"));
				return false;
			}
		}
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return ByteBuffer.wrap(ErrorPageProvider.get403());
	}

}
