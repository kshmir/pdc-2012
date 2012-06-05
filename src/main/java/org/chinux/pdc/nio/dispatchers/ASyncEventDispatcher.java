package org.chinux.pdc.nio.dispatchers;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.api.Worker;

public class ASyncEventDispatcher<T extends DataEvent> implements Runnable,
		MonitorableEventDispatcher, UrgentEventDispatcher<T> {

	private Deque<T> events = new LinkedList<T>();
	private Worker<T> worker;

	private Logger log = Logger.getLogger(this.getClass());

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

	@Override
	public void processDataUrgent(final T event) {
		synchronized (this.events) {

			this.events.addFirst(event);
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

				T event;
				try {
					event = this.worker.DoWork(dataEvent);

					if (event.canSend()) {
						event.getReceiver().receiveEvent(event);
					}

					if (event.canClose()) {
						event.getReceiver().closeConnection(event);
					}

				} catch (final IOException e) {
					this.log.error("Unexpected exception", e);
				} catch (final Exception e) {
					this.log.error("Unexpected exception", e);
				}
			}
		}
	}

	@Override
	public synchronized int getQueueSize() {
		return this.events.size();
	}

}