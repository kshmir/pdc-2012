package org.chinux.pdc.workers.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;
import org.chinux.pdc.server.LoginService;
import org.chinux.pdc.server.User;
import org.chinux.pdc.workers.api.Worker;

public abstract class LogueableWorker implements Worker<DataEvent> {

	String data;
	protected List<User> users;
	LoginService loginservice;

	public LogueableWorker() {
		this.users = new ArrayList<User>();
	}

	abstract void resetWorkerState();

	String obtainCommand(final DataEvent dataEvent) {
		if (new String(dataEvent.getData().array()).split("\n").length != 0) {
			this.data = new String(dataEvent.getData().array()).split("\n")[0];
		} else {
			this.data = "";
		}
		final String command = this.data.trim();
		return command;
	}

	DataEvent helo(final DataEvent dataEvent, final String command,
			final User currUser) {
		ServerDataEvent event;
		byte[] resp;
		if (command.compareTo("HELO") != 0) {
			resp = "".getBytes();
		} else {
			currUser.setGreeted(true);
			resp = "250 Hello user, I am glad to meet you\nEnter user name: "
					.getBytes();
		}
		event = new ServerDataEvent(((ServerDataEvent) dataEvent).getChannel(),
				ByteBuffer.wrap(resp), dataEvent.getReceiver());
		event.setCanClose(false);
		event.setCanSend(true);
		return event;
	}

}
