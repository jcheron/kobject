package net.ko.http.objects;


import javax.servlet.http.HttpServletRequest;

import net.ko.types.KStringsType;
import net.ko.utils.KStrings;
import net.ko.utils.KStrings.KGlueMode;

public class KQueryString {
	private KStrings strings;
	public KQueryString(String queryString){
		if(queryString==null)
			queryString="";
		if (queryString.startsWith("?"))
			queryString=queryString.substring(1);
		strings=new KStrings(queryString, KStringsType.kstQueryString);
	}
	public String addValue(String key,String value){
		return setValue(key, value);
	}
	public String addValueFromRequest(String key,HttpServletRequest request){
		String result="";
		String value=KRequest.GETPOST(key, request);
		if(value!=null&!"".equals(value))
			result=addValue(key, value);
		return result;
	}
	public String setValue(String key,String value){
		strings.getStrings().put(key, value);
		return toString();
	}
	public void deleteKey(String key){
		strings.getStrings().remove(key);
	}
	public String toString(){
		return strings.implode_param("&", "", "=", KGlueMode.KEY_AND_VALUE, false);
	}
	public static String setValue(String queryString,String key,String value){
		return new KQueryString(queryString).setValue(key, value);
	}
	public static String removeKey(String queryString,String key){
		KQueryString qs=new KQueryString(queryString);
		qs.deleteKey(key);
		return qs.toString();
	}
}
