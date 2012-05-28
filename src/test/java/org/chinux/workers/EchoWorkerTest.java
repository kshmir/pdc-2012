package org.chinux.workers;

import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.workers.impl.EchoWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EchoWorkerTest {

	@Test
	public void doWorkTest() {
		final EchoWorker worker = new EchoWorker();

		final ByteBuffer data = ByteBuffer.wrap("data".getBytes());

		final DataEvent event = new DataEvent(data, null) {

		};

		final DataEvent echoEvent = worker.DoWork(event);

		Assert.assertEquals(event.getData(), echoEvent.getData());
		Assert.assertEquals(true, echoEvent.canSend());
	}
}
