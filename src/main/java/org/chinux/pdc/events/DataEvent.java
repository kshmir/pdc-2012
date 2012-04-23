package org.chinux.pdc.events;

import org.chinux.pdc.handlers.DataReceiver;

public interface DataEvent {
	public byte[] getData();

	public DataReceiver<DataEvent> getReceiver();

	public void setCanSend(boolean sendable);

	public void setCanClose(boolean closeable);

	public boolean canSend();

	public boolean canClose();
}
