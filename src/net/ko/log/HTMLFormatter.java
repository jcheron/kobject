package net.ko.log;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class HTMLFormatter extends Formatter {

	public HTMLFormatter() {
		
	}

	public String format(LogRecord record) {
		StringBuffer s = new StringBuffer(1000);
		Date d = new Date(record.getMillis());
		
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, Locale.FRANCE);  
		s.append("<div class='"+record.getLevel().getName()+" RECORD'><div class='date'>" + df.format(d) + "</div>");
		s.append("<div class='message'>"+formatMessage(record) +"</div>");
		if(record.getParameters()!=null)
			if(record.getParameters().length>0)
				s.append("<div class='object'>"+display(record.getParameters()[0])+"</div>");
		s.append("</div>\n\n\n\n");
		return s.toString();
	}
	
	public String getHead(Handler h) {
		return "";
	}
	
	public String getTail(Handler h) {
		return "";
	}
	public String display(Object o){
		String result="";
		if(o instanceof Exception){
			Exception e=(Exception) o;
			StackTraceElement ste= e.getStackTrace()[4];
			result=e.getClass() + ": " +  e.getMessage() + ": [" + ste.getClassName()+"/"+ste.getMethodName()+"(line "+ste.getLineNumber()+")]";
		}
		return result;
	}
}
