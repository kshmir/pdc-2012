package org.chinux.pdc.workers;

import java.util.Deque;
import java.util.LinkedList;

import org.chinux.pdc.nio.events.api.DataEvent;

public abstract class ASyncWorker<T extends DataEvent> implements Runnable,
		Worker<T> {

	private Deque<Object> events = new LinkedList<Object>();

	@Override
	public void processData(final Object event) {
		synchronized (this.events) {
			this.events.addLast(event);
			this.events.notify();
		}
	}

	/**
	 * This loop receives all the data and handles all the bussiness logic.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		T dataEvent;

		while (true) {
			synchronized (this.events) {
				while (this.events.isEmpty()) {
					try {
						this.events.wait();
					} catch (final InterruptedException e) {
					}
				}
				dataEvent = (T) this.events.poll();
			}

			final T event = this.DoWork(dataEvent);

			if (event.canSend()) {
				event.getReceiver().receiveEvent(event);
			}

			if (event.canClose()) {
				event.getReceiver().closeConnection(event);
			}

		}
	}
}