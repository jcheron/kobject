package net.ko.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import net.ko.converters.DateFormatter;

public class KDateUtils {
	private Calendar cal;
	
	public KDateUtils() {
		cal=new GregorianCalendar(Locale.FRANCE);
	}
	public KDateUtils(Date d) {
		this();
		cal.setTime(d);
	}
	public static Calendar getCalendar(Date d){
		return new KDateUtils(d).getCal();
	}
	public static String getStrDayOfWeek(Date d){
		SimpleDateFormat formater = new SimpleDateFormat("E",Locale.FRENCH);
		return formater.format(d);
	}
	public static int getDayOfWeek(Date d){
		return getCalendar(d).get(Calendar.DAY_OF_WEEK);
	}
	public static int getDayInMonth(Date d){
		return getCalendar(d).get(Calendar.DAY_OF_MONTH);
	}
	public static String getStrMonth(Date d){
		SimpleDateFormat formater = new SimpleDateFormat("MMMM",Locale.FRENCH);
		return formater.format(d);
	}
	public static int getMonth(Date d){
		return getCalendar(d).get(Calendar.MONTH);
	}
	public static String getStrYear(Date d){
		SimpleDateFormat formater = new SimpleDateFormat("YYYY",Locale.FRENCH);
		return formater.format(d);
	}
	public static int getYear(Date d){
		return getCalendar(d).get(Calendar.YEAR);
	}
	public static int getFirstDayOfWeek(Date d){
		Calendar cal=Calendar.getInstance(Locale.FRENCH);
		cal.setTime(d);
		return cal.getFirstDayOfWeek();
	}
	public static Date getFirstDateOfMonth(Date d){
		Calendar cal=getCalendar(d);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}
	public static int getLastDayOfMonth(Date d){
		Calendar cal=getCalendar(d);
		return cal.getActualMaximum(Calendar.DATE);
	}
	public static Date getLastDateOfMonth(Date d){
		Calendar cal=getCalendar(d);
		int lastDate=cal.getActualMaximum(Calendar.DATE);
		cal.set(Calendar.DATE, lastDate);
		return cal.getTime();
	}
	public static Date set(int field,int value,Date d){
		Calendar cal=getCalendar(d);
		cal.set(field,value);
		return cal.getTime();
	}
	public Calendar getCal() {
		return cal;
	}
	public Date getDate(){
		return cal.getTime();
	}
	public Date set(int field,int value){
		cal.set(field, value);
		return getDate();
	}
	public int get(int field){
		return cal.get(field);
	}
	public void add(int field,int value){
		cal.add(field,value);
	}
	public boolean isNow(){
		KDateUtils now=new KDateUtils();
		return toString().equals(now.toString());
	}
	public boolean dateEquals(Date d){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(d).equals(df.format(cal.getTime()));
	}
	public int getDayOfWeekInMonth(){
		return cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
	}
	public int getDayOfWeek(){
		return cal.get(Calendar.DAY_OF_WEEK);
	}
	public String toString(){
		return DateFormatter.toKoDate(getDate());
	}
	public int getYear(){
		return cal.get(Calendar.YEAR);
	}
	public int getMonth(){
		return cal.get(Calendar.MONTH);
	}
	public int getDay(){
		return cal.get(Calendar.DATE);
	}
	public int getHour(){
		return cal.get(Calendar.HOUR);
	}
	public int getMinutes(){
		return cal.get(Calendar.MINUTE);
	}
}
