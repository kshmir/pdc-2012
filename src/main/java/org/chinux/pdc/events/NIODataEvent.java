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
	public byte[] data;
	private boolean canSend;
	private boolean canClose;

	public NIODataEvent(final SocketChannel socket, final byte[] data) {
		this(socket, data, false, false);
	}

	public NIODataEvent(final SocketChannel socket, final byte[] data,
			final boolean canSend, final boolean canClose) {
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
		// TODO Auto-generated method stub
		return null;
	}
}