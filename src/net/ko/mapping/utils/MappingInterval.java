package net.ko.mapping.utils;

import net.ko.utils.KString;

public class MappingInterval {
	private String name;
	private int interval = -1;

	public MappingInterval(String strInterval) {
		if (KString.isNotNull(strInterval)) {
			String s[] = strInterval.split(":");
			if (s.length > 0) {
				name = s[0];
				interval = 1000;
			}
			if (s.length > 1) {
				try {
					interval = Integer.valueOf(s[1]);
				} catch (Exception e) {

				}
			}
		}
	}

	public boolean isValid() {
		return KString.isNotNull(name) && interval != -1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

}
