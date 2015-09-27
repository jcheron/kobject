package net.ko.thread;

public class KQueryThreadParams {
	private int maxInterval;
	private int minInterval;
	private int queriesCount;
	private boolean start;

	public KQueryThreadParams(int maxInterval, int minInterval, int queriesCount, boolean start) {
		this.maxInterval = maxInterval;
		this.minInterval = minInterval;
		this.queriesCount = queriesCount;
		this.start = start;
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

	public int getQueriesCount() {
		return queriesCount;
	}

	public void setQueriesCount(int queriesCount) {
		this.queriesCount = queriesCount;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

}
