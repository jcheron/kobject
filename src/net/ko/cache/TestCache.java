package net.ko.cache;

import net.ko.framework.Ko;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
//import tests.classes.KUtilisateur;

public class TestCache {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		KCache cache=KCache.getInstance();
		Ko.useCache=true;
		//KUtilisateur jc=new KUtilisateur();
		//jc.setId(17);
		//System.out.println(cache.getObjectValue(jc));
		
		//jc=(KUtilisateur)cache.getObjectValue(jc);
		//System.out.println(jc);
		//KListObject<KUtilisateur> users=new KListObject<KUtilisateur>(KUtilisateur.class);
		//System.out.println(cache.getObjectValue(users));
		KCache.shutdown();
		//cache.save();

	}

}
