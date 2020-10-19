/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datamodel;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Author: Anas H. Sulaiman 
 */
public class TagGroup {
	private static final Set<TagGroup> instances = new LinkedHashSet<>();
	long id;
	String name;

	public TagGroup() {
		this(-1, "");
	}

	public TagGroup(long id, String name) {
		this.id = id;
		this.name = name.trim();

		instances.add(this);
	}

	public static TagGroup getIfExists(long id) {
		for (TagGroup tg : instances) {
			if (tg.getId() == id) return tg;
		}
		return null;
	}

	public static TagGroup getIfExists(String name) {
		for (TagGroup tg : instances) {
			if (tg.getName().equalsIgnoreCase(name)) return tg;
		}
		return null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public boolean isNull() {
		return name.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TagGroup tagGroup = (TagGroup) o;

		return name.equals(tagGroup.name);

	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "Tag Group:\n\tID\t\t:\t" + id + "\n\tName\t:\t" + name;
	}
}
