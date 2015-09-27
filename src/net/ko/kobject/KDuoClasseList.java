package net.ko.kobject;

import java.util.ArrayList;

public class KDuoClasseList {
	private ArrayList<KDuoClasse> duoClasses;
	
	public KDuoClasseList() {
		super();
		duoClasses=new ArrayList<KDuoClasse>();
	}
	public KDuoClasseList(ArrayList<KDuoClasse> duoClasses) {
		super();
		this.duoClasses = duoClasses;
	}
	public boolean add(Class<? extends KObject> clazz,Class<? extends KObject> relClazz){
		return add(new KDuoClasse(clazz, relClazz));
	}
	public boolean add(KDuoClasse aDuo){
		boolean exist=exists(aDuo);
		if(!exist)
			duoClasses.add(aDuo);
		return exist;
	}
	public boolean exists(KDuoClasse aDuo){
		boolean trouve=false;
		int i=0;
		while(i<duoClasses.size() && !trouve){
			if(duoClasses.get(i).equals(aDuo))
				trouve=true;
			i++;
		}
		return trouve;
	}
	@SuppressWarnings("unchecked")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new KDuoClasseList((ArrayList<KDuoClasse>) duoClasses.clone());
	}
	public int size(){
		return duoClasses.size();
	}
	public void setDuoClasses(ArrayList<KDuoClasse> duoClasses) {
		this.duoClasses = duoClasses;
	}
	public ArrayList<KDuoClasse> getDuoClasses() {
		return duoClasses;
	}
}
