package org.chinux.pdc.nio.dispatchers;

import java.io.IOException;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.api.Worker;

public class SyncEventDispatcher<T extends DataEvent> implements
		EventDispatcher<T> {

	private Worker<T> worker;

	public SyncEventDispatcher(final Worker<T> worker) {
		this.worker = worker;
	}

	@Override
	public void processData(final T event) {
		T processed;
		try {
			processed = this.worker.DoWork(event);
			if (processed.canSend()) {
				processed.getReceiver().receiveEvent(processed);
			}

			if (processed.canClose()) {
				processed.getReceiver().closeConnection(processed);
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
