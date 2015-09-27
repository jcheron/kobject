package net.ko.utils;

import java.io.File;
import java.util.regex.Matcher;

import net.ko.creator.KClassCreator;

public class KFileUtils {
	public static String getPathName(String name) {
		String result = "";
		String rootPath = KApplication.getRootPath(KClassCreator.class);
		if (!rootPath.endsWith(".jar"))
			result = rootPath + "/" + name;
		else
			result = name;
		return result;
	}

	public static boolean isInRootFolder(String path) {
		boolean result = true;
		if (path != null) {
			path = Matcher.quoteReplacement(path);
			String aPath = path.replaceFirst("^(.+?)/(.*?)\\.do$", "$1");
			result = path.equals(aPath);
		}
		return result;
	}

	public static String getExtension(String fileName) {
		String extension = "";

		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}
		return extension;
	}

	public static String getExtension(File f) {
		return getExtension(f.getName());
	}
}
