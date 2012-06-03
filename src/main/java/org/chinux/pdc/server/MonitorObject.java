package org.chinux.pdc.server;

import org.chinux.pdc.nio.events.impl.ServerDataEvent;

public class MonitorObject {

	private ServerDataEvent serverEvent;
	private boolean newServerEvent = false;

	public MonitorObject() {
		this.serverEvent = null;
		this.newServerEvent = false;
	}

	public ServerDataEvent getServerEvent() {
		return this.serverEvent;
	}

	public void setServerEvent(final ServerDataEvent serverEvent) {
		this.serverEvent = serverEvent;
	}

	public boolean isNewServerEvent() {
		return this.newServerEvent;
	}

	public void setNewServerEvent(final boolean newServerEvent) {
		this.newServerEvent = newServerEvent;
	}

	@Override
	public String toString() {
		return "MonitorObject [serverEvent=" + this.serverEvent
				+ ", newServerEvent=" + this.newServerEvent + "]";
	}

}
