/**
 * Enumeration KRecordStatus
 * 
 * @author jcheron
 * @link http://www.kobject.net/
 * @copyright Copyright kobject 2008-2009
 * @license http://www.kobject.net/index.php?option=com_content&view=article&id=50&Itemid=2  BSD License
 * @version $Id: KRecordStatus.java,v 1.2 2010/01/06 00:35:22 jcheron Exp $
 * @package ko.kobject
 */
package net.ko.cache;

public enum KCacheType {
	ktNone(0),
	ktListOnly(1),
	ktListAndObjects(2);
	protected int label;
	
	private KCacheType(int value) {
		this.label = value;
	}
	
	public int getLabel() {
		return label;
	}
	
	public void setLabel(int value) {
		this.label = value;
	}
	
	public static KCacheType getCacheTypeByIndex(int index) {
		switch (index) {
		case 0:
			return ktNone;
		case 1:
			return ktListOnly;
		case 2:
			return ktListAndObjects;

		default:
			return ktListOnly;
		}
	}
	public static KCacheType getCacheTypeByIndex(String index) {
		switch (index) {
		case "0":
			return ktNone;
		case "1":
			return ktListOnly;
		case "2":
			return ktListAndObjects;

		default:
			return ktListOnly;
		}
	}
}
