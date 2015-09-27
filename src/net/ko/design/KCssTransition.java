package net.ko.design;

import java.util.ArrayList;
import java.util.List;

import net.ko.mapping.IAjaxObject;
import net.ko.mapping.IHasSelector;
import net.ko.mapping.KAjaxFunction;
import net.ko.utils.KString;
import net.ko.utils.KStrings;
import net.ko.utils.XmlUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KCssTransition extends KAjaxFunction implements IHasSelector {
	protected String targetId;
	protected List<KCssOneTransition> transitions;

	public KCssTransition() {
		super();
		transitions = new ArrayList<>();
		targetId = "";
	}

	public KCssTransition(String targetId, String property, String startValue, String endValue, String duration, String timing) {
		this();
		this.targetId = targetId;
		KCssOneTransition one = new KCssOneTransition(property, startValue, endValue, duration, timing);
		if (one.isValid())
			transitions.add(one);
	}

	@Override
	public void initFromXml(Node item) {
		Element e = (Element) item;
		targetId = e.getAttribute("targetId");
		NodeList cNodes = item.getChildNodes();
		for (int i = 0; i < cNodes.getLength(); i++) {
			if ("oneTransition".equals(cNodes.item(i).getNodeName())) {
				KCssOneTransition oneTransition = new KCssOneTransition();
				oneTransition.initFromXml(cNodes.item(i));
				if (oneTransition.isValid())
					transitions.add(oneTransition);
			}
		}
	}

	private String createJSONTransitionsArray() {
		String result = "";
		ArrayList<String> strTransitions = new ArrayList<>();
		for (KCssOneTransition one : transitions) {
			strTransitions.add(one.createFunction());
		}
		result = KStrings.implode(",", strTransitions, "");
		return result;
	}

	@Override
	public String createFunction() {
		String result = "Forms.Css3.defineTransitions($('" + targetId + "'),[" + createJSONTransitionsArray() + "]);";
		return result;
	}

	public void addTransition(KCssOneTransition one) {
		transitions.add(one);
	}

	public void addTransitions(List<KCssOneTransition> multi) {
		transitions.addAll(multi);
	}

	public static KCssTransition preparedTransition(String targetId, String transitionType) {
		KCssTransition result = new KCssTransition();
		result.setTargetId(targetId);
		if (transitionType.contains(",")) {
			String[] strsTransitions = transitionType.split(",");
			for (String trans : strsTransitions) {
				result.addTransitions(preparedTransition(targetId, trans).getTransitions());
			}
		}
		else {
			switch (transitionType) {
			case "opacityShow":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "1", "linear"));
				break;

			case "opacityHide":
				result.addTransition(new KCssOneTransition("opacity", "1", "0", "1", "linear"));
				break;

			case "rotation10":
				result.addTransition(new KCssOneTransition("transform", "rotate(350deg)", "rotate(360deg)", "0.5", "ease"));
				break;

			case "rotation120":
				result.addTransition(new KCssOneTransition("transform", "rotate(240deg)", "rotate(360deg)", "0.7", "ease"));
				break;

			case "rotation180":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "1", "linear"));
				result.addTransition(new KCssOneTransition("transform", "rotate(180deg)", "rotate(360deg)", "1", "ease"));
				break;

			case "deflate":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "0.8", "linear"));
				result.addTransition(new KCssOneTransition("transform", "scale(1.5,1.5)", "scale(1,1)", "0.8", "ease"));
				break;

			case "inflate":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "0.8", "linear"));
				result.addTransition(new KCssOneTransition("transform", "scale(0.2,0.2)", "scale(1,1)", "0.8", "ease"));
				break;

			case "skew":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "1", "linear"));
				result.addTransition(new KCssOneTransition("transform", "skew(30deg,20deg)", "skew(0deg,0deg)", "1", "ease"));
				break;

			case "translate":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "1", "linear"));
				result.addTransition(new KCssOneTransition("transform", "translate(-500px,-500px)", "translate(0px,0px)", "1", "ease"));
				break;

			case "banzai":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "1", "linear"));
				result.addTransition(new KCssOneTransition("transform", "rotate(180deg) scale(2,2) skew(40deg,30deg)", "rotate(360deg) scale(1,1) skew(0deg,0deg)", "2", "ease"));
				break;

			case "background":
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "0.8", "linear"));
				result.addTransition(new KCssOneTransition("background-color", "#ccccb8", null, "0.8", "linear"));
				break;

			case "color":
				result.addTransition(new KCssOneTransition("color", "rgb(61, 116, 147)", null, "1", "linear"));
				break;

			case "shadow":
				result.addTransition(new KCssOneTransition("box-shadow", "0px 0px 0px #33ff66", null, "1", "ease"));
				break;

			default:
				result.addTransition(new KCssOneTransition("opacity", "0", "1", "1", "linear"));
				break;
			}
		}
		return result;

	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public List<KCssOneTransition> getTransitions() {
		return transitions;
	}

	@Override
	public boolean isValid() {
		return KString.isNotNull(targetId) && transitions.size() > 0;
	}

	@Override
	public String getName() {
		return "transition";
	}

	@Override
	public String getDisplayCaption() {
		return "[" + getDOMSelector() + "]" + ":transition" + "(" + transitions.size() + ")";
	}

	@Override
	public String getDOMSelector() {
		return "#" + targetId;
	}

	@Override
	public Element saveAsXML(Element parentElement) {
		Element result = super.saveAsXML(parentElement);
		XmlUtils.setAttribute(result, "targetId", targetId);
		for (IAjaxObject transition : transitions) {
			transition.saveAsXML(result);
		}
		return result;
	}
}
