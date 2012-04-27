package org.chinux.pdc.workers;

import org.chinux.pdc.nio.events.api.DataEvent;

public interface Worker<T extends DataEvent> {
	public T DoWork(T dataEvent);
}
