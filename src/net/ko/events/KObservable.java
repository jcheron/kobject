package net.ko.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class KObservable extends Observable {
	private Map<KEvents, KEventListener> events;

	public KObservable() {
		events = new HashMap<>();
		this.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				KFireEvent fireEvt = (KFireEvent) arg;
				if (events.containsKey(fireEvt.getEvent()))
					events.get(fireEvt.getEvent()).update(fireEvt);
			}
		});
	}

	public void setChanged() {
		super.setChanged();
	}

	public void addListener(Object object, KEventType evtType, KEventListener listener) {
		events.put(KEvents.getEvent(object, evtType), listener);
	}

}
