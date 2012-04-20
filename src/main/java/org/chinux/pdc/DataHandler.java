package org.chinux.pdc;

public interface DataHandler<T> {
	public void accept(T key);

	public void write(T key);

	public void read(T key);
}
