package org.chinux.pdc.http.api;

public interface HTTPReader {

	public byte[] processData(byte[] data);

	public boolean isFinished();

}
