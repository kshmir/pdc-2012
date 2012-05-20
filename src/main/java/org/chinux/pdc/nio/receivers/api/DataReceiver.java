package org.chinux.pdc.nio.receivers.api;

import org.chinux.pdc.nio.events.api.DataEvent;

public interface DataReceiver<T extends DataEvent> {

	/**
	 * Receive a dataEvent from a dispatcher, it can be opening a new
	 * connection, writing data, handling errros etc.
	 * 
	 * @param event
	 */
	public void receiveEvent(T event);

	/**
	 * Closes a connection, can be done after a receipt of an event.
	 * 
	 * @param event
	 */
	public void closeConnection(T event);
}
