package org.chinux.pdc.nio.receivers.impl;

import java.util.ArrayList;
import java.util.List;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.receivers.api.DataReceiver;

/**
 * Data receiver that can send events to multiple receivers
 * 
 * @author cris
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CompositeDataReceiver implements DataReceiver<DataEvent> {

	private List<DataReceiver> receivers = new ArrayList<DataReceiver>();

	public void addReceiver(final DataReceiver receiver) {
		this.receivers.add(receiver);
	}

	@Override
	public void receiveEvent(final DataEvent event) {
		for (final DataReceiver receiver : this.receivers) {
			receiver.receiveEvent(event);
		}
	}

	@Override
	public void closeConnection(final DataEvent event) {
		for (final DataReceiver receiver : this.receivers) {
			receiver.closeConnection(event);
		}
	}
}
