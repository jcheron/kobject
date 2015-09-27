package net.ko.ksql;

public class KParameterizedExecute extends KParameterizedInstruction {
	public KParameterizedExecute(String quote, String tableName) {
		super(quote);
		this.tableName = tableName;
	}
}
