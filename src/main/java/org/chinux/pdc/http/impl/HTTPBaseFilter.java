package org.chinux.pdc.http.impl;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPFilter;
import org.chinux.pdc.http.impl.filters.HTTPAllAccessFilter;
import org.chinux.pdc.http.impl.filters.HTTPIPFilter;
import org.chinux.pdc.http.impl.filters.HTTPMediaTypesFilter;
import org.chinux.pdc.http.impl.filters.HTTPSizeFilter;
import org.chinux.pdc.http.impl.filters.HTTPUriFilter;
import org.chinux.pdc.server.MonitorObject;
import org.chinux.pdc.workers.impl.HTTPProxyEvent;

public class HTTPBaseFilter implements HTTPFilter {

	private MonitorObject monitorObject;
	private static HTTPBaseFilter baseResponseFilter = null;
	private static HTTPBaseFilter baseRequestFilter = null;
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

	public static synchronized HTTPBaseFilter getBaseRequestFilter(
			final MonitorObject monitorObject) {
		if (baseRequestFilter == null) {
			baseRequestFilter = new HTTPBaseFilter();
			baseRequestFilter.monitorObject = monitorObject;
			baseRequestFilter.addFilter(new HTTPUriFilter(), 1);
			baseRequestFilter.addFilter(new HTTPAllAccessFilter(), 2);
			baseRequestFilter.addFilter(new HTTPIPFilter(), 3);
		}
		return baseRequestFilter;
	}

	public static synchronized HTTPBaseFilter getBaseResponseFilter(
			final MonitorObject monitorObject) {
		if (baseResponseFilter == null) {
			baseResponseFilter = new HTTPBaseFilter();
			baseResponseFilter.monitorObject = monitorObject;
			baseResponseFilter.addFilter(new HTTPSizeFilter(), 1);
			baseResponseFilter.addFilter(new HTTPMediaTypesFilter(), 2);
		}
		return baseResponseFilter;
	}

	public void addFilter(final HTTPFilter filter, final int priority) {
		this.priorityMap.put(filter, priority);
		this.filters.add(filter);
	}

	@Override
	public boolean isValid(final HTTPProxyEvent event) {
		for (final HTTPFilter filter : this.filters) {
			if (!filter.isValid(event)) {
				this.updateMonitorObject(filter);
				this.rejectedFilter = filter;
				return false;
			}
		}
		return true;
	}

	private void updateMonitorObject(final HTTPFilter filter) {
		if (filter instanceof HTTPIPFilter) {
			this.monitorObject.increaseIpBlocksQuant();
		} else if (filter instanceof HTTPAllAccessFilter) {
			this.monitorObject.increaseAllAccessBlocksQuant();
		} else if (filter instanceof HTTPUriFilter) {
			this.monitorObject.increaseUrlBlocksQuant();
		} else if (filter instanceof HTTPMediaTypesFilter) {
			this.monitorObject.increaseContentTypeBlocksQuant();
		} else if (filter instanceof HTTPSizeFilter) {
			this.monitorObject.increaseTooBigResourceBlocksQuant();
		}
	}

	@Override
	public ByteBuffer getErrorResponse(final HTTPProxyEvent event) {
		return this.rejectedFilter.getErrorResponse(event);
	}

}
