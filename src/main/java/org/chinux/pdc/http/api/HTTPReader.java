package org.chinux.pdc.http.api;

public interface HTTPReader {

	/**
	 * Processes the data and returns the filtered data.
	 * 
	 * @param data
	 *            The read data, can be of any size and even empty.
	 * @return null if nothing can be send yet. Or the byte array if something
	 *         can be sent.
	 */
	public byte[] processData(byte[] data);

	/**
	 * @return true if the http request is done.
	 */
	public boolean isFinished();

}
