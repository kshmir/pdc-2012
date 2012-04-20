package org.chinux.pdc;

public interface DataEvent {
	public byte[] getData();

	public void setCanSend(boolean sendable);

	public void setCanClose(boolean closeable);

	public boolean canSend();

	public boolean canClose();
}
