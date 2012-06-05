package org.chinux.pdc.nio.events.impl;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

/**
 * Represents a server data event
 * 
 * @author cris
 * 
 */
public class ServerDataEvent extends DataEvent {

	private SocketChannel channel;

	public ServerDataEvent(final SocketChannel channel, final ByteBuffer data) {
		this(channel, data, null);
	}

	public ServerDataEvent(final SocketChannel channel, final ByteBuffer data,
			final DataReceiver<DataEvent> receiver) {
		super(data, receiver);

		this.channel = channel;
	}

	/**
	 * Gets the channel corresponding to this connection.
	 * 
	 * @return
	 */
	public SocketChannel getChannel() {
		return this.channel;
	}

	@Override
	public String toString() {
		return "ServerDataEvent [channel=" + this.channel + ", toString()="
				+ super.toString() + "]";
	}
}
