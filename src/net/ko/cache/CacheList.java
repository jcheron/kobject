package net.ko.cache;

import java.io.Serializable;
import java.util.ArrayList;

import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.utils.KHash;


public class CacheList implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4842975312272553856L;
	private ArrayList<String> items;
	private boolean complete=true;
	private Class<? extends KObject> clazz;
	
	private void setList(KListObject<? extends KObject> kl,KCache kcache){
		items.clear();
		for(KObject ko:kl){
			String key=ko.getUniqueId();
			key=KHash.getMD5(key);
			items.add(key);
			//kcache.put(ko);
		}
	}
	public void add(String item){
		items.add(item);
	}
	public CacheList(Class<? extends KObject> clazz) {
		super();
		items=new ArrayList<String>();
		this.clazz=clazz;
	}
	public CacheList(KListObject<? extends KObject> kl,KCache kcache){
		this(kl.getClazz());
		this.setList(kl,kcache);
	}
	public KListObject<? extends KObject> getList(KCache kcache){
		KListObject<? extends KObject> kl=new KListObject(clazz);
		for(String key:items){
			try{
				//KObject ko=clazz.cast(kcache.getObjectValue(key)) ;
				//kl.add(ko);
				}
			catch(Exception e){
				complete=false;
			}
		}
		return kl;
	}
	public boolean isComplete() {
		return complete;
	}
	public Class<? extends KObject> getClazz() {
		return clazz;
	}
	
}
