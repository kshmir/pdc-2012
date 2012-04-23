package org.chinux.pdc.events;

import java.nio.channels.SocketChannel;

import org.chinux.pdc.handlers.DataReceiver;

public class NIOServerEvent extends NIODataEvent {

	public NIOServerEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<DataEvent> receiver) {
		super(socket, data, receiver);
	}

}
