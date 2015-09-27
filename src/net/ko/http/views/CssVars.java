package net.ko.http.views;

import java.util.Properties;

import net.ko.utils.KColor;
import net.ko.utils.KString;
import net.ko.utils.KStrings;

public class CssVars extends KStrings {
	public CssVars() {
		super();
	}

	public CssVars(Properties properties) {
		super(properties);
	}

	public String getRealValue(String key){
		String result=this.get(key)+"";
		String value=this.get(key)+"";
		String valueClean=KString.getBefore(value, "+");
		valueClean=KString.getBefore(valueClean, "-");
		if(this.containsKey(valueClean)){
			String originalValue=this.get(valueClean)+"";
			if(originalValue.startsWith("#")){
				int darkerValue=KColor.matchCount(value, "\\+");
				int brighterValue=KColor.matchCount(value, "\\-");
				if(darkerValue>0)
					result= KColor.colorDarker(originalValue, darkerValue);
				else if(brighterValue>0)
					result=KColor.colorBrighter(originalValue, brighterValue);
				else
					result= originalValue;
			}
		}
		return result;
	}
}
