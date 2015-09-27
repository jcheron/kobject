/**
 * Enumeration KRecordStatus
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2012
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KRecordStatus.java,v 1.2 2010/01/06 00:35:22 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.kobject;

public enum KRecordStatus {
	rsNone(0,"Aucune"),
	rsUpdate(1,"Mis à jour"),
	rsNew(2,"Ajout"),
	rsDelete(3,"Suppression"),
	rsLoaded(4,"Chargement"),
	rsImpossible(9,"Opération impossible ou annulée");
	protected int label;
	protected String caption;
	private KRecordStatus(int value,String caption) {
		this.label = value;
		this.caption=caption;
	}
	public int getLabel() {
		return label;
	}
	public void setLabel(int value) {
		this.label = value;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String toString(){
		return caption;
	}
}
