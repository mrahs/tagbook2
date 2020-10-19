/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datamodel;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import utils.D;
import utils.PhoneNumberValidator;
import utils.Utils;

import java.sql.Timestamp;
import java.util.*;

/**
 * Author: Anas H. Sulaiman 
 */
public class Item {
	long id;
	String name;
	String info;
	String ref; // null, not empty
	RefType refType;
	boolean privy;
	String username;
	String password;
	Set<Tag> tags;
	Timestamp dateadd;
	Timestamp datemod;
	Image image; // null

	/**
	 * Constructs a new Item with the following defaults:
	 * <pre>
	 *     id = -1;
	 *     all string attributes other that ref are empty
	 *     privy = false
	 *     dateadd = datemod = now
	 *     image = null
	 *     tags = new empty set
	 * </pre>
	 */
	public Item(String ref) {
		this(-1, "", ref);
	}

	/**
	 * Constructs a new Item with the following defaults:
	 * <pre>
	 *     id = -1;
	 *     all string attributes other than name and ref are empty
	 *     privy = false
	 *     dateadd = datemod = now
	 *     image = null
	 *     tags = new empty set
	 * </pre>
	 */
	public Item(String name, String ref) {
		this(-1, name, ref);
	}

	/**
	 * Constructs a new Item with the following defaults:
	 * <pre>
	 *     all string attributes other than name and ref are empty
	 *     privy = false
	 *     dateadd = datemod = now
	 *     image = null
	 *     tags = new empty set
	 * </pre>
	 */
	public Item(long id, String name, String ref) {
		this(id, name, "", ref, false, "", "", new LinkedHashSet<Tag>(), null);
	}


	/**
	 * Constructs a new Item with the following defaults:
	 * <pre>
	 *     dateadd = datemod = now
	 * </pre>
	 */
	public Item(long id, String name, String info, String ref, boolean privy, String username, String password, Collection<Tag> tags, Image image) {
		this(id, name, info, ref, privy, username, password, tags, image, Utils.getInstance().nowTs(), Utils.getInstance().nowTs());
	}

	/**
	 * Constructs a new Item with the following defaults:
	 * <pre>
	 *     id = -1
	 *     dateadd = datemod = now
	 * </pre>
	 */
	public Item(String name, String info, String ref, boolean privy, String username, String password, Collection<Tag> tags, Image image) {
		this(-1, name, info, ref, privy, username, password, tags, image, Utils.getInstance().nowTs(), Utils.getInstance().nowTs());
	}

	public Item(long id, String name, String info, String ref, boolean privy, String username, String password, Collection<Tag> tags, Image image, Timestamp dateadd, Timestamp datemod) {
		this.id = id;
		this.name = name.trim();
		this.info = info.trim();

		String s;
		this.ref = ref == null ? null : ((s = ref.trim()).isEmpty() ? null : s);
		D.checkValidation(validateRef());

		this.privy = privy;
		this.username = username.trim();
		this.password = Objects.requireNonNull(password);

		this.tags = new LinkedHashSet<>();
		this.tags.addAll(tags);

		this.image = image;

		this.dateadd = Objects.requireNonNull(dateadd);
		setDatemod(datemod);
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
		setDatemod(Utils.getInstance().nowTs());
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info.trim();
		setDatemod(Utils.getInstance().nowTs());
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		String s;
		this.ref = ref == null ? null : ((s = ref.trim()).isEmpty() ? null : s);
		D.checkValidation(validateRef());
		setDatemod(Utils.getInstance().nowTs());
	}

	private boolean validateRef() {
		// Order of statements is IMPORTANT here.
		if (ref == null) {
			this.refType = RefType.NULL;
			return true;
		} else if (UrlValidator.getInstance().isValid(ref)) {
			this.refType = RefType.URL;
			return true;
		} else if (EmailValidator.getInstance().isValid(ref)) {
			this.refType = RefType.EMAIL;
			return true;
		} else if (PhoneNumberValidator.getInstance().isValid(ref)) {
			this.refType = RefType.PHONE;
			return true;
		}
		return false;
	}

	public RefType getRefType() {
		return this.refType;
	}

	public boolean isPrivy() {
		return privy;
	}

	public void setPrivy(boolean state) {
		this.privy = state;
		setDatemod(Utils.getInstance().nowTs());
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username.trim();
		setDatemod(Utils.getInstance().nowTs());
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = Objects.requireNonNull(password);
		setDatemod(Utils.getInstance().nowTs());
	}

	/**
	 * @return an unmodifiable view of this item's tags collection.
	 */
	public Collection<Tag> getTags() {
		return Collections.unmodifiableCollection(this.tags);
	}

	/**
	 * This item's set of tags is cleared then all tags in the specified collection are added to this item's set of tags.
	 */
	public void setTags(Collection<Tag> tags) {
		Objects.requireNonNull(tags);
		this.tags.clear();
		this.tags.addAll(tags);
		setDatemod(Utils.getInstance().nowTs());
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

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		setDatemod(Utils.getInstance().nowTs());
	}

	public void addTag(Tag tag) {
		if (this.tags.add(tag)) setDatemod(Utils.getInstance().nowTs());
	}

	public void removeTag(Tag tag) {
		if (this.tags.remove(tag)) setDatemod(Utils.getInstance().nowTs());
	}

	public void removeTagByName(String tagName) {
		Tag toRemove = null;
		for (Tag t : this.tags) {
			if (t.name.equals(tagName)) {
				toRemove = t;
				break;
			}
		}
		if (toRemove != null) {
			this.tags.remove(toRemove);
			setDatemod(Utils.getInstance().nowTs());
		}
	}

	public void addTags(Tag... tags) {
		for (Tag t : tags) {
			addTag(t);
		}
	}

	public void removeTags(Tag... tags) {
		for (Tag t : tags) {
			removeTag(t);
		}
	}

	public void removeTagsByName(String... tNames) {
		for (String tName : tNames) {
			removeTagByName(tName);
		}
	}

	/**
	 * @param sep tags separator. if it's an empty string, this method will use comma "," as a separator
	 * @return a separated list of this item's tags or empty string if there isn't any
	 */
	public String getTagsAsString(String sep) {
		if (this.tags.isEmpty()) return "";
		if (sep.isEmpty()) sep = ",";
		StringBuilder tagsString = new StringBuilder();
		for (Tag tag : tags) {
			tagsString.append(tag.name).append(sep);
		}
		tagsString.delete(tagsString.length() - sep.length(), tagsString.length());
		return tagsString.toString();
	}

	public boolean isUrl() {
		return this.refType.equals(RefType.URL);
	}

	public boolean isEmail() {
		return this.refType.equals(RefType.EMAIL);
	}

	public boolean isPhone() {
		return this.refType.equals(RefType.PHONE);
	}

	public boolean isNull() {
		return ref == null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Item item = (Item) o;

		return ref == null ? item.ref == null : ref.equals(item.ref);
	}

	@Override
	public int hashCode() {
		return ref != null ? ref.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "Item:" +
				       "\n\tID          :\t" + id +
				       "\n\tName        :\t" + name +
				       "\n\tInfo        :\t" + info +
				       "\n\tRef.        :\t" + ref +
				       "\n\tRef. Type   :\t" + refType +
				       "\n\tPrivy       :\t" + (privy ? "Yes" : "No") +
				       "\n\tUsername    :\t" + username +
				       "\n\tPassword    :\t" + password +
				       "\n\tTags        :\t" + getTagsAsString(", ") +
				       "\n\tAdded on    :\t" + dateadd +
				       "\n\tModified on :\t" + datemod +
				       "\n\tHas image   :\t" + ((image == null) ? "No" : "Yes");
	}

	public void copyTo(Item other) {
		other.id = this.id;
		other.name = this.name;
		other.info = this.info;
		other.ref = this.ref;
		other.refType = this.refType;
		other.privy = this.privy;
		other.username = this.username;
		other.password = this.password;
		other.tags.clear();
		other.tags.addAll(this.tags);
		other.dateadd.setTime(this.dateadd.getTime());
		other.datemod.setTime(this.datemod.getTime());
		other.image = this.image;
	}

	public enum RefType {
		URL, EMAIL, PHONE, NULL
	}
}
