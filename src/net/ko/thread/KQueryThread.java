package net.ko.thread;

import java.util.logging.Level;

import net.ko.framework.Ko;
import net.ko.persistence.GenericDAOEngine;

public class KQueryThread extends Thread {
	private GenericDAOEngine engine;
	private boolean suspended;
	private int maxInterval;
	private int minInterval;
	private int interval;
	private int queriesCount;

	public KQueryThread(GenericDAOEngine engine) {
		this.engine = engine;
		this.suspended = false;
		maxInterval = 120;
		minInterval = 30;
		queriesCount = 2;
	}

	@Override
	public void run() {
		while (isAlive() && !suspended) {
			if (engine != null) {
				Ko.klogger().log(Level.SEVERE, "Saving queries (max : " + queriesCount + " )");
				engine.saveDataTo(Ko.kmainDAOEngine(), queriesCount);
				setInterval();
			}
			try {
				Thread.sleep(interval * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void setInterval() {
		if (engine.getQueries().size() == 0)
			interval = maxInterval;
		else
			interval = minInterval;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public int getMaxInterval() {
		return maxInterval;
	}

	public void setMaxInterval(int maxInterval) {
		this.maxInterval = maxInterval;
	}

	public int getMinInterval() {
		return minInterval;
	}

	public void setMinInterval(int minInterval) {
		this.minInterval = minInterval;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getQueriesCount() {
		return queriesCount;
	}

	public void setQueriesCount(int queriesCount) {
		this.queriesCount = queriesCount;
	}

	public void setParams(KQueryThreadParams params) {
		this.maxInterval = params.getMaxInterval();
		this.minInterval = params.getMinInterval();
		this.queriesCount = params.getQueriesCount();
		this.suspended = !params.isStart();
	}
}
