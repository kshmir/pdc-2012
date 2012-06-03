package org.chinux.pdc.workers.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.LoginService;
import org.chinux.pdc.server.LoginService.Code;
import org.chinux.pdc.server.MonitorObject;

public class MonitorWorker extends LogueableWorker {

	public MonitorObject monitorObject;

	public MonitorObject getMonitorObject() {
		return this.monitorObject;
	}

	public void setMonitorObject(final MonitorObject monitorObject) {
		this.monitorObject = monitorObject;
	}

	public MonitorWorker(final String propertiespath) {
		super(propertiespath);
	}

	@Override
	public DataEvent DoWork(final DataEvent dataEvent) throws IOException {
		ServerDataEvent event = null;
		/* if there is an error.. */
		if (dataEvent instanceof ErrorDataEvent) {
			this.resetWorkerState();
			return dataEvent;
		}
		/* obtains the command to process */
		final String command = this.obtainCommand(dataEvent);
		/* initial salutation */
		if (!this.helo) {
			return this.helo(dataEvent, command);
		}
		/* if the user is not logged , it should be */
		if (!this.logged) {
			this.loginservice = LoginService.getInstance(this.propertiespath);
			final Code code = this.loginservice.login(dataEvent, command);
			this.logged = this.loginservice.isLogged(code);
			return this.loginservice.createResponseEvent(code, dataEvent);
		}
		/* gets monitored info */
		event = this.getMonitoredInfo(dataEvent);
		return event;

	}

	private byte[] processCommand(final String command) {
		byte[] resp;
		if (command.equals("GET")) {
			resp = "get".getBytes();
		} else {
			resp = "Invalid Command\n".getBytes();
		}
		return resp;
	}

	@Override
	void resetWorkerState() {
		this.helo = false;
		this.logged = false;
		this.loginservice = null;
		LoginService.resetInstance();
	}

	@Override
	void quit() {
		// TODO Auto-generated method stub
	}

	private ServerDataEvent getMonitoredInfo(final DataEvent dataEvent) {
		int totalBytes = 0;
		int fromClientBytes = 0;
		int fromServersBytes = 0;
		int totalTrans = 0;
		int imageFlips = 0;
		int testToL33t = 0;
		synchronized (this) {
			totalBytes = this.monitorObject.getTotalTransferedBytes();
			fromClientBytes = this.monitorObject
					.getFromClientsTransferedBytes();
			fromServersBytes = this.monitorObject
					.getFromServersTransferedBytes();
			totalTrans = this.monitorObject.getTransformationsQuant();
			imageFlips = this.monitorObject.getImageFlipsQuant();
			testToL33t = this.monitorObject.getText2L33tQuant();
		}

		final String resp = "Bytes Transferred from clients:" + fromClientBytes
				+ "\n" + "Bytes Transferred from servers: " + fromServersBytes
				+ "\n" + "Total Bytes Transferred: " + totalBytes + "\n"
				+ "Image Flips Quantity: " + imageFlips + "\n"
				+ "Text to L33t Transformations Quantity: " + testToL33t + "\n"
				+ "Total Transformations Quantity: " + totalTrans + "\n";
		final ServerDataEvent event = new ServerDataEvent(
				((ServerDataEvent) dataEvent).getChannel(),
				ByteBuffer.wrap(resp.getBytes()), dataEvent.getReceiver());
		event.setCanClose(false);
		event.setCanSend(true);
		return event;
	}
}
