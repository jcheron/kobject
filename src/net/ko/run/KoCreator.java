/**
 * Classe KoCreator
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2010
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KoCreator.java,v 1.5 2011/01/14 01:12:55 jcheron Exp $
 * @package ko.run
 */
package net.ko.run;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import net.ko.db.KDataBase;
import net.ko.db.provider.KDBMysql;
import net.ko.utils.Input;
import net.ko.utils.KFonction;
import net.ko.utils.KProperties;


public class KoCreator {
	public static KDataBase connect(KProperties saisie){
		if ( System.getProperty("os.name").startsWith("Windows") )
			try {
				System.setOut ( new PrintStream(System.out, true, "CP850") );
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
		KDataBase db=null;
		try {
			db=new KDBMysql();
			db.connect(saisie.getProperty("host"), saisie.getProperty("user"), saisie.getProperty("password"), saisie.getProperty("base"));
		} catch (ClassNotFoundException e) {
			db=null;
			System.out.println("Impossible de trouver la classe du driver");
		} catch (SQLException e) {
			db=null;
			e.printStackTrace();
		}
		return db;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		class IpFonction implements KFonction{
			public Input ip;
			@Override
			public void execute() {
				ip.setStop(true);
				
			}
			public IpFonction(Input ip){
				this.ip=ip;
			}
		}
		KApplication application=new KApplication();
		KProperties saisie=new KProperties();
		
		if ( System.getProperty("os.name").startsWith("Windows") )
			try {
				System.setOut ( new PrintStream(System.out, true, "cp850") );
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			
		System.out.println("help pour obtenir de l'aide sur les commandes.");
		do{
			Input ip=new Input();
			ip.setFonction(new IpFonction(ip));
			String rep=ip.scanf();
			String[] cmd=rep.split(";");
			for (int i = 0; i < cmd.length; i++) {
				saisie=new KProperties(cmd[i], " ");
				application.send(saisie);				
			}
			
		}while(!(saisie.keyExist("quit")||saisie.keyExist("q")));
		System.out.println("Bye\n");

	}

}
