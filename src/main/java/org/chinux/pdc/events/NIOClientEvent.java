package org.chinux.pdc.events;

import java.nio.channels.SocketChannel;

import org.chinux.pdc.handlers.DataReceiver;

public class NIOClientEvent extends NIODataEvent {

	public NIOClientEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<DataEvent> receiver) {
		super(socket, data, receiver);
	}

}
