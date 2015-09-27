package net.ko.kobject;

public class KDuoClasse {
	private Class<? extends KObject> clazz;
	private Class<? extends KObject> relClazz;
	public KDuoClasse(Class<? extends KObject> clazz,
			Class<? extends KObject> relClazz) {
		super();
		this.clazz = clazz;
		this.relClazz = relClazz;
	}
	public Class<? extends KObject> getClazz() {
		return clazz;
	}
	public void setClazz(Class<? extends KObject> clazz) {
		this.clazz = clazz;
	}
	public Class<? extends KObject> getRelClazz() {
		return relClazz;
	}
	public void setRelClazz(Class<? extends KObject> relClazz) {
		this.relClazz = relClazz;
	}
	@Override
	public boolean equals(Object obj) {
		KDuoClasse kd=(KDuoClasse)obj;
		return (kd.getClazz()==clazz & kd.getRelClazz()==relClazz);
	}
}
