package net.ko.types;

public enum HtmlControlType {
		khcNone("","",""),
		khcText("text","text","1"),
		khcPassWord("password","password","1"),
		khcCheckBox("checkbox","checkbox","*"),
		khcRadio("radio","radio","*"),
		khcFile("file","file","1"),
		khcCmb("cmb","select","*"),
		khcDataList("datalist","list","*"),
		khcList("list","select size='5'","*"),
		khcHidden("hidden","hidden","1"),
		khcTextarea("textarea","textarea","1"),
		khcLabel("label","label","1"),
		khcReadOnlyText("readonlytext","readonlytext","1"),
		khcReadOnlyList("readonlylist","readonlylist","*"),
		khcFieldSet("fieldset","fieldset","1"),
		khcButton("button","button","1"),
		khcSubmit("submit","submit","1"),
		khcEmail("email","email","1"),
		khcUrl("url","url","1"),
		khcTel("tel","tel","1"),
		khcDateTime("datetime","datetime","1"),
		khcDateTimeCmb("datetimecmb","datetime","1"),
		khcDate("date","date","1"),
		khcDateCmb("datecmb","date","1"),
		khcMonth("month","month","1"),
		khcWeek("week","week","1"),
		khcTime("time","time","1"),
		khcTimeCmb("timecmb","time","1"),
		khcDateTimeLocal("datetime-local","datetime-local","1"),
		khcNumber("number","number","1"),
		khcRange("range","range","1"),
		khcColor("color","color","1"),
		khcSearch("search","search","1"),
		khcPageList("kpagelist","net.ko.http.views.KPageList","*"),
		khcCheckedList("checkedlist","checkedlist","*"),
		khcRadioList("radiolist","radiolist","*"),
		khcCheckedDataList("checkeddatalist","checkeddatalist","*"),
		khcRadioDataList("radiodatalist","radiodatalist","*"),
		khcCheckedAjaxList("checkedajaxlist","checkedajaxlist","*",true),
		khcRadioAjaxList("radioajaxlist","radioajaxlist","*",true),
		khcListForm("listform","listform","*"),
		khcListFormMany("listformmany","listformmany","*"),
		khcAjaxList("ajaxlist","ajaxlist","*",true),
		khcCustom("custom","custom","1");
		protected String label;
		protected String htmlLabel;
		protected String cardinality;
		protected boolean ajax;
		private HtmlControlType(String label,String htmlLabel,String cardinality) {
			this(label, htmlLabel, cardinality, false);
		}
		private HtmlControlType(String label,String htmlLabel,String cardinality,boolean isAjax) {
			this.label = label;
			this.htmlLabel=htmlLabel;
			this.cardinality=cardinality;
			this.ajax=isAjax;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String toString(){
			return this.htmlLabel;
		}
		public static HtmlControlType getType(String cType){
			HtmlControlType result=null;
			for(HtmlControlType ct:HtmlControlType.values())
				if(ct.getLabel().equals(cType)){
					result=ct;
					break;
				}
			if(result==null)
				result=khcText;
			return result;
		}
		public String getHtmlLabel() {
			return htmlLabel;
		}
		public void setHtmlLabel(String htmlLabel) {
			this.htmlLabel = htmlLabel;
		}
		public String getCardinality() {
			return cardinality;
		}
		public static HtmlControlType getCtForList(HtmlControlType ct){
    		HtmlControlType result=ct;
			if(!ct.getCardinality().equals("*"))
    			result=HtmlControlType.khcList;
			else
				result=khcCmb;
			return result;
		}
		public boolean isAjax() {
			return ajax;
		}
		public void setAjax(boolean ajax) {
			this.ajax = ajax;
		}
}
