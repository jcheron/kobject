package net.ko.events;

import java.util.EventObject;

public class EventFile extends EventObject {
	public boolean doit;
	public EventFile(Object source,Boolean doit) {
		super(source);
		this.doit=doit;
	}

}
