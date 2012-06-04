package org.chinux.pdc.nio.dispatchers;

import org.chinux.pdc.nio.events.api.DataEvent;

public interface UrgentEventDispatcher<T extends DataEvent> extends
		EventDispatcher<T> {

	public void processDataUrgent(final T event);

}