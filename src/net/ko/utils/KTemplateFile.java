package net.ko.utils;

import java.io.File;
import java.util.regex.Matcher;

import net.ko.events.EventFileListener;


public class KTemplateFile {
	private String text;
	private String separator;
	private EventFileListener eventFileListener;
	
	public void addFileListener(EventFileListener eventFileListener){
		this.eventFileListener=eventFileListener;
	}
	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	public KTemplateFile(String text) {
		this();
		this.text = text;
	}
	public KTemplateFile() {
		super();
		separator="%";
	}
	public void open(String fileName){
		text=KTextFile.open(fileName);
	}
	public void parseWith(String target,String replacement,boolean insert){
		if (!insert)
			text=text.replaceAll("(?i)"+separator+target+separator, replacement);
		else
			text=text.replaceAll("(?i)"+separator+target+separator, separator+target+separator+replacement);
	}
	public void parseWith(String target,String replacement){
		parseWith(target, replacement, false);
	}	
	public String toString(){
		return text;
	}
	public void saveAs(String srcFolder,String kpackage,String kClassName){
		parseWith("package", kpackage);
		parseWith("className", kClassName);
		String pathSep=File.separator;
		String dir=srcFolder+pathSep+kpackage.replaceAll("\\.", Matcher.quoteReplacement(pathSep))+pathSep;
		File d=new File(dir);
		d.mkdirs();
		String fileName=dir+kClassName+".java";
		clean();
		KTextFile kt=new KTextFile(fileName, text);
		if(eventFileListener!=null)
			kt.addFileListener(eventFileListener);
		if (kt.save())
			System.out.println("Classe ["+kClassName+"] générée dans "+dir);
	}
	public void clean(){
		text=text.replaceAll(separator+".+"+separator, "");
	}
	public KTemplateFile clone(){
		KTemplateFile tpl=new KTemplateFile();
		tpl.setText(text);
		tpl.setSeparator(separator);
		return tpl;
	}
}
