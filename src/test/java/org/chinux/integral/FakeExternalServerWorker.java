package org.chinux.integral;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.api.Worker;

public class FakeExternalServerWorker implements Worker<DataEvent> {

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) {
		dataEvent.setCanSend(true);

		// TODO: create new data event with response.txt info

		return dataEvent;
	}
}
