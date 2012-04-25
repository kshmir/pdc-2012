package org.chinux.pdc.nio.events.impl;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.api.DataReceiver;

/**
 * Represents any kind of processed data ready to be sent to a server
 * 
 * @author cris
 * 
 */
public class NIODataEvent implements DataEvent {

	public Object owner;

	public InetAddress inetAddress;

	public SocketChannel socket;
	public DataReceiver<NIODataEvent> receiver;
	public byte[] data;

	private boolean canSend;
	private boolean canClose;

	public NIODataEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<NIODataEvent> receiver) {
		this(socket, data, receiver, false, false);
	}

	public NIODataEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<NIODataEvent> receiver, final boolean canSend,
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

	@SuppressWarnings("unchecked")
	@Override
	public DataReceiver getReceiver() {
		return this.receiver;
	}
}