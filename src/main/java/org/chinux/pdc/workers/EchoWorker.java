package org.chinux.pdc.workers;

import org.chinux.pdc.events.NIODataEvent;

public class EchoWorker extends WorkerAsync<NIODataEvent> {

	@Override
	public NIODataEvent DoWork(final NIODataEvent dataEvent) {
		dataEvent.setCanSend(true);
		return dataEvent; // An echo is just 'send the same' isn't it ?
	}
}
