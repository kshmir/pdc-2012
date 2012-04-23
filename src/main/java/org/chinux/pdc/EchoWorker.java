package org.chinux.pdc;

public class EchoWorker extends Worker<NIODataEvent> {

	public EchoWorker(final DataReceiver<NIODataEvent> receiver) {
		super(receiver);
	}

	@Override
	public NIODataEvent DoWork(final NIODataEvent dataEvent) {
		dataEvent.setCanSend(true);
		return dataEvent; // An echo is just 'send the same' isn't it ?
	}
}
