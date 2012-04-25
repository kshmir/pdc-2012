package org.chinux.pdc.nio.events.api;

public interface DataEvent {
	public byte[] getData();

	@SuppressWarnings("rawtypes")
	public DataReceiver getReceiver();

	public void setCanSend(boolean sendable);

	public void setCanClose(boolean closeable);

	public boolean canSend();

	public boolean canClose();
}
