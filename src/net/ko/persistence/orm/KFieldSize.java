package net.ko.persistence.orm;

public class KFieldSize {
	private int precision;
	private int scale;

	public KFieldSize() {
		this(-1, -1);
	}

	public KFieldSize(int precision, int scale) {
		this.precision = precision;
		this.scale = scale;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	@Override
	public String toString() {
		String result = "";
		if (precision != -1) {
			result = precision + "";
			if (scale != -1)
				result += "," + scale;
		}
		if (!"".equals(result))
			result = "(" + result + ")";
		return result;
	}

}
