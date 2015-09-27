package net.ko.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.ko.db.creation.KForeignKey;

public class KDBForeignKeyList implements Iterable<KDBForeignKey> {
	List<KDBForeignKey> items;

	public KDBForeignKeyList() {
		items = new ArrayList<>();
	}

	@Override
	public Iterator<KDBForeignKey> iterator() {
		return items.iterator();
	}

	public void add(KDBForeignKey fk) {
		items.add(fk);
	}

	public void remove(KDBForeignKey fk) {
		items.remove(fk);
	}

	public int indexOf(KDBForeignKey fk) {
		return items.indexOf(fk);
	}

	public List<KDBForeignKey> getFkUsingReferencesField(String tableName, String fieldName) {
		List<KDBForeignKey> result = new ArrayList<>();
		for (KDBForeignKey fk : items) {
			if (fieldName.equalsIgnoreCase(fk.getPkFieldName()) && tableName.equalsIgnoreCase(fk.getPkTableName()))
				result.add(fk);
		}
		return result;
	}

	public List<KDBForeignKey> getFkUsingReferencesField(String fieldName) {
		List<KDBForeignKey> result = new ArrayList<>();
		for (KDBForeignKey fk : items) {
			if (fieldName.equalsIgnoreCase(fk.getPkFieldName()))
				result.add(fk);
		}
		return result;
	}

	public void add(KDBForeignKeyList fks) {
		items.addAll(fks.getItems());
	}

	public List<KDBForeignKey> getItems() {
		return items;
	}

	public int size() {
		return items.size();
	}

	public KDBForeignKey get(int index) {
		return items.get(index);
	}

	public boolean contains(KForeignKey fk) {
		return items.indexOf(fk) != -1;
	}
}
