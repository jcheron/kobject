package net.ko.bean;

public class KDbParams {
	protected String host = "localhost";
	protected String user = "root";
	protected String pass = "";
	protected String base;
	protected String port = "";
	protected String options = "";

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public KDbParams() {
		super();
	}

	public KDbParams(String host, String user, String pass) {
		this(host, user, pass, "", "", "");
	}

	public KDbParams(String host, String user, String pass, String base) {
		this(host, user, pass, base, "", "");
	}

	public KDbParams(String host, String user, String pass, String base, String port) {
		this(host, user, pass, base, port, "");
	}

	public KDbParams(String host, String user, String pass, String base, String port, String options) {
		super();
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.port = port;
		this.base = base;
		this.options = options;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public KDbParams clone(String newDb) {
		return new KDbParams(host, user, pass, newDb, port, options);
	}

	public void setParams(String... args) {
		if (args.length > 5)
			this.options = args[5];
		if (args.length > 4)
			this.port = args[4];
		if (args.length > 3)
			this.base = args[3];
		if (args.length > 2)
			this.pass = args[2];
		if (args.length > 1)
			this.user = args[1];
		if (args.length > 0)
			this.host = args[0];
	}
}
