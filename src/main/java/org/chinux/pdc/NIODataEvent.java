package org.chinux.pdc;

import java.nio.channels.SocketChannel;

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

	public void setCanClose(final boolean closeable) {
		this.canClose = closeable;
	}

	public void setCanSend(final boolean sendable) {
		this.canSend = sendable;
	}

	public byte[] getData() {
		return this.data;
	}

	public boolean canSend() {
		return this.canSend;
	}

	public boolean canClose() {
		return this.canClose;
	}
}