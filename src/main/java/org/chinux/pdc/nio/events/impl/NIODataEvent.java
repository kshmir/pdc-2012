package org.chinux.pdc.nio.events.impl;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.api.DataReceiver;

/**
 * Represents any kind of processed data ready to be sent to a receiver
 * 
 * @author cris
 * 
 */
public abstract class NIODataEvent implements DataEvent<NIODataEvent> {

	private DataReceiver<NIODataEvent> receiver;
	private byte[] data;

	private boolean canSend;
	private boolean canClose;

	public NIODataEvent(final byte[] data,
			final DataReceiver<NIODataEvent> receiver) {
		this(data, receiver, false, false);
	}

	public NIODataEvent(final byte[] data,
			final DataReceiver<NIODataEvent> receiver, final boolean canSend,
			final boolean canClose) {
		this.receiver = receiver;
		this.data = data;
		this.canSend = canSend;
		this.canClose = canClose;
	}

	@Override
	public void setCanClose(final boolean closeable) {
		canClose = closeable;
	}

	@Override
	public void setCanSend(final boolean sendable) {
		canSend = sendable;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public boolean canSend() {
		return canSend;
	}

	@Override
	public boolean canClose() {
		return canClose;
	}

	@Override
	public DataReceiver<NIODataEvent> getReceiver() {
		return receiver;
	}
}