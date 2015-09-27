package net.ko.db.validation;

public enum KSchemaDDL {
	sdNone("false", "none"), sdValidate("validate"), sdUpdate("update"), sdCreate("create");
	private String[] captions;

	private KSchemaDDL(String... values) {
		this.captions = values;
	}

	public String[] getCaptions() {
		return captions;
	}

	public static KSchemaDDL getValue(String aCaption) {
		KSchemaDDL result = sdValidate;
		for (KSchemaDDL sd : KSchemaDDL.values()) {
			for (String caption : sd.getCaptions()) {
				if (caption.equalsIgnoreCase(aCaption)) {
					result = sd;
					break;
				}
			}
		}
		return result;
	}
}
