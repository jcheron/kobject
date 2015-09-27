package net.ko.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KLogger {
	private Logger logger;
	private String fileName="";
	
	public KLogger() {
		logger=Logger.getLogger("KObject");
		logger.setUseParentHandlers(false);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new ConsoleFormatter());
		ch.setLevel(Level.WARNING);
		logger.addHandler(ch);
	}
	
	public void setFileHandler(String fileName) throws SecurityException, IOException{
		this.fileName=fileName;
		FileHandler fh=new FileHandler(fileName);
		logger.addHandler(fh);
	}
	
	public void setFileHandler(String fileName,Formatter formatter) throws SecurityException, IOException{
		this.fileName=fileName;
		File f=new File(fileName);
		File fo=new File(f.getParent());
		if(!fo.exists())
			fo.mkdirs();
		FileHandler fh=new FileHandler(fileName);
		fh.setFormatter(formatter);
		logger.addHandler(fh);
	}
	
	public void log(Level level,String msg,Object param){
			logger.log(level, msg, param);
	}
	
	public void log(Level level,String msg){
		logger.log(level, msg);
	}
	
	public void setLevel(Level newLevel){
		logger.setLevel(newLevel);
	}

	public Logger getLogger() {
		return logger;
	}
	public Handler getHTMLHandler(){
		Handler result=null;
		for(Handler hndl:logger.getHandlers()){
			if(hndl.getFormatter().getClass().equals(HTMLFormatter.class)){
				result=hndl;
				break;
			}
		}
		return result;
	}

	public void clear() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write("");
			bw.flush();
			bw.close();
		} catch (IOException ioe) {
		}
	}
}
