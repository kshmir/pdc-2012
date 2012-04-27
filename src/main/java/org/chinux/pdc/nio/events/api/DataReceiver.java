package org.chinux.pdc.nio.events.api;


public interface DataReceiver<T extends DataEvent> {

	public void receiveEvent(T event);

	public void closeConnection(T event);
}
