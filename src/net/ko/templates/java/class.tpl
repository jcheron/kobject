package %package%;

import net.ko.kobject.KObject;
import net.ko.persistence.annotation.Entity;
import net.ko.persistence.annotation.Table;
%imports%

/**
* Classe %className%
*/
@SuppressWarnings("serial")
%annotations%
public class %className% extends KObject {
%members%
	public %className%() {
		super();
		//%constraints%
	}
%getters%
%setters%
	@Override
	public String toString() {
		%toString%;
	}
}