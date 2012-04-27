package org.chinux.pdc.workers;

import org.chinux.pdc.nio.events.api.DataEvent;

@SuppressWarnings("rawtypes")
public interface Worker<T extends DataEvent> {

	public void processData(final T event);

	public T DoWork(T dataEvent);

}
