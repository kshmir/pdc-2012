package org.chinux.pdc.workers;

public interface Worker<T> {

	public void processData(final T event);

	public T DoWork(T dataEvent);

}
