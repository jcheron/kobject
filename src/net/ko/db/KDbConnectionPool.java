package net.ko.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;

public abstract class KDbConnectionPool {
	private ArrayList<KDataBase> connections;
	final private long timeout = 60000;
	private KDataBaseConnectionReaper reaper;
	private int maxConnectionCount = 1;

	public KDbConnectionPool() {
		connections = new ArrayList<KDataBase>();
		reaper = new KDataBaseConnectionReaper(this);
		reaper.start();
	}

	protected abstract KDataBase createDbPoolInstance(boolean connect);

	public KDataBase kconnection() throws InstantiationException, IllegalAccessException {
		return getConnection(true);
	}

	public KDataBase kconnection(boolean connect) throws InstantiationException, IllegalAccessException {
		return getConnection(connect);
	}

	public void add(KDataBase connection) {
		connections.add(connection);
		connection.setPool(this);
	}

	public synchronized KDataBase getConnection(boolean connect) throws InstantiationException, IllegalAccessException {
		KDataBase result = null;
		ArrayList<KDataBase> toRemove = new ArrayList<>();
		for (KDataBase db : connections) {
			if (db.lease() || connections.size() == maxConnectionCount) {
				if (db.isValid())
					return db;
				else
					toRemove.add(db);
			}
		}
		for (KDataBase db : toRemove) {
			remove(db);
		}
		result = createDbPoolInstance(connect);
		if (result != null) {
			result.lease();
			add(result);
		}
		KDebugConsole.print("nb connections: " + connections.size(), "POOL", "KDataBase.getConnection");
		return result;
	}

	public synchronized KDataBase getNewConnection(boolean connect) {
		KDataBase result = null;
		result = createDbPoolInstance(connect);
		if (result != null) {
			result.lease();
			add(result);
		}
		return result;
	}

	public void remove(KDataBase db) {
		try {
			db.close();
		} catch (SQLException e) {
			Ko.klogger().log(Level.SEVERE, "Impossible de fermer la connexion à la base de données", e);
		}
		connections.remove(db);
	}

	public synchronized void returnConnection(KDataBase db) {
		db.expireLease();
	}

	public synchronized void reapConnections() {
		long stale = System.currentTimeMillis() - timeout;
		ArrayList<KDataBase> toRemove = new ArrayList<KDataBase>();
		if (connections != null) {
			for (KDataBase db : connections) {
				if (db.inUse() && stale > db.getLastUse() && !KDataBase.isValid(db)) {
					KDebugConsole.print("remove " + db, "POOL", "KDbConnectionPool.reapConnections");
					toRemove.add(db);
				}
			}
			for (KDataBase db : toRemove)
				remove(db);
		}
	}

	public synchronized void removeAll() {
		ArrayList<KDataBase> toRemove = new ArrayList<KDataBase>();
		for (KDataBase db : connections)
			toRemove.add(db);
		for (KDataBase db : toRemove) {
			try {
				db.close();
				db.clear();
			} catch (SQLException e) {
			}
		}
	}

	public int getMaxConnectionCount() {
		return maxConnectionCount;
	}

	public void setMaxConnectionCount(int maxConnectionCount) {
		this.maxConnectionCount = maxConnectionCount;
	}
}
