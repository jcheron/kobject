package net.ko.events;

public enum KEventType {
	etLoadKlistObjectStart("onLoadKlistObjectStart"),etLoadKlistObjectEnd("onLoadKlistObjectEnd"),
	etLoadKobjectStart("onLoadKobjectStart"),etLoadKobjectEnd("onLoadKobjectEnd"),
	etUpdateKobjectStart("onUpdateKobjectStart"),etUpdateKobjectEnd("onUpdateKobjectEnd");
	private String label;
	
	private KEventType(String label){
		this.label=label;
	}
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
