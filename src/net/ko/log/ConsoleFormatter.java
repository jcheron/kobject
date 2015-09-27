package net.ko.log;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class ConsoleFormatter extends SimpleFormatter {

	public ConsoleFormatter() {
		super();
	}

	@Override
	public synchronized String format(LogRecord record) {
		String s= super.format(record);
		if(record.getParameters()!=null)
			if(record.getParameters().length>0)
				s+=display(record.getParameters()[0]);
		return s;
	}
	public String display(Object o){
		String result="";
		if(o instanceof Exception){
			Exception e=(Exception) o;
			StackTraceElement ste= e.getStackTrace()[4];
			result=e.getClass() + ": " +  e.getMessage() + ": [" + ste.getClassName()+"/"+ste.getMethodName()+"(line "+ste.getLineNumber()+")]\n";
		}
		return result;
	}
}
