package org.chinux.pdc.http.util;

public class ChunkedInputStream {

	private StringBuilder builder = new StringBuilder();

	/**
	 * Write the chunked data to the stream.
	 */
	public void write(final byte[] data) {
		this.builder.append(new String(data));
	}

	/**
	 * Read all the available data from the stream.
	 */
	public byte[] read() {

		if (this.builder.length() == 0) {
			return new byte[] {};
		}

		final String str = this.builder.toString();

		try {
			final String line = str.split("\\r\\n", 2)[0];
			final Integer size = Integer.valueOf(line.split(";")[0], 16);

			if (size == 0) {
				return null;
			}

			final int index = line.length() + 2;

			this.builder.delete(0, index);

			final byte[] data = this.builder.toString().substring(0, size)
					.getBytes();

			System.out.println(new String(data));

			this.builder.delete(0, size + 2);

			System.out.println(size);
			return data;
		} catch (final NumberFormatException e) {
			e.printStackTrace();
			return null;
		} catch (final Exception e) {
			e.printStackTrace();
			this.builder = new StringBuilder();
			this.builder.append(str);
			return new byte[] {};
		}

	}
}
