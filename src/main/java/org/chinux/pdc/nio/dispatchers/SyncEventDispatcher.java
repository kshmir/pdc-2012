package org.chinux.pdc.nio.dispatchers;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.Worker;

public class SyncEventDispatcher<T extends DataEvent> implements
		EventDispatcher<T> {

	private Worker<T> worker;

	public SyncEventDispatcher(final Worker<T> worker) {
		this.worker = worker;
	}

	@Override
	public void processData(final T event) {
		final T processed = worker.DoWork(event);

		if (processed.canSend()) {
			processed.getReceiver().receiveEvent(processed);
		}

		if (event.canClose()) {
			processed.getReceiver().closeConnection(processed);
		}
	}
}
