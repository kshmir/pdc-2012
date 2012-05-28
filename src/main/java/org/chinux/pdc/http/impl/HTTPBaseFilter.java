package org.chinux.pdc.http.impl;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.impl.filters.HTTPSizeFilter;
import org.chinux.pdc.http.impl.filters.HTTPUriFilter;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPBaseFilter implements HTTPFilter {

	private static HTTPBaseFilter baseFilter = null;
	private static Logger log = Logger.getLogger(HTTPBaseFilter.class);
	private HTTPFilter rejectedFilter;
	private Map<HTTPFilter, Integer> priorityMap = new HashMap<HTTPFilter, Integer>();
	private Set<HTTPFilter> filters = new TreeSet<HTTPFilter>(
			new Comparator<HTTPFilter>() {
				@Override
				public int compare(final HTTPFilter arg0, final HTTPFilter arg1) {
					if (arg0 == null && arg1 == null) {
						return 0;
					}

					if (arg1 == null) {
						return 1;
					}
					if (arg0 == null) {
						return -1;
					}
					return HTTPBaseFilter.this.priorityMap.get(arg0).compareTo(
							HTTPBaseFilter.this.priorityMap.get(arg1));
				}
			});

	private HTTPBaseFilter() {

	}

	public static synchronized HTTPBaseFilter getBaseFilter() {
		if (baseFilter == null) {
			baseFilter = new HTTPBaseFilter();
			baseFilter.addFilter(new HTTPSizeFilter(), 1);
			baseFilter.addFilter(new HTTPUriFilter(), 2);
		}
		return baseFilter;
	}

	public void addFilter(final HTTPFilter filter, final int priority) {
		this.priorityMap.put(filter, priority);
		this.filters.add(filter);
	}

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		for (final HTTPFilter filter : this.filters) {
			if (!filter.isValid(event)) {
				this.rejectedFilter = filter;
				return false;
			}
		}
		return true;
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return this.rejectedFilter.getErrorResponse(event);
	}

}
