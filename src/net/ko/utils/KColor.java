package net.ko.utils;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KColor {
	public static String getColor(String hexColorCode){
		String result=hexColorCode;
		int nbPlus=matchCount(hexColorCode, "\\+");
		int nbMoins=matchCount(hexColorCode, "\\-");
		hexColorCode=KString.getBefore(hexColorCode, "+");
		hexColorCode=KString.getBefore(hexColorCode, "-");
		if(nbPlus>0)
			result=colorDarker(hexColorCode, nbPlus);
		else if(nbMoins>0)
			result=colorBrighter(hexColorCode, nbMoins);
		return "#"+result.replace("#", "");
	}
	
	public static final int matchCount(String text, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(text);
		int result = 0;
		while(matcher.find()) {
			result ++;
		}
		if(result==1){
			int val=0;
			String nb=KString.getAfter(text, regex.replace("\\", ""));
			try{
				val=Integer.parseInt(nb);
				result=val;
			}catch(NumberFormatException e){
				
			}
		}
		return result;
	}
	public static Color hexToColor(String hexColor){
		return Color.decode(hexColor);
	}
	public static String colorToHex(Color color){
		String result=Integer.toHexString(color.getRGB());
		result="#"+result.substring(2, result.length());
		return result;
	}
	public static Color colorDarker(Color color){
		return color.darker();
	}
	public static Color colorBrighter(Color color){
		return color.brighter();
	}
	public static String colorDarker(String hexColor){
		return colorToHex(hexToColor(hexColor).darker());
	}
	public static String colorBrighter(String hexColor){
		return colorToHex(hexToColor(hexColor).brighter());
	}
	public static String colorDarker(String hexColor,int count){
		Color originalColor=hexToColor(hexColor);
		float hsbVals[] = Color.RGBtoHSB( originalColor.getRed(),originalColor.getGreen(),originalColor.getBlue(), null );
		float h=getCorrectValue(hsbVals[2]-0.01f*(float)count);
		return colorToHex(Color.getHSBColor(hsbVals[0], hsbVals[1], h));
	}
	public static String colorBrighter(String hexColor,int count){
		Color originalColor=hexToColor(hexColor);
		float hsbVals[] = Color.RGBtoHSB( originalColor.getRed(),originalColor.getGreen(),originalColor.getBlue(), null );
		float h=getCorrectValue(hsbVals[2]+0.01f*(float)count);
		return colorToHex(Color.getHSBColor(hsbVals[0], hsbVals[1], h));
	}
	public static float getCorrectValue(float original){
		float result=original;
		if(original<0f)
			result=0f;
		else if(original>1f)
			result=1f;
		return result;
	}
}
