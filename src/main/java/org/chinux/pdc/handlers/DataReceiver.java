package org.chinux.pdc.handlers;

import org.chinux.pdc.events.DataEvent;

public interface DataReceiver<T extends DataEvent> {

	public void sendAnswer(T event);

	public void closeConnection(T event);
}
