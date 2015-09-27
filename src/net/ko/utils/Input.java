package net.ko.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Input {
	private boolean stop;
	private String prompt;
	private KFonction fonction;
	
	public KFonction getFonction() {
		return fonction;
	}

	public void setFonction(KFonction fonction) {
		this.fonction = fonction;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public Input(){
		stop=false;
		prompt=">";
	}
	public String scanf(){
		System.out.print(prompt);
		String rep=null;
		BufferedReader inStr = new BufferedReader(new InputStreamReader(System.in));
		do{
			   try{
				rep = inStr.readLine();
				if (fonction!=null) fonction.execute();
			   }
			   catch(Exception e){}
		}while(!stop);
		return rep;
	}
}
