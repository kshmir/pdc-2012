package org.chinux.pdc.nio.services.util;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

public class ClientSelectorFactory {

	public ClientSelectorFactory() {

	}

	public Selector getSelector() throws IOException {
		return this.initSelector();
	}

	private Selector initSelector() throws IOException {
		return SelectorProvider.provider().openSelector();
	}

}
