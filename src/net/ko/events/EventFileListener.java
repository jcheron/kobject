package net.ko.events;

import java.util.EventListener;

public interface EventFileListener extends EventListener {
	void fileExist(EventFile e);
}
