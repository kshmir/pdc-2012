package org.chinux.pdc.nio.services.util;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.Selector;

public interface ServerSelectorFactory {

	public Selector getSelector(final InetAddress host, final int port)
			throws IOException;

}