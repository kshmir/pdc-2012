package org.chinux.pdc.nio.dispatchers;

import java.util.Deque;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.api.Worker;

public class ASyncEventDispatcher<T extends DataEvent> implements Runnable,
		EventDispatcher<T> {

	private Deque<T> events = new LinkedList<T>();
	private Worker<T> worker;

	private Logger log = Logger.getLogger(this.getClass());

	public ASyncEventDispatcher(final Worker<T> worker) {
		this.worker = worker;
	}

	@Override
	public void processData(final T event) {
		synchronized (this.events) {
			this.log.debug("Process data from event " + event.toString());
			this.events.addLast(event);
			this.events.notify();
		}
	}

	/**
	 * This loop receives all the data and handles all the business logic.
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
			this.log.debug("Do work from event" + dataEvent.toString());
			final T event = this.worker.DoWork(dataEvent);
			this.log.debug("Got event" + event.toString());
			if (event.canSend()) {
				event.getReceiver().receiveEvent(event);
			}

			if (event.canClose()) {
				event.getReceiver().closeConnection(event);
			}

		}
	}
}