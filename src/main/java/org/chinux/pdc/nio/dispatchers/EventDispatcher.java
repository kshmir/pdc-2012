package org.chinux.pdc.nio.dispatchers;

import org.chinux.pdc.nio.events.api.DataEvent;

public interface EventDispatcher<T extends DataEvent> {
	public void processData(final T event);
}
