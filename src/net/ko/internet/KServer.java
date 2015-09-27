package net.ko.internet;

public class KServer {
	protected String host;
	protected String userName;
	protected String password;
	protected boolean authentification;

	public KServer() {
		authentification = true;
	}

	public KServer(String host, String userName, String password) {
		this();
		this.host = host;
		authentification = true;
		this.userName = userName;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAuthentification() {
		return authentification;
	}

	public void setAuthentification(boolean authentification) {
		this.authentification = authentification;
	}

}
