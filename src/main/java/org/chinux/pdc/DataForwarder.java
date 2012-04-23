package org.chinux.pdc;

public interface DataForwarder<T extends DataEvent> {

	public void sendForward(T event);

	public void closeConnection(T event);

}
