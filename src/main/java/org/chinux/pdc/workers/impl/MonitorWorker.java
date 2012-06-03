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

	// public MonitorWorker(final String propertiespath,
	// final MonitorObject monitor) {
	// super(propertiespath);
	// this.monitor = monitor;
	// }

	public MonitorObject getMonitorObject() {
		return this.monitorObject;
	}

	public void setMonitorObject(final MonitorObject monitorObject) {
		this.monitorObject = this.monitorObject;
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
			System.out.println("===================================== HELO");
			return this.helo(dataEvent, command);
		}
		/* if the user is not logged , it should be */
		if (!this.logged) {
			this.loginservice = LoginService.getInstance(this.propertiespath);
			final Code code = this.loginservice.login(dataEvent, command);
			this.logged = this.loginservice.isLogged(code);
			return this.loginservice.createResponseEvent(code, dataEvent);
		}
		System.out.println("===================================== LOGGED");
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
		int bytesTransfered = 0;
		synchronized (this) {
			if (this.monitorObject.isNewServerEvent()) {
				bytesTransfered = this.monitorObject.getServerEvent().getData()
						.array().length;
				System.out
						.println("===================================== MONITOROBJECT");
			}
		}

		final String resp = "Bytes Transfered from client:" + bytesTransfered;
		final ServerDataEvent event = new ServerDataEvent(
				((ServerDataEvent) dataEvent).getChannel(),
				ByteBuffer.wrap(resp.getBytes()));

		return event;
	}

}
