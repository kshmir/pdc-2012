package org.chinux.pdc.events;

import java.nio.channels.SocketChannel;

import org.chinux.pdc.handlers.DataReceiver;

/**
 * Represents any kind of processed data ready to be sent to a server
 * 
 * @author cris
 * 
 */
public class NIODataEvent implements DataEvent {
	public SocketChannel socket;
	public DataReceiver<DataEvent> receiver;
	public byte[] data;
	private boolean canSend;
	private boolean canClose;

	public NIODataEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<DataEvent> receiver) {
		this(socket, data, receiver, false, false);
	}

	public NIODataEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<DataEvent> receiver, final boolean canSend,
			final boolean canClose) {
		this.receiver = receiver;
		this.socket = socket;
		this.data = data;
		this.canSend = canSend;
		this.canClose = canClose;
	}

	@Override
	public void setCanClose(final boolean closeable) {
		this.canClose = closeable;
	}

	@Override
	public void setCanSend(final boolean sendable) {
		this.canSend = sendable;
	}

	@Override
	public byte[] getData() {
		return this.data;
	}

	@Override
	public boolean canSend() {
		return this.canSend;
	}

	@Override
	public boolean canClose() {
		return this.canClose;
	}

	@Override
	public DataReceiver<DataEvent> getReceiver() {
		return this.receiver;
	}
}