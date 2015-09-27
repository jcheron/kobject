/**
 * Classe KCache
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: $
 * @package ko.kobject
 */
package net.ko.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


import net.ko.framework.KDebugConsole;
import net.ko.framework.Ko;
import net.ko.kobject.KListObject;
import net.ko.kobject.KObject;
import net.ko.kobject.KRecordStatus;
import net.ko.utils.KHash;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

public class KCache {
	private static KCache instance;
	private CacheManager manager;
	
	private KListObject<? extends KObject> getList(Object o){
		CacheList lst;
		KListObject<? extends KObject> kl=null;
		if (o instanceof CacheList){
			lst=(CacheList)o;
			kl=lst.getList(this);}
		return kl;
	}
	
	private void mkCacheManager(){
		manager = CacheManager.create();
	}
	
	private KCache() {
		mkCacheManager();
	}
	private static Cache getDefaultCache(){
		Cache cache=null;
		CacheManager manager=getInstance().getManager();
		if (!manager.cacheExists("kocache")){
////			cache = new Cache(
////				     new CacheConfiguration("kocache", 1000)
////				       .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.FIFO)
////				       .overflowToDisk(true)
////				       .eternal(false)
////				       .timeToLiveSeconds(240)
////				       .timeToIdleSeconds(240)
////				       .diskPersistent(false)
////				       .diskExpiryThreadIntervalSeconds(240));
//			cache=manager.;
			manager.addCache("kocache");
			cache=manager.getCache("kocache");
		}
		else
			cache=manager.getCache("kocache");
		return cache;
	}

	public static CacheConfiguration getConfiguration(String cacheName){
		return KCache.getCache(cacheName).getCacheConfiguration();
	}
	
	public static CacheConfiguration getConfiguration(){
		return KCache.getConfiguration("");
	}
	
	public static void setTimeToIdleSeconds(int timeToIdleSeconds,String cacheName){
		KCache.getConfiguration(cacheName).setTimeToIdleSeconds(timeToIdleSeconds);
	}
	
	public static void setTimeToIdleSeconds(int timeToIdleSeconds){
		KCache.setTimeToIdleSeconds(timeToIdleSeconds,"");
	}
	
	public static void setTimeToLiveSeconds(int timeToLiveSeconds){
		KCache.getConfiguration().setTimeToLiveSeconds(timeToLiveSeconds);
	}
	
	public static void setMaxElementsInMemory(int maxElementsInMemory){
		KCache.getConfiguration().setMaxElementsInMemory(maxElementsInMemory);
	}
	
	public static void setMaxElementsOnDisk(int maxElementsOnDisk){
		KCache.getConfiguration().setMaxElementsOnDisk(maxElementsOnDisk);
	}
	
	 public static KCache getInstance() {
	        if (null == instance) {
	            instance = new KCache();
	        }
	        return instance;
	    }
	 
	public CacheManager getManager() {
		if(manager==null)
			mkCacheManager();
		return manager;
	}
	
	public CacheManager getManager(String fileName) {
		if (fileName.equals("")){
			mkCacheManager();
		}
		else
			manager = CacheManager.create(fileName);
		return manager;
	}
	
	public static CacheManager getCacheManager(String fileName){
		return getInstance().getManager(fileName);
	}
	
	public static CacheManager getCacheManager(){
		return getInstance().getManager();
	}
	public static Cache getCache() {
		return getDefaultCache();
	}
	
	public static Cache getCache (String cacheName){
		CacheManager manager=getCacheManager();
		if (manager.cacheExists(cacheName))
			return manager.getCache(cacheName);
		else
			return getDefaultCache();
			
	}
	public static void loadAllCache(){
		CacheManager manager=getCacheManager();
		String[] names=manager.getCacheNames();
		for (int i = 0; i < names.length; i++)
			KCache.getCache(names[i]);
		KCache.getCache();
	}
	
	private static void put(String key,Object o,Cache cache){
		Element element=new Element(KHash.getMD5(key), o);
		cache.put(element);
		if (!System.getProperty("java.vm.info", "").contains("sharing"))
			KDebugConsole.print("put:"+o.getClass()+":"+KHash.getMD5(key), "CACHE","KCache.put");
	}
	private static void replace(Element element,Cache cache){
		cache.replace(element);
	}
	
	public static void replace(KObject ko,boolean fromList){
		//if (!fromList)
			opOnKObjectInKL(ko, ko.getRecordStatus());
		
		if(Ko.cacheType.equals(KCacheType.ktListAndObjects)){
			Cache cache=getCache(ko.getClass().getSimpleName());
			String key=ko.getUniqueId();
			Element element=new Element(KHash.getMD5(key), ko);
			replace(element, cache);
		}
	}	
	
	public static void replace(KObject ko){
		replace(ko,false);
	}
	
	public static void put(KObject ko){
		Cache cache=getCache(ko.getClass().getSimpleName());
		String key=ko.getUniqueId();
		put(key, ko,cache);
	}
	private static String getKlKey(KListObject<? extends KObject> kl){
		return kl.getSql().toLowerCase();
	}
	public static void replace(KListObject<? extends KObject> kl){
		Cache cache=getCache(kl.getClazz().getSimpleName());
		String key=getKlKey(kl);
		Element element=new Element(KHash.getMD5(key), kl);
		replace(element, cache);
		if (!System.getProperty("java.vm.info", "").contains("sharing"))
			KDebugConsole.print("replaceKL:"+key, "CACHE","KCache.replace");
	}
	
	public static void remove(KListObject<? extends KObject> kl){
		Cache cache=getCache(kl.getClazz().getSimpleName());
		String key=getKlKey(kl);
		cache.remove(KHash.getMD5(key));
		if (!System.getProperty("java.vm.info", "").contains("sharing"))
			KDebugConsole.print("removeKL:"+key, "CACHE","KCache.remove");
	}
	
	public static void removeKLFrom(KObject ko){
		ArrayList<Object> al=getAll(ko.getClass().getSimpleName());
		String criteria="";
		try {
			criteria = ko.makeCriteria();
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur sur makeCriteria : "+ko, e);
		}
		for(Object l:al){
			if(l instanceof KListObject){
				KListObject kl=((KListObject) l);
				if(kl.getClazz().equals(ko.getClass())){
					int i=kl.indexByCriteria(criteria);
					if (i!=-1){
						remove(kl);
					}
				}
			}
		}
	}
	
	public static void removeKLFrom(Class clazz){
		ArrayList<Object> al=getAll(clazz.getSimpleName());
		for(Object l:al){
			if(l instanceof KListObject){
				KListObject kl=((KListObject) l);
				if(kl.getClazz().equals(clazz))
					remove(kl);
			}
		}
	}
	public static void put(KListObject<? extends KObject> kl){
		Cache cache=getCache(kl.getClazz().getSimpleName());
		put(getKlKey(kl),kl,cache);
	}
	public static Object getObjectValue(String key,Cache cache){
		if (!System.getProperty("java.vm.info", "").contains("sharing"))
			KDebugConsole.print("get:"+key, "CACHE","KCache.getObjectValue");
		Element element = cache.get(key);
		Object o=null;
		if (element!=null){
			o=element.getObjectValue();
			//if (o instanceof CacheList)
				//return getList(o);
		}
		if (!System.getProperty("java.vm.info", "").contains("sharing"))
			KDebugConsole.print(element+"", "CACHE","KCache.getObjectValue");
		return o;
	}
	public static Object getObjectValue(KObject ko){
		Cache cache=getCache(ko.getClass().getSimpleName());
		String key=ko.getUniqueId();
		Object o=getObjectValue(KHash.getMD5(key),cache);
		if(o!=null){
			KDebugConsole.print(o+"", "CACHE","KCache.getObjectValue");
			return o;
		}
		else{
			return getObjectValueInKL(ko);
		}
	}
	public static Object getObjectValue(KListObject<? extends KObject> kl){
		Cache cache=getCache(kl.getClazz().getSimpleName());
		String key=getKlKey(kl);
		return getObjectValue(KHash.getMD5(key),cache);
	}
	public static Object getObjectValueInKL(KObject ko){
		Object result=null;
		ArrayList<Object> al=getAll(ko.getClass().getSimpleName());
		String criteria="";
		try {
			criteria = ko.makeCriteria();
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur sur makeCriteria : "+ko, e);
		}
		for(Object l:al){
			if(l instanceof KListObject){
				KListObject<? extends KObject> kl=((KListObject<? extends KObject>) l);
				if(kl.getClazz().equals(ko.getClass())){
					int i=kl.indexByCriteria(criteria);
					if (i!=-1){
						result=kl.get(i);
						KDebugConsole.print("index:"+i, "CACHE","KCache.getObjectValueInKL");
						return kl.get(i);
					}
				}else
					Ko.klogger().log(Level.WARNING, "La classe de la liste : "+kl.getClazz()+" ne correspond pas à la classe de l'objet : "+ko.getClass());
			}
		}
		return result;
	}
	
	public static void opOnKObjectInKL(KObject ko,KRecordStatus rs){
		ArrayList<Object> al=getAll(ko.getClass().getSimpleName());
		String criteria="";
		try {
			criteria = ko.makeCriteria();
		} catch (Exception e) {
			Ko.klogger().log(Level.WARNING, "Erreur sur makeCriteria : "+ko, e);
		}
		for(Object l:al){
			if(l instanceof KListObject){
				KListObject kl=((KListObject) l);
				if(kl.getClazz().equals(ko.getClass())){
					int i=kl.indexByCriteria(criteria);
					if (i!=-1){
						if(rs==KRecordStatus.rsUpdate)
							kl.set(i, ko);
						if(rs==KRecordStatus.rsDelete)
							kl.delete(i);
						KCache.replace(kl);
					}
					if(rs==KRecordStatus.rsNew){
						kl.add(ko, true);
						KCache.replace(kl);
					}
				}else
					Ko.klogger().log(Level.WARNING, "La classe de la liste : "+kl.getClazz()+" ne correspond pas à la classe de l'objet : "+ko.getClass());
			}
		}
	}

	
	public static void replaceKObjectInKL(KObject ko){
		opOnKObjectInKL(ko, KRecordStatus.rsUpdate);
	}
	
	public static void deleteKObjectInKL(KObject ko){
		opOnKObjectInKL(ko, KRecordStatus.rsDelete);
	}
	
	public static void addKObjectInKL(KObject ko){
		opOnKObjectInKL(ko, KRecordStatus.rsNew);
	}
	
	public static ArrayList<Object> getAll(){
		ArrayList<Object> al=new ArrayList<Object>();
		String names[]=getCacheManager().getCacheNames();
		for (int i = 0; i < names.length; i++) {
			if(names[i]!=null)
				if(!names[i].equals(""))
					al.addAll(getAll(names[i]));
		}
		return al;
	}
	
	public static ArrayList<Object> getAll(String cacheName){
		Cache cache=getCache(cacheName);
		ArrayList<Object> al=new ArrayList<Object>();
		List<String> keys=cache.getKeys();
		for(String k:keys){
			al.add(getObjectValue(k,cache));
		}
		return al;
	}
	
	public static void save(){
		save("");
	}
	
	public static void save(String cacheName){
		getCache(cacheName).flush();
	}
	
	public static void shutdown(){
		getCacheManager().shutdown();
	}
}
