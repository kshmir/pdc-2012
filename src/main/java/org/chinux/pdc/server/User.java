package org.chinux.pdc.server;

import java.net.InetAddress;

public class User {

	private String username;
	private String password;
	private int srcport;
	private InetAddress host;
	private boolean logged;

	public User(final String username, final String password,
			final int srcport, final InetAddress host) {
		super();
		this.username = username;
		this.password = password;
		this.srcport = srcport;
		this.host = host;
		this.logged = false;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public int getSrcport() {
		return this.srcport;
	}

	public void setSrcport(final int srcport) {
		this.srcport = srcport;
	}

	public InetAddress getHost() {
		return this.host;
	}

	public void setHost(final InetAddress host) {
		this.host = host;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.host == null) ? 0 : this.host.hashCode());
		result = prime * result
				+ ((this.password == null) ? 0 : this.password.hashCode());
		result = prime * result + this.srcport;
		result = prime * result
				+ ((this.username == null) ? 0 : this.username.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final User other = (User) obj;
		if (this.host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!this.host.equals(other.host)) {
			return false;
		}
		if (this.password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!this.password.equals(other.password)) {
			return false;
		}
		if (this.srcport != other.srcport) {
			return false;
		}
		if (this.username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!this.username.equals(other.username)) {
			return false;
		}
		return true;
	}

	public boolean isLogged() {
		return this.logged;
	}

	public void setLogged(final boolean logged) {
		this.logged = logged;
	}

}
