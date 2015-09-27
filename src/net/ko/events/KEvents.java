package net.ko.events;

import java.util.Hashtable;

public class KEvents {
	private Object object;
	private KEventType evtType;
	private boolean cancel;
	private static Hashtable<String, KEvents> events = new Hashtable<>();

	public KEvents(Object object, KEventType evtType) {
		super();
		this.cancel = false;
		this.object = object;
		this.evtType = evtType;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public KEventType getEvtType() {
		return evtType;
	}

	public void setEvtType(KEventType evtType) {
		this.evtType = evtType;
	}

	public static KEvents getEvent(Object o, KEventType evtType) {
		String key = o + evtType.getLabel();
		KEvents evt = null;
		if (events.containsKey(key))
			evt = events.get(key);
		else {
			evt = new KEvents(o, evtType);
			events.put(key, evt);
		}
		return evt;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof KEvents)
			result = object.equals(((KEvents) obj).getObject()) && evtType.equals(((KEvents) obj).getEvtType());
		return result;
	}

	@Override
	public int hashCode() {
		return (object + evtType.getLabel()).hashCode();
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

}
