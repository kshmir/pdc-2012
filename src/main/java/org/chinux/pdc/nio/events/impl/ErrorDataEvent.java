package org.chinux.pdc.nio.events.impl;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.receivers.impl.CompositeDataReceiver;

public class ErrorDataEvent extends DataEvent {

	public static final int PROXY_CLIENT_DISCONNECT = 0;

	public static final int REMOTE_CLIENT_DISCONNECT = 1;

	private int errorType;
	private Object attachment;
	private Object owner;

	public ErrorDataEvent(final int errorType, final Object attachment,
			final Object owner) {
		super(null, new CompositeDataReceiver());
		this.attachment = attachment;
		this.errorType = errorType;
		this.owner = owner;
	}

	public Object getAttachment() {
		return this.attachment;
	}

	public int getErrorType() {
		return this.errorType;
	}

	public CompositeDataReceiver getReceivers() {
		return (CompositeDataReceiver) this.getReceiver();
	}

	public Object getOwner() {
		return this.owner;
	}

	public void setOwner(final Object owner) {
		this.owner = owner;
	}

}
