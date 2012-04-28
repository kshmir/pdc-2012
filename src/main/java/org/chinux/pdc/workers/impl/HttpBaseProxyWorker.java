package org.chinux.pdc.workers.impl;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.workers.api.Worker;

public abstract class HttpBaseProxyWorker implements Worker<DataEvent> {

	protected abstract DataEvent DoWork(ClientDataEvent clientEvent);

	protected abstract DataEvent DoWork(ServerDataEvent clientEvent);

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		if (dataEvent instanceof ClientDataEvent) {
			return this.DoWork((ClientDataEvent) dataEvent);
		}

		if (dataEvent instanceof ServerDataEvent) {
			return this.DoWork((ServerDataEvent) dataEvent);
		}

		throw new RuntimeException("Invalid DataEvent type received");
	}
}
