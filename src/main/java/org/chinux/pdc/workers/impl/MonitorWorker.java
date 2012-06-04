package org.chinux.pdc.workers.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ErrorDataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.LoginService;
import org.chinux.pdc.server.LoginService.Code;
import org.chinux.pdc.server.MonitorObject;
import org.chinux.pdc.server.User;

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
		/* gets monitored info */
		final ServerDataEvent event;
		/* if there is an error.. */
		if (dataEvent instanceof ErrorDataEvent) {
			this.resetWorkerState();
			return dataEvent;
		}
		/* obtains the command to process */
		final String command = this.obtainCommand(dataEvent);

		final InetAddress addr = ((ServerDataEvent) dataEvent).getChannel()
				.socket().getInetAddress();
		final int port = ((ServerDataEvent) dataEvent).getChannel().socket()
				.getPort();
		User currUser = new User(port, addr);
		if (this.users.contains(currUser)) {
			for (final User u : this.users) {
				if (u.equals(currUser)) {
					currUser = u;
				}
			}
		}
		/* initial salutation */
		if (!currUser.isGreeted()) {
			currUser.setGreeted(true);
			this.users.add(currUser);
			return this.helo(dataEvent, command, currUser);
		}
		/* if the user is not logged , it should be */
		if (!currUser.isLogged()) {
			this.loginservice = LoginService.getInstance(this.propertiespath);
			final Code code = this.loginservice.login(dataEvent, command,
					currUser);
			currUser.setLogged(this.loginservice.isLogged(code));
			return this.loginservice.createResponseEvent(code, dataEvent);
		}
		event = this.getMonitoredInfo(dataEvent);
		return event;

	}

	@Override
	void resetWorkerState() {
		this.loginservice = null;
		LoginService.resetInstance();
	}

	private ServerDataEvent getMonitoredInfo(final DataEvent dataEvent) {
		int totalBytes = 0;
		int fromClientBytes = 0;
		int fromServersBytes = 0;
		int totalTrans = 0;
		int imageFlips = 0;
		int testToL33t = 0;
		int totalblocks = 0;
		int ipblocks = 0;
		int urlblocks = 0;
		int allblocks = 0;
		int ctypeblocks = 0;
		int sizeblocks = 0;
		int totalconnections = 0;
		int timedconnections = 0;

		synchronized (this) {
			totalBytes = this.monitorObject.getTotalTransferedBytes();
			fromClientBytes = this.monitorObject
					.getFromClientsTransferedBytes();
			fromServersBytes = this.monitorObject
					.getFromServersTransferedBytes();
			totalTrans = this.monitorObject.getTransformationsQuant();
			imageFlips = this.monitorObject.getImageFlipsQuant();
			testToL33t = this.monitorObject.getText2L33tQuant();
			totalblocks = this.monitorObject.getTotalblocksQuant();
			ipblocks = this.monitorObject.getIpBlocksQuant();
			urlblocks = this.monitorObject.getUrlBlocksQuant();
			allblocks = this.monitorObject.getAllAccessBlocksQuant();
			ctypeblocks = this.monitorObject.getContentTypeBlocksQuant();
			sizeblocks = this.monitorObject.getTooBigResourceBlocksQuant();
			totalconnections = this.monitorObject.getConnectionsQuant();
			timedconnections = this.monitorObject.getTimedConnections();
		}

		String totalkb = "";
		if (totalBytes > 1024) {
			totalkb = " ("
					+ String.valueOf(truncate(
							Double.valueOf(totalBytes) / 1024, 2)) + " KB)";
		}

		String totalmb = "";
		if (totalBytes > (1024 * 1024)) {
			totalmb = " ("
					+ String.valueOf(truncate(Double.valueOf(totalBytes)
							/ (1024 * 1024), 2)) + " MB)";
		}

		String clientkb = "";
		if (fromClientBytes > 1024) {
			clientkb = " ("
					+ String.valueOf(truncate(
							Double.valueOf(fromClientBytes) / 1024, 2))
					+ " KB)";
		}

		String clientmb = "";
		if (fromClientBytes > (1024 * 1024)) {
			clientmb = " ("
					+ String.valueOf(truncate(Double.valueOf(fromClientBytes)
							/ (1024 * 1024), 2)) + " MB)";
		}

		String serverkb = "";
		if (fromServersBytes > 1024) {
			serverkb = " ("
					+ String.valueOf(truncate(
							Double.valueOf(fromServersBytes) / 1024, 2))
					+ " KB)";
		}

		String servermb = "";
		if (fromServersBytes > (1024 * 1024)) {
			servermb = " ("
					+ String.valueOf(truncate(Double.valueOf(fromServersBytes)
							/ (1024 * 1024), 2)) + " MB)";
		}

		final int clientsconnections = (totalconnections - timedconnections) / 2;
		final int serversconnections = (totalconnections - timedconnections) / 2;

		final String resp = "======================= PROXY MONITOR INFORMATION ===========================\n"
				+ "Bytes Transferred from clients:                               "
				+ fromClientBytes
				+ clientkb
				+ clientmb
				+ "\n"
				+ "Bytes Transferred from servers:                               "
				+ fromServersBytes
				+ serverkb
				+ servermb
				+ "\n"
				+ "Total Bytes Transferred:                                      "
				+ totalBytes
				+ totalkb
				+ totalmb
				+ "\n"
				+ "Image Flips Quantity:                                         "
				+ imageFlips
				+ "\n"
				+ "Text to L33t Transformations Quantity:                        "
				+ testToL33t
				+ "\n"
				+ "Total Transformations Quantity:                               "
				+ totalTrans
				+ "\n"
				+ "All Access Blocks Quantity:                                   "
				+ allblocks
				+ "\n"
				+ "Ip Blocks Quantity:                                           "
				+ ipblocks
				+ "\n"
				+ "Url Blocks Quantity:                                          "
				+ urlblocks
				+ "\n"
				+ "Media Types Blocks Quantity:                                  "
				+ ctypeblocks
				+ "\n"
				+ "Size Blocks Quantity:                                         "
				+ sizeblocks
				+ "\n"
				+ "Total Blocks Quantity:                                        "
				+ totalblocks
				+ "\n"
				+ "Waiting Connections Quantity:                                 "
				+ timedconnections
				+ "\n"
				+ "Total Connections Quantity:                                   "
				+ totalconnections
				+ "\n"
				+ "Clients Connections Quantity                                  "
				+ clientsconnections
				+ "\n"
				+ "Servers Connections Quantity                                  "
				+ serversconnections + "\n\n";
		final ServerDataEvent event = new ServerDataEvent(
				((ServerDataEvent) dataEvent).getChannel(),
				ByteBuffer.wrap(resp.getBytes()), dataEvent.getReceiver());
		event.setCanClose(false);
		event.setCanSend(true);
		return event;
	}

	public static double truncate(final double value, final int places) {
		final double multiplier = Math.pow(10, places);
		return Math.floor(multiplier * value) / multiplier;
	}
}
