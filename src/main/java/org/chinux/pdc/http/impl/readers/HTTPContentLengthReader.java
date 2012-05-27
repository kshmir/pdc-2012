package org.chinux.pdc.http.impl.readers;

import java.nio.ByteBuffer;

import org.apache.commons.lang.ArrayUtils;
import org.chinux.pdc.http.api.HTTPReader;

public class HTTPContentLengthReader implements HTTPReader {

	private int lengthToRead;
	private boolean finished;
	private Integer currlenght;

	public HTTPContentLengthReader(final int lengthToRead) {
		this.lengthToRead = lengthToRead;
		this.finished = false;
		this.currlenght = 0;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		final int oldlength = this.currlenght;
		this.currlenght += data.array().length;
		if (this.currlenght >= this.lengthToRead) {
			this.finished = true;
			if (this.currlenght > this.lengthToRead) {
				return ByteBuffer.wrap(ArrayUtils.subarray(data.array(), 0,
						this.lengthToRead - oldlength));
			}

		}
		return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
