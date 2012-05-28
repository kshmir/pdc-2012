package org.chinux.pdc.nio.events.impl;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.receivers.impl.CompositeDataReceiver;

public class ErrorDataEvent extends DataEvent {

	public static final int PROXY_CLIENT_DISCONNECT = 0;

	private int errorType;
	private Object owner;

	public ErrorDataEvent(final int errorType, final Object owner) {
		super(null, new CompositeDataReceiver());
		this.owner = owner;
		this.errorType = errorType;
	}

	public Object getOwner() {
		return this.owner;
	}

	public int getErrorType() {
		return this.errorType;
	}

	public CompositeDataReceiver getReceivers() {
		return (CompositeDataReceiver) this.getReceiver();
	}

}
