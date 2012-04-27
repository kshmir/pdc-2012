package org.chinux.pdc.nio.events.impl;

import java.nio.channels.SocketChannel;

import org.chinux.pdc.nio.events.api.DataReceiver;

/**
 * Represents a server data event
 * 
 * @author cris
 * 
 */
public class NIOServerDataEvent extends NIODataEvent {

	private SocketChannel channel;

	public NIOServerDataEvent(final SocketChannel channel, final byte[] data) {
		this(channel, data, null);
	}

	public NIOServerDataEvent(final SocketChannel channel, final byte[] data,
			final DataReceiver<NIODataEvent> receiver) {
		super(data, receiver);

		this.channel = channel;
	}

	/**
	 * Gets the channel corresponding to this connection.
	 * 
	 * @return
	 */
	public SocketChannel getChannel() {
		return channel;
	}
}
