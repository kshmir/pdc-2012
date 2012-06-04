package org.chinux.pdc.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.chinux.pdc.nio.events.api.DataEvent;
import org.chinux.pdc.nio.events.impl.ServerDataEvent;

public class LoginService {

	public enum Code {
		BADLOGIN, OTHERTERMINALOPEN, OKLOGIN, GETPASSWORD
	}

	Map<String, User> usersByUserName;

	private static LoginService instance = null;

	public static LoginService getInstance(final String propertiespath) {
		if (instance == null) {
			instance = new LoginService(propertiespath);
		}
		return instance;
	}

	public static void resetInstance() {
		instance = null;
	}

	LoginService(final String propertiespath) {
		this.usersByUserName = new HashMap<String, User>();

		final Properties props = new Properties();
		try {
			final FileInputStream fis = new FileInputStream(propertiespath);
			props.load(fis);
			fis.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final String users[] = props.getProperty("user").split(",");
		final String passwords[] = props.getProperty("password").split(",");
		for (int i = 0; i < users.length && i < passwords.length; i++) {
			this.usersByUserName.put(users[i], new User(users[i], passwords[i],
					-1, null));
		}

	}

	public Code login(final DataEvent dataEvent, final String command,
			final User currUser) {
		Code resp;
		if (currUser.getUsername() != null) {
			final String password = command.trim();

			if (!this.usersByUserName.get(currUser.getUsername()).getPassword()
					.equals(password)) {
				/* the password for the user is not correct */
				resp = Code.BADLOGIN;
			} /*
			 * else if (this.usersByUserName.get(this.currentUserName)
			 * .isLogged()) { resp = Code.OTHERTERMINALOPEN; }
			 */else {
				this.usersByUserName.get(currUser.getUsername())
						.setLogged(true);
				currUser.setLogged(true);
				resp = Code.OKLOGIN;
			}
		} else {
			final String username = command.trim();
			if (!this.usersByUserName.containsKey(username)) {
				/* none existent username */
				return Code.BADLOGIN;
			}
			currUser.setUsername(username);
			resp = Code.GETPASSWORD;
		}
		return resp;
	}

	public DataEvent createResponseEvent(final Code code,
			final DataEvent dataEvent) {
		byte[] resp = null;
		switch (code) {
		case BADLOGIN:
			resp = "Login Incorrect\nEnter user name:".getBytes();
			break;
		case OTHERTERMINALOPEN:
			resp = "Login Incorrect - The user is yet logged in another terminal\n"
					.getBytes();
			break;
		case OKLOGIN:
			resp = "250 Login OK\n".getBytes();
			break;
		case GETPASSWORD:
			resp = "Enter password: ".getBytes();
			break;
		default:
			resp = null;
		}
		final ServerDataEvent event;
		event = new ServerDataEvent(((ServerDataEvent) dataEvent).getChannel(),
				ByteBuffer.wrap(resp), dataEvent.getReceiver());
		event.setCanClose(false);
		event.setCanSend(true);
		return event;
	}

	public boolean isLogged(final Code code) {
		switch (code) {
		case OKLOGIN:
			return true;
		default:
			return false;
		}
	}

	public boolean isLogged(final String currentUser) {
		if (!this.usersByUserName.containsKey(currentUser)) {
			return false;
		}
		return this.usersByUserName.get(currentUser).isLogged();
	}
}
