package org.chinux.pdc.workers.impl;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;

public class HttpProxyWorker extends HttpBaseProxyWorker {

	@Override
	protected DataEvent DoWork(final ClientDataEvent clientEvent) {
		return clientEvent;
	}

	@Override
	protected DataEvent DoWork(final ServerDataEvent clientEvent) {
		return clientEvent;
	}
}
