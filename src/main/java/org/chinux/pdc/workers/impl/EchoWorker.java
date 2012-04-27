package org.chinux.pdc.workers.impl;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.api.Worker;

public class EchoWorker implements Worker<DataEvent> {

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		dataEvent.setCanSend(true);
		return dataEvent; // An echo is just 'send the same' isn't it ?
	}
}
