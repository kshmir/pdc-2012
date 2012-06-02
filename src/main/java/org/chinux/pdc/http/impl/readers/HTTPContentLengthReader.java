package org.chinux.pdc.http.impl.readers;

import java.nio.ByteBuffer;

import org.apache.commons.lang.ArrayUtils;
import org.chinux.pdc.http.api.HTTPDelimiterReader;
import org.chinux.pdc.http.api.HTTPRequestHeader;
import org.chinux.pdc.http.api.HTTPResponseHeader;

public class HTTPContentLengthReader implements HTTPDelimiterReader {

	private Integer lengthToRead;
	private boolean finished;
	private Integer currlength;
	private HTTPResponseHeader respHeader;
	private HTTPRequestHeader reqHeader;
	private ByteBuffer dataOffset;

	public HTTPContentLengthReader(final HTTPRequestHeader reqHeader) {
		this();
		this.reqHeader = reqHeader;
	}

	public HTTPContentLengthReader(final HTTPResponseHeader respHeader) {
		this();
		this.respHeader = respHeader;
	}

	private HTTPContentLengthReader() {
		this.finished = false;
		this.currlength = 0;
	}

	private int getLengthToRead() {
		if (this.lengthToRead == null) {
			if (this.respHeader != null) {
				this.lengthToRead = Integer.valueOf(this.respHeader
						.getHeader("content-length"));
			} else {
				this.lengthToRead = Integer.valueOf(this.reqHeader
						.getHeader("content-length"));
			}
		}
		return this.lengthToRead;
	}

	@Override
	public ByteBuffer processData(final ByteBuffer data) {
		final int oldlength = this.currlength;
		this.currlength += data.array().length;
		if (this.currlength >= this.getLengthToRead()) {
			this.finished = true;
			if (this.currlength > this.getLengthToRead()) {
				this.dataOffset = ByteBuffer.wrap(ArrayUtils.subarray(
						data.array(), this.lengthToRead - oldlength,
						data.array().length));

				return ByteBuffer.wrap(ArrayUtils.subarray(data.array(), 0,
						this.lengthToRead - oldlength));
			} else {
				this.dataOffset = ByteBuffer.allocate(0);
			}

		}
		return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

	@Override
	public boolean modifiesHeaders() {
		return false;
	}

	@Override
	public ByteBuffer getDataOffset() {
		return this.dataOffset;
	}
}
