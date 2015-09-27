package net.ko.design;

import net.ko.mapping.KAjaxObject;
import net.ko.utils.KString;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class KCssOneTransition extends KAjaxObject {
	private String property;
	private String startValue;
	private String endValue;
	private String duration;
	private String timing;

	public KCssOneTransition() {
		super();
	}

	@Override
	public String createFunction() {
		String sv = "";
		if (KString.isNotNull(startValue))
			sv = "startValue:'" + startValue + "',";
		String ev = "";
		if (KString.isNotNull(endValue))
			ev = "endValue:'" + endValue + "',";
		return "{property:'" + property + "'," + sv + ev + "duration:'" + duration + "s " + timing + "'}";
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(property);
	}

	@Override
	public void initFromXml(Node item) {
		Element e = (Element) item;
		property = e.getAttribute("property");
		startValue = e.getAttribute("startValue");
		endValue = e.getAttribute("endValue");
		duration = e.getAttribute("duration");
		timing = e.getAttribute("timing");
		if (KString.isNull(duration))
			duration = "1";
		if (KString.isNull(timing))
			timing = "linear";
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "property", property);
		XmlUtils.setAttribute(result, "startValue", startValue);
		XmlUtils.setAttribute(result, "endValue", endValue);
		XmlUtils.setAttribute(result, "duration", duration);
		XmlUtils.setAttribute(result, "timing", timing);
		return result;
	}

	public KCssOneTransition(String property, String startValue, String endValue, String duration, String timing) {
		super();
		this.property = property;
		this.startValue = startValue;
		this.endValue = endValue;
		this.duration = duration;
		this.timing = timing;
	}

	@Override
	public String getDisplayCaption() {
		return property + "(" + startValue + "->" + endValue + ")";
	}

	@Override
	public String getDebugClientId() {
		return null;
	}

	@Override
	public String getName() {
		return "oneTransition";
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getStartValue() {
		return startValue;
	}

	public void setStartValue(String startValue) {
		this.startValue = startValue;
	}

	public String getEndValue() {
		return endValue;
	}

	public void setEndValue(String endValue) {
		this.endValue = endValue;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getTiming() {
		return timing;
	}

	public void setTiming(String timing) {
		this.timing = timing;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

}
