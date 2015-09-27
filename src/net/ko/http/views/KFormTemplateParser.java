package net.ko.http.views;

import java.util.Map;

import net.ko.kobject.KObject;
import net.ko.utils.KStrings;

public class KFormTemplateParser extends KTemplateParser {
	
	protected KObject ko;
	public KFormTemplateParser(KObject ko, String strTemplate) {
		super(strTemplate);
		this.ko=ko;
	}

	protected KFieldControl getFieldControl(String fieldName,Map<String,Object> values) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
		KFieldControl fc=getFieldControl(fieldName);
		fc.setAttributes(values);
		return fc;
	}
	public String getValue(String jsonFieldName){
		String result="";
		KStrings strings=new KStrings(jsonFieldName);
		if(strings.containsKey("0")){
			String field=(String) strings.get("0");
			KFieldControl fc=getFieldControl(field);
			if(fc!=null)
				result=fc+"";
		}else if(strings.containsKey("name")){
			try {
				KFieldControl fc=getFieldControl((String)strings.get("name"), strings.getStrings());
				if(fc!=null)
					result=fc+"";
			} catch (Exception e) {
				result="";
			}
		}
		return result;
	}
}
