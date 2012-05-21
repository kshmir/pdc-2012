package org.chinux.pdc.workers.impl;

import java.io.UnsupportedEncodingException;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.workers.api.Worker;

public abstract class HttpBaseProxyWorker implements Worker<DataEvent> {

	protected abstract DataEvent DoWork(ClientDataEvent clientEvent)
			throws UnsupportedEncodingException;

	protected abstract DataEvent DoWork(ServerDataEvent clientEvent);

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		if (dataEvent instanceof ClientDataEvent) {
			try {
				return this.DoWork((ClientDataEvent) dataEvent);
			} catch (final UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (dataEvent instanceof ServerDataEvent) {
			return this.DoWork((ServerDataEvent) dataEvent);
		}

		throw new RuntimeException("Invalid DataEvent type received");
	}
}
