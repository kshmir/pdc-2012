package org.chinux.pdc.nio.events.api;

import org.chinux.pdc.nio.receivers.api.DataReceiver;

/**
 * Represents any kind of processed data ready to be sent to a receiver
 * 
 * @author cris
 * 
 */
public abstract class DataEvent {

	private DataReceiver<DataEvent> receiver;
	private byte[] data;

	private boolean canSend;
	private boolean canClose;

	public DataEvent(final byte[] data, final DataReceiver<DataEvent> receiver) {
		this(data, receiver, false, false);
	}

	public DataEvent(final byte[] data, final DataReceiver<DataEvent> receiver,
			final boolean canSend, final boolean canClose) {
		this.receiver = receiver;
		this.data = data;
		this.canSend = canSend;
		this.canClose = canClose;
	}

	public void setCanClose(final boolean closeable) {
		canClose = closeable;
	}

	public void setCanSend(final boolean sendable) {
		canSend = sendable;
	}

	public byte[] getData() {
		return data;
	}

	public boolean canSend() {
		return canSend;
	}

	public boolean canClose() {
		return canClose;
	}

	public DataReceiver<DataEvent> getReceiver() {
		return receiver;
	}
}