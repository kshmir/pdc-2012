package org.chinux.pdc.nio.dispatchers;

import java.util.Deque;
import java.util.LinkedList;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.Worker;

public class ASyncEventDispatcher<T extends DataEvent> implements Runnable,
		EventDispatcher<T> {

	private Deque<T> events = new LinkedList<T>();
	private Worker<T> worker;

	public ASyncEventDispatcher(final Worker<T> worker) {
		this.worker = worker;
	}

	@Override
	public void processData(final T event) {
		synchronized (this.events) {
			this.events.addLast(event);
			this.events.notify();
		}
	}

	/**
	 * This loop receives all the data and handles all the bussiness logic.
	 */
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
				dataEvent = this.events.poll();
			}

			final T event = worker.DoWork(dataEvent);

			if (event.canSend()) {
				event.getReceiver().receiveEvent(event);
			}

			if (event.canClose()) {
				event.getReceiver().closeConnection(event);
			}

		}
	}
}