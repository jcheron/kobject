package net.ko.db.provider;

import net.ko.bean.KDbParams;

public class KDBHsqldbmem extends KDBHsqldb {
	public KDBHsqldbmem() {
		super("mem", "sa", "", "HSQLDB.memory");
	}

	@Override
	public void setParams(KDbParams params) {
	}
}
