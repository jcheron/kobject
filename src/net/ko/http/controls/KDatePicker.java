package net.ko.http.controls;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.ko.converters.DateFormatter;
import net.ko.utils.KDateUtils;

public class KDatePicker {
	private String value;
	private Date dateValue;
	private Date selectedDate;
	private String onclick="onmousedown='javascript:$(\"{inputId}\").focused=false;$(\"{inputId}\").dtp.setValue(\"{dateValue}\",\"{valueType}\",this);'";
	private String dayCell="<td><div class='{class}' id='{idDay}' {onclick}>{value}</div></td>";
	private String inputId;
	private String id;
	private boolean hasDate=false;
	private boolean hasTime=false;
	private int minHour=0;
	private int maxHour=23;
	private int stepHour=6;
	private int stepMinutes=3;
	
	public KDatePicker() {
		this(Calendar.getInstance().getTime());
	}
	public KDatePicker(String value) {
		this(value,value);
	}
	public KDatePicker(String value,String selectedDate) {
		super();
		setType("date");
		setValue(value);
		setSelectedDate(selectedDate);
	}
	public KDatePicker(String value,String selectedDate,String type) {
		super();
		setType(type);
		setValue(value);
		setSelectedDate(selectedDate);
	}
	public KDatePicker(Date dateValue) {
		super();
		setDateValue(dateValue);
		selectedDate=dateValue;
	}
	private String getCell(){
		return getCell("", "", "", "","");
	}
	private String onClick(String dateValue,String valueType){
		String result="";
		if(!"".equals(dateValue)){
			result=onclick.replaceAll("\\{dateValue\\}", dateValue);
			result=result.replace("{valueType}", valueType);
		}
		return result;
		
	}
	private String getBtn(String caption,String date){
		return "<div class='dtpBtn' onmousedown=\"javascript:$('"+inputId+"').dtp.requestDtp('"+date+"');\">"+caption+"</div>";
	}
	private String getHeader(){
		String result="<div class='dtpHeader'>"+getBtn("<div class='arrow-left'></div>", getPrevMonthDate());
		result+="<div class='dtpCaption'><span class='dtpMonth'>"+KDateUtils.getStrMonth(dateValue)+"</span>";
		result+="&#xA0;<span class='dtpYear'>"+KDateUtils.getStrYear(dateValue)+"</span></div>";
		result+=getBtn("<div class='arrow-right'></div>", getNextMonthDate());
		result+="</div>";
		return result;
	}
	private String getDays(){
		String[] joursCourts = new String[] {"Di","Lu","Ma","Me","Je","Ve","Sa"};
		String result="<tr>";
		for(String j:joursCourts)
			result+="<td class='dtpDHeader'>"+j+"</td>";
		result+="</tr>";
		return result;
	}
	private String getCell(String value,String dateValue,String htmlClass,String idDay,String valueType){
		String result=dayCell;
		result=result.replace("{idDay}", idDay);
		result=result.replace("{class}", htmlClass);
		result=result.replace("{value}", value);
		result=result.replace("{dateValue}", dateValue);
		result=result.replace("{onclick}", onClick(dateValue,valueType));
		result=result.replaceAll("\\{inputId\\}", inputId);
		result=result.replace("{id}", id);
		return result;
	}
	private String repeat(String html,int count){
		String result="";
		for(int i=0;i<count;i++)
			result+=html;
		return result;
	}
	public void addOn(String id,String inputId){
		this.id=id;
		this.inputId=inputId;
	}
	public int getPosOfFirstDayOfMonth(){
		return KDateUtils.getDayOfWeek(getFirstDayOfMonth())-1;
	}
	public Date getFirstDayOfMonth(){
		return KDateUtils.getFirstDateOfMonth(dateValue);
	}
	public Date getLastDayOfMonth(){
		return KDateUtils.getLastDateOfMonth(dateValue);
	}	
	public int getPosOfLastDayOfMonth(){
		return KDateUtils.getDayOfWeek(getLastDayOfMonth())-1;
	}
	private String getPrevMonthDate(){
		KDateUtils du=new KDateUtils(dateValue);
		du.add(Calendar.MONTH, -1);
		return du.toString();
	}
	private String getNextMonthDate(){
		KDateUtils du=new KDateUtils(dateValue);
		du.add(Calendar.MONTH,1);
		return du.toString();
	}
	public String getMonth(){
		return KDateUtils.getStrMonth(dateValue);
	}
	public String getYear(){
		return KDateUtils.getStrYear(dateValue);
	}
	private String[] getMinutes(){
		return getPlage(0, 55, 5);
	}
	private String[] getHours(){
		return getPlage(0, 23, 1);
	}
	private String[] getPlage(int min,int max,int step){
		String[] result=new String[((max-min)/step)+1];
		int j=0;
		for(int i=min;i<=max;i=i+step){
			result[j]=String.format("%02d", i);
			j++;
		}
		return result;
	}
	private String partToString(String title,String[] values,String valueType,int step,String selectedValue){
		String result="<div class='innerDtp' id='dtp-"+id+"-"+valueType+"'><table border='0'><tr><td colspan='"+step+"'><div class='dtpHeader'><span class='dtpHour'>"+title+"</span></div></td></tr><tr>";
		boolean open=true;
		int j=0;
		for(int i=0;i<values.length;i++){
			String v=values[i];
			String className="day";
			if(!open){
				result+="<tr>";
				open=true;
			}
			if(v.equals(selectedValue))
				className="dateActive";
			result+=getCell(v, v, className, "id-"+valueType+"-"+v, valueType);
			j++;
			if(j==step){
				result+="</tr>\n";
				open=false;
				j=0;
			}
		}
		result+=repeat(getCell(), step-j-1);
		result+="</tr></table></div>\n";
		return result;
	}
	private String getSelectedHours(){
		DateFormat df = new SimpleDateFormat("HH");
		return df.format(selectedDate);
	}
	private String hourToString(){
		String result=partToString("Heure", getHours(),"h",stepHour,getSelectedHours());
		return result;
	}
	private String getSelectedMinutes(){
		DateFormat df = new SimpleDateFormat("mm");
		return df.format(selectedDate);
	}
	private String minutesToString(){
		String result=partToString("Minute", getMinutes(),"m",stepMinutes,getSelectedMinutes());
		return result;
	}
	public String timeToString(){
		String result=hourToString()+minutesToString();
		return result;
	}
	public String dateToString(){
		String result="<div class='innerDtp' id='dtp-"+id+"'><table border='0'><tr><td colspan='7'>"+getHeader()+"</td></tr>\n<tr>";
		result+=getDays();
		boolean open=true;
		KDateUtils du=new KDateUtils(dateValue);
		result+=repeat(getCell(), getPosOfFirstDayOfMonth());
		int max=KDateUtils.getLastDayOfMonth(dateValue);
		for(int d=1;d<=max;d++){
			String className="day";
			du.set(Calendar.DAY_OF_MONTH, d);
			if(du.getDayOfWeek()==1 && !open){
				result+="<tr>";
				open=true;
			}
			if(du.isNow())
				className="now";
			if(du.dateEquals(selectedDate))
				className="dateActive";
			result+=getCell(d+"", du+"", className, "id"+d,"d");
			if(du.getDayOfWeek()==7){
				result+="</tr>\n";
				open=false;
			}
		}
		result+=repeat(getCell(), 7-getPosOfLastDayOfMonth()-1);
		result+="</tr></table></div>\n";
		return result;
	}
	public String toString(){
		String result="";
		if(hasDate)
			result+=dateToString();
		if(hasTime)
			result+=timeToString();
		return result;
	}

	public void setValue(String value) {
		this.value = value;
		try{
			if(hasTime && hasDate)
				dateValue=DateFormatter.getKoDateTime(value);
			else if(hasDate)
				dateValue=DateFormatter.getKoDate(value);
			else
				dateValue=DateFormatter.getKoTime(value);
		}catch(Exception e){dateValue=new Date();}
	}
	public void setSelectedDate(String value) {
		try{
			if(hasTime && hasDate)
				selectedDate=DateFormatter.getKoDateTime(value);
			else if(hasDate)
				selectedDate=DateFormatter.getKoDate(value);
			else
				selectedDate=DateFormatter.getKoTime(value);
		}catch(Exception e){selectedDate=new Date();}
	}
	public String getValue() {
		return value;
	}
	public Date getDateValue() {
		return dateValue;
	}
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
		value=DateFormatter.toKoDate(dateValue);
	}
	public String getInputId() {
		return inputId;
	}
	public void setInputId(String inputId) {
		this.inputId = inputId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setType(String type){
		if(type.toLowerCase().contains("date"))
			hasDate=true;
		if(type.toLowerCase().contains("time"))
			hasTime=true;
	}
}
