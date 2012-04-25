package org.chinux.pdc.nio.services.util;

import java.io.IOException;
import java.nio.channels.Selector;

public interface ClientSelectorFactory {

	public Selector getSelector() throws IOException;

}