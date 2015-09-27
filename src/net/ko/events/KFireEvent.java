package net.ko.events;

public class KFireEvent {
	private KEvents event;
	private Object object;
	private Object[] params;

	public KFireEvent(KEvents event, Object object, Object[] params) {
		super();
		event.setCancel(false);
		this.event = event;
		this.object = object;
		this.params = params;
	}

	public KFireEvent(KEvents event, Object object) {
		this(event, object, null);
	}

	public KEvents getEvent() {
		return event;
	}

	public void setEvent(KEvents event) {
		this.event = event;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

}
