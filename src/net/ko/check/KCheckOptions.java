package net.ko.check;

public enum KCheckOptions {
	coConfig("config"), coDb("db"), coClasses("classes"), coControllers("controllers"), coMappings("mappings"), coFilters("filters"), coAjaxIncludes("ajax-includes"), coNone("none");
	private String caption;

	private KCheckOptions(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public static KCheckOptions[] getValues() {
		return KCheckOptions.class.getEnumConstants();
	}

	public static KCheckOptions getValue(String caption) {
		for (KCheckOptions opt : getValues()) {
			if (opt.getCaption().equals(caption))
				return opt;
		}
		return KCheckOptions.coNone;
	}
}
