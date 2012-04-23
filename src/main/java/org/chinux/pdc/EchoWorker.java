package org.chinux.pdc;

public class EchoWorker extends Worker<NIODataEvent> {

	@Override
	public NIODataEvent DoWork(final NIODataEvent dataEvent) {
		dataEvent.setCanSend(true);
		return dataEvent; // An echo is just 'send the same' isn't it ?
	}
}
