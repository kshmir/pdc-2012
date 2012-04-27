package org.chinux.pdc.nio.receivers.api;

import org.chinux.pdc.nio.events.api.DataEvent;

public interface DataReceiver<T extends DataEvent> {

	public void receiveEvent(T event);

	public void closeConnection(T event);
}
