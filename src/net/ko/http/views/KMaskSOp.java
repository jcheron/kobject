package net.ko.http.views;

import javax.servlet.http.HttpServletRequest;

import net.ko.kobject.KMask;
import net.ko.utils.KString;

public class KMaskSOp extends KMask {

	public KMaskSOp() {
		super();
	}

	public KMaskSOp(String mask) {
		super(mask);
	}

	public KMaskSOp(String[] masks) {
		super(masks);
	}
	
	public String getSpecificOperation(String operation,HttpServletRequest request){
		String result=getOperation(operation, "{#", "#}",true);
		if(result!=null&&!"".equals(result)){
			result=KTemplateParser.setQueryString(result, request);
			result=KString.cleanJSONValue(result);
		}
		return result;
	}
}
