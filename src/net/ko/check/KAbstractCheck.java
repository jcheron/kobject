package net.ko.check;

import java.io.File;
import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;

import net.ko.utils.KString;

public abstract class KAbstractCheck {
	protected KCheckMessagesList messageList;
	protected EnumSet<KCheckMessageType> optionsAf = EnumSet.of(KCheckMessageType.TITLE, KCheckMessageType.NONE, KCheckMessageType.UNKNOWN);
	protected EnumSet<KCheckOptions> options = EnumSet.noneOf(KCheckOptions.class);
	protected boolean expand;
	protected HttpServletRequest request;

	public KAbstractCheck(HttpServletRequest request) {
		this.request = request;
		messageList = new KCheckMessagesList();
		expand = false;
	}

	public abstract void checkAll();

	public String getMessages() {
		String result = messageList.display(optionsAf);
		if (KString.isNull(result))
			if (isActive())
				result = messageList.addInfoMessage("Aucune information à afficher").toString();
		return result;
	}

	public void setOptions(String optionsCheck, String optionsAff) {
		if (KString.isNull(optionsCheck)) {
			for (KCheckOptions opt : KCheckOptions.getValues())
				this.options.add(opt);
		} else {
			String[] options = optionsCheck.split(";");
			for (String opt : options) {
				KCheckOptions realOption = KCheckOptions.getValue(opt);
				if (realOption != null)
					this.options.add(realOption);
			}
		}
		if (optionsAff.contains("info"))
			optionsAf.add(KCheckMessageType.INFO);
		if (optionsAff.contains("error"))
			optionsAf.add(KCheckMessageType.ERROR);
		if (optionsAff.contains("warning"))
			optionsAf.add(KCheckMessageType.WARNING);
		if (optionsAff.contains("valid"))
			optionsAf.add(KCheckMessageType.VALID);
	}

	protected void addInfo(KCheckMessageWithChilds checkMessage, String caption, String value) {
		if (KString.isNotNull(value))
			checkMessage.addInfoMessage(caption + value);
	}

	protected void addInfo(KCheckMessageWithChilds checkMessage, String caption, boolean value) {
		if (value)
			checkMessage.addInfoMessage(caption + value);
	}

	public EnumSet<KCheckOptions> getOptions() {
		return options;
	}

	public boolean isExpand() {
		return expand;
	}

	public void setExpand(boolean expand) {
		this.expand = expand;
	}

	public abstract boolean isActive();

	protected KCheckMessage checkFile(String fileName, String caption, KCheckMessageWithChilds messageParent) {
		KCheckMessage result = null;
		if (KString.isNull(fileName)) {
			result = messageParent.addMessage(KCheckMessageType.ERROR, "<b>" + caption + "</b>" + "élément de configuration absent");
		} else {
			String realPath = request.getServletContext().getRealPath(fileName);
			File f = new File(realPath);

			if (!f.exists())
				result = messageParent.addMessage(KCheckMessageType.WARNING, "<b>" + caption + "</b>" + fileName + " n'existe pas sur le serveur");
			else
				result = messageParent.addInfoMessage(caption + fileName + " Ok");
		}
		return result;
	}
}
