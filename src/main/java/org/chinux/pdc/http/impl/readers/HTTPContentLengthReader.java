package org.chinux.pdc.http.impl.readers;

import java.nio.ByteBuffer;

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
		this.currlenght += data.array().length;
		if (this.currlenght >= lengthToRead) {
			this.finished = true;
		}
		return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
