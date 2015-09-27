package net.ko.mapping;

public class KParameter {
	private String name;
	private String value;
	private int position;
	public KParameter() {
	}
	public KParameter(String name, String position) {
		super();
		this.name = name;
		try{
			this.position = Integer.valueOf(position);
		}catch(Exception e) {this.position=0;}
	}
	public KParameter(String name, int position) {
		super();
		this.name = name;
		this.position = position;
	}
	public KParameter(String name, String position,int defaultPos) {
		super();
		this.name = name;
		try{
			this.position = Integer.valueOf(position);
		}catch(Exception e) {this.position=defaultPos;}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
}
