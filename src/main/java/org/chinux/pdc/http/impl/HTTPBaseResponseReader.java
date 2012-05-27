package org.chinux.pdc.http.impl;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.chinux.pdc.http.api.HTTPReader;
import org.chinux.pdc.workers.impl.HTTPEvent;

public class HTTPBaseResponseReader implements HTTPReader {

	private boolean finished;
	private Logger log = Logger.getLogger(this.getClass());
	private HTTPEvent event;
	private Map<HTTPReader, Integer> priorityMap = new HashMap<HTTPReader, Integer>();
	private Set<HTTPReader> readers = new TreeSet<HTTPReader>(
			new Comparator<HTTPReader>() {
				@Override
				public int compare(final HTTPReader arg0, final HTTPReader arg1) {
					if (arg0 == null && arg1 == null) {
						return 0;
					}

					if (arg1 == null) {
						return 1;
					}
					if (arg0 == null) {
						return -1;
					}
					return HTTPBaseResponseReader.this.priorityMap.get(arg0)
							.compareTo(
									HTTPBaseResponseReader.this.priorityMap
											.get(arg1));
				}
			});

	public HTTPBaseResponseReader(final HTTPEvent event) {
		this.event = event;
		this.finished = true;
	}

	public void addResponseReader(final HTTPReader reader, final int priority) {
		this.priorityMap.put(reader, priority);
		this.readers.add(reader);
	}

	@Override
	public ByteBuffer processData(ByteBuffer data) {
		for (final HTTPReader reader : this.readers) {
			data = reader.processData(data);

			if (data == null) {
				this.finished = false;
				return null;
			} else {
				this.finished = this.finished && reader.isFinished();
			}
		}

		return data;
	}

	@Override
	public boolean isFinished() {
		return this.finished;
	}

}
