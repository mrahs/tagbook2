/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datamodel;

import utils.D;
import utils.Utils;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Author: Anas H. Sulaiman 
 */

public class Tag {
	private static final String ALLOWED_CHARS = "(?U)^\\w*$";
	private static final Pattern pattern = Pattern.compile(ALLOWED_CHARS);
	private static final Set<Tag> instances = new LinkedHashSet<>();
	long id;
	Long parentId; // null
	String name;
	String color;
	Timestamp dateadd;
	Timestamp datemod;
	TagGroup group; // null
	private Utils u = Utils.getInstance();


	/**
	 * Constructs a new Tag with the following defaults:
	 * <pre>
	 *     id = -1
	 *     parentId = null
	 *     color = ""
	 *     dateadd = datemod = now
	 *     group = null;
	 * </pre>
	 */
	public Tag(String name) {
		this(-1, name);
	}

	/**
	 * Constructs a new Tag with the following defaults:
	 * <pre>
	 *     parentId = null
	 *     color = ""
	 *     dateadd = datemod = now
	 *     group = null;
	 * </pre>
	 */
	public Tag(long id, String name) {
		this(id, name, "", null, null, Utils.getInstance().nowTs(), Utils.getInstance().nowTs());
	}

	/**
	 * Constructs a new Tag with the following defaults:
	 * <pre>
	 *     id = -1
	 *     dateadd = datemod = now
	 * </pre>
	 */
	public Tag(String name, String color, Long parentId, TagGroup group) {
		this(-1, name, color, parentId, group, Utils.getInstance().nowTs(), Utils.getInstance().nowTs());
	}

	public Tag(long id, String name, String color, Long parentId, TagGroup group, Timestamp dateadd, Timestamp datemod) {
		this.id = id;

		name = name.trim();
		D.checkValidation(isValidTagName(name));
		this.name = name;

		color = color.trim();
		if (!color.isEmpty()) D.checkValidation(u.isHexColor(color));
		this.color = color;

		this.parentId = parentId;
		this.group = group;

		this.dateadd = Objects.requireNonNull(dateadd);
		setDatemod(datemod);

		instances.add(this);
	}

	public static Tag getIfExists(long id) {
		for (Tag t : instances) {
			if (t.getId() == id) return t;
		}
		return null;
	}

	public static Tag getIfExists(String name) {
		for (Tag t : instances) {
			if (t.getName().equalsIgnoreCase(name)) return t;
		}
		return null;
	}

	public static boolean isValidTagName(String name) {
		return name.isEmpty() || pattern.matcher(name).matches();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
		setDatemod(u.nowTs());
	}

	public TagGroup getGroup() {
		return group;
	}

	public void setGroup(TagGroup group) {
		this.group = group;
		setDatemod(u.nowTs());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		name = name.trim();
		D.checkValidation(isValidTagName(name));
		this.name = name;
		setDatemod(u.nowTs());
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		color = color.trim();
		if (!color.isEmpty()) D.checkValidation(u.isHexColor(color));
		this.color = color;
		setDatemod(u.nowTs());
	}

	public Timestamp getDateadd() {
		return dateadd;
	}

	public void setDateadd(Timestamp dateadd) {
		this.dateadd = Objects.requireNonNull(dateadd);
		if (this.dateadd.compareTo(this.datemod) > 0) this.datemod = new Timestamp(this.dateadd.getTime());
	}

	public Timestamp getDatemod() {
		return datemod;
	}

	public void setDatemod(Timestamp datemod) {
		if (datemod.compareTo(this.dateadd) >= 0) this.datemod = datemod;
	}

	public boolean isNull() {
		return name.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Tag tag = (Tag) o;

		return name.equals(tag.name);

	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "Tag:" +
				       "\n\tID            :\t" + id +
				       "\n\tParent ID     :\t" + parentId +
				       "\n\tGroup Name    :\t" + (group == null ? "n/a" : group.getName()) +
				       "\n\tName          :\t" + name +
				       "\n\tColor         :\t" + color +
				       "\n\tAdded on      :\t" + dateadd +
				       "\n\tModified on   :\t" + datemod;
	}

	public void copyTo(Tag other) {
		other.id = this.id;
		other.parentId = this.parentId;
		other.group = this.group;
		other.name = this.name;
		other.color = this.color;
		other.dateadd.setTime(this.dateadd.getTime());
		other.datemod.setTime(this.datemod.getTime());
	}
}
