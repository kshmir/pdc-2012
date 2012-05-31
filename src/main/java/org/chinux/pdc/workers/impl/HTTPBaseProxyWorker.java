package org.chinux.pdc.workers.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ClientDataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.workers.api.Worker;

public abstract class HTTPBaseProxyWorker implements Worker<DataEvent> {

	protected abstract DataEvent DoWork(ClientDataEvent clientEvent)
			throws UnsupportedEncodingException, IOException;

	protected abstract DataEvent DoWork(ErrorDataEvent errorEvent)
			throws IOException;

	protected abstract DataEvent DoWork(ServerDataEvent clientEvent)
			throws IOException;

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) throws IOException {
		if (dataEvent instanceof ClientDataEvent) {
			try {
				return this.DoWork((ClientDataEvent) dataEvent);
			} catch (final UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		if (dataEvent instanceof ServerDataEvent) {
			return this.DoWork((ServerDataEvent) dataEvent);
		}

		if (dataEvent instanceof ErrorDataEvent) {
			return this.DoWork((ErrorDataEvent) dataEvent);
		}

		throw new RuntimeException("Invalid DataEvent type received");
	}
}
