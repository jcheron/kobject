package net.ko.bean;

import net.ko.http.views.KFieldControl;
import net.ko.http.views.KViewElementsMap;
import net.ko.types.KViewElementsMapOpType;

/**
 * Opération différée sur un objet de type KViewElementsMap (Liste d'éléments à afficher dans une vue)
 * @author jc
 * @see KViewElementsMap
 */
public class KViewElementsMapOperation {
	private KViewElementsMapOpType opType;
	private String[] fields;
	private KFieldControl value;
	public KViewElementsMapOperation toAdd(String fieldName,KFieldControl value){
		fields=new String[]{fieldName};
		this.value=value;
		opType=KViewElementsMapOpType.kOpAdd;
		return this;
	}
	public KViewElementsMapOperation toDelete(String fieldName){
		fields=new String[]{fieldName};
		opType=KViewElementsMapOpType.kOpDelete;
		return this;
	}
	public KViewElementsMapOperation toSwap(String fieldName1,String fieldName2){
		fields=new String[]{fieldName1,fieldName2};
		opType=KViewElementsMapOpType.kOpSwap;
		return this;
	}
	public KViewElementsMapOperation() {
		super();
	}
	public KViewElementsMapOpType getOpType() {
		return opType;
	}
	public void setOpType(KViewElementsMapOpType opType) {
		this.opType = opType;
	}
	public String[] getFields() {
		return fields;
	}
	public void setFields(String[] fields) {
		this.fields = fields;
	}
	public KFieldControl getValue() {
		return value;
	}
	public void setValue(KFieldControl value) {
		this.value = value;
	}
	public void execute(KViewElementsMap elementsMap){
		switch (opType) {
		case kOpAdd:
			elementsMap.put(fields[0], value);
			break;
		case kOpDelete:
			elementsMap.remove(fields[0]);
			break;
		case kOpSwap:
			elementsMap.swap(fields[0], fields[1]);
		default:
			break;
		}
	}
}
