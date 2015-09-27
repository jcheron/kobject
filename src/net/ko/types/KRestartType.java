package net.ko.types;

public enum KRestartType {
	rtBddConn("0"),rtValidation("1"),rtMappings("2"),rtMessages("3"),rtCssVars("4");
	public String value;
	private KRestartType(String value){
		this.value=value;
	}
}
