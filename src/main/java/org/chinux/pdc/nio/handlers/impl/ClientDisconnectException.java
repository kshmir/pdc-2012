package org.chinux.pdc.nio.handlers.impl;

import org.chinux.pdc.nio.services.util.ChangeRequest;

public class ClientDisconnectException extends Exception {

	private ChangeRequest change;

	public ClientDisconnectException(final ChangeRequest change) {
		this.change = change;
	}

	public ChangeRequest getChange() {
		return this.change;
	}

}
