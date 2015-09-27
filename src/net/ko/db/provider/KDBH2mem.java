package net.ko.db.provider;

import java.sql.SQLException;

import net.ko.bean.KDbParams;

public class KDBH2mem extends KDBH2 {

	public KDBH2mem() {
		super("mem", "sa", "", "H2memory");
	}

	@Override
	public void setParams(KDbParams params) {
	}

	@Override
	public void close() throws SQLException {
	}

	@Override
	public String javaTypeToDbType(Class<? extends Object> aClazz) {
		String result = super.javaTypeToDbType(aClazz);
		if (aClazz.equals(String.class))
			result = "VARCHAR";
		return result;
	}

}
