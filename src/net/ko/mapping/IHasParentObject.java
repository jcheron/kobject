package net.ko.mapping;

public interface IHasParentObject {
	public Object getParentObject();

	public <T extends IHasParentObject> T getParentObject(Class<T> clazz);

	public void setParentObject(Object parentObject);

	public boolean addElement(IAjaxObject element);

	public boolean removeElement(IAjaxObject element);
}
