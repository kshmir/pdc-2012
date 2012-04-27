package org.chinux.pdc.nio.events.impl;

import java.nio.channels.SocketChannel;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.api.DataReceiver;

public class NIOClientEvent extends NIODataEvent {

	public NIOClientEvent(final SocketChannel socket, final byte[] data,
			final DataReceiver<DataEvent> receiver) {
		super(socket, data, receiver);
	}

}
