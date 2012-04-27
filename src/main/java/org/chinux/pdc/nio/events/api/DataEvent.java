package org.chinux.pdc.nio.events.api;

@SuppressWarnings("rawtypes")
public interface DataEvent<T extends DataEvent> {
	public byte[] getData();

	public DataReceiver<T> getReceiver();

	public void setCanSend(boolean sendable);

	public void setCanClose(boolean closeable);

	public boolean canSend();

	public boolean canClose();
}
