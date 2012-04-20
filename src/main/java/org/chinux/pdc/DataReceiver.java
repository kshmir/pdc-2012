package org.chinux.pdc;

public interface DataReceiver<T extends DataEvent> {
	public void sendAnswer(T event);

	public void closeConnection(T event);
}
