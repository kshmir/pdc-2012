package org.chinux.pdc;

import java.util.Deque;
import java.util.LinkedList;

public abstract class Worker<T extends DataEvent> implements Runnable {

	private DataReceiver<T> receiver;

	public Worker(final DataReceiver<T> receiver) {
		this.receiver = receiver;
	}

	private Deque<T> events = new LinkedList<T>();

	public void processData(final T event) {
		synchronized (this.events) {
			this.events.addLast(event);
			this.events.notify();
		}
	}

	public abstract T DoWork(T dataEvent);

	/**
	 * This loop receives all the data and handles all the bussiness logic.
	 */
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

			final T event = this.DoWork(dataEvent);

			if (event.canSend()) {
				this.receiver.sendAnswer(event);
			}

			if (event.canClose()) {
				this.receiver.closeConnection(event);
			}

		}
	}

}