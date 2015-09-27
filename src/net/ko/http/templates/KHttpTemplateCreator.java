package net.ko.http.templates;

import net.ko.utils.KProperties;

public class KHttpTemplateCreator {
	public KProperties open(String fileName){
		KProperties result=new KProperties();
		result.loadFromRessource(fileName);
		return result;
	}
	public static KProperties getProperties(String fileName){
		KHttpTemplateCreator tc=new KHttpTemplateCreator();
		return tc.open(fileName);
	}
}
