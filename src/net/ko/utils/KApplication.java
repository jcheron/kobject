package net.ko.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.ko.creator.KClassCreator;


public class KApplication {
	public static String getPathName(String name){
		String result="";
		String rootPath=KApplication.getRootPath(KClassCreator.class);
		if(!rootPath.endsWith(".jar"))
			result=rootPath+"/"+name;
		else
			result=name;
		return result;
	}	
	public static String getRootPath(Class<? extends Object> clazz) {
		String rootPath;
			
		try {
			rootPath = 
	URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
				
			String osName = System.getProperty("os.name");
				
			if (osName.toUpperCase().contains("WINDOWS")) {
				rootPath = rootPath.substring(1);
			}
				
			//rootPath = new File(rootPath).getParent();
				
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

			// If an error occurred, default to current directory.
			rootPath = System.getProperty("user.dir");
		}

		return rootPath;
	}
	public static String getPackageFolder(String kpackage){
		return kpackage.replace(".", "/");
	}
}
