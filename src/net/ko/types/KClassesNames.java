package net.ko.types;

@SuppressWarnings("rawtypes")
public enum KClassesNames {
	kcnKobject("kobject","kobject","KObject"),
	kcnKlistObject("klistobject","kobject","KListObject"),
	kcnKo("ko","framework","Ko"),
	kcnKoHttp("ko","framework","KoHttp"),
	kcnKSession("ksession","kobject","KSession"),
	kcnKhttpSession("khttpsession","http.objects","KHttpSession");
	protected String realName;
	protected String name;
	protected String packageName;
	private static final String prefix="net.ko.";
	private KClassesNames(String name,String packageName,String realName){
		this.name=name;
		this.packageName=packageName;
		this.realName=realName;
	}
	public String getCompleteClassName(String className){
		String result=null;
		for(KClassesNames cn:KClassesNames.values())
			if(cn.getName().equals(className)){
				result=prefix+cn.getPackageName()+"."+cn.getRealName();
				break;
			}
		return result;
	}
	
	public Class getClazz(String className) throws ClassNotFoundException{
		Class clazz=null;
		String cn=getCompleteClassName(className);
		if(cn!=null)
			clazz=Class.forName(cn);
		return clazz;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
