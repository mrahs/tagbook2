/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.importers;

import datamodel.Field;
import datamodel.Item;
import datamodel.Tag;
import utils.Utils;

import java.util.*;

/**
 * Author: Anas H. Sulaiman 
 */
public class ImportConfigs {
	Collection<Tag> tags;
	Collection<Tag> collisionTags;
	Map<Field, Boolean> skipFields;
	Map<Field, String> defaults;
	Map<CollisionOption, Boolean> collisionOptions;

	public ImportConfigs() {
		tags = new LinkedHashSet<>();
		collisionTags = new LinkedHashSet<>();
		skipFields = new HashMap<>(9);    // cannot skip ref. field
		defaults = new HashMap<>(5);
		collisionOptions = new HashMap<>(8);

		for (Field f : Field.values()) {
			skipFields.put(f, false);
		}

		setDefaultName("");
		setDefaultInfo("");
		setDefaultPrivy(false);
		setDefaultUsername("");
		setDefaultPassword("");

		for (CollisionOption co : CollisionOption.values()) {
			setCollisionOption(co, false);
		}

		setCollisionOption(CollisionOption.skip, true);
	}

	public void setSkipField(Field f, boolean state) {
		if (f.equals(Field.ref)) return;
		skipFields.put(f, state);
	}

	public boolean getSkipField(Field f) {
		return skipFields.get(f);
	}

	public String getDefaultName() {
		return defaults.get(Field.name);
	}

	public void setDefaultName(String name) {
		defaults.put(Field.name, name);
	}

	public String getDefaultInfo() {
		return defaults.get(Field.info);
	}

	public void setDefaultInfo(String info) {
		defaults.put(Field.info, info);
	}

	public boolean getDefaultPrivy() {
		return Boolean.parseBoolean(defaults.get(Field.privy));
	}

	public void setDefaultPrivy(boolean state) {
		String s = state ? "TRUE" : "FALSE";
		defaults.put(Field.privy, s);
	}

	public String getDefaultUsername() {
		return defaults.get(Field.username);
	}

	public void setDefaultUsername(String value) {
		defaults.put(Field.username, value);
	}

	public String getDefaultPassword() {
		return defaults.get(Field.password);
	}

	public void setDefaultPassword(String value) {
		defaults.put(Field.password, value);
	}

	public void addTags(Tag... tags) {
		for (Tag t : tags) {
			if (t.isNull()) continue;
			this.tags.add(t);
		}
	}

	public void removeTags(Tag... tags) {
		for (Tag t : tags) {
			this.tags.remove(t);
		}
	}

	public void addCollisionTags(Tag... tags) {
		for (Tag t : tags) {
			if (t.isNull()) continue;
			this.collisionTags.add(t);
		}
	}

	public Collection<Tag> getTags() {
		return Collections.unmodifiableCollection(this.tags);
	}

	public Collection<Tag> getCollisionTags() {
		return Collections.unmodifiableCollection(this.collisionTags);
	}

	public void removeCollisionTags(Tag... tags) {
		for (Tag t : tags) {
			this.collisionTags.remove(t);
		}
	}

	public void setCollisionOption(CollisionOption co, boolean state) {
		if (state) switch (co) {
			case take_info_replace:
				collisionOptions.put(CollisionOption.take_info_replace, true);
				collisionOptions.put(CollisionOption.take_info_append, false);
				collisionOptions.put(CollisionOption.take_info_prepend, false);
				break;
			case take_info_append:
				collisionOptions.put(CollisionOption.take_info_replace, false);
				collisionOptions.put(CollisionOption.take_info_append, true);
				collisionOptions.put(CollisionOption.take_info_prepend, false);
				break;
			case take_info_prepend:
				collisionOptions.put(CollisionOption.take_info_replace, false);
				collisionOptions.put(CollisionOption.take_info_append, false);
				collisionOptions.put(CollisionOption.take_info_prepend, true);
				break;
			case take_tags_add:
				collisionOptions.put(CollisionOption.take_tags_add, true);
				collisionOptions.put(CollisionOption.take_tags_replace, false);
				break;
			case take_tags_replace:
				collisionOptions.put(CollisionOption.take_tags_add, false);
				collisionOptions.put(CollisionOption.take_tags_replace, true);
				break;
			default:
				collisionOptions.put(co, true);
		}
		else collisionOptions.put(co, state);
	}

	public boolean getCollisionOption(CollisionOption co) {
		return collisionOptions.get(co) && !collisionOptions.get(CollisionOption.skip);
	}

	public Item resolve(Item oldItem, Item newItem) {
		if (!oldItem.equals(newItem)) return newItem;

		oldItem.addTags(collisionTags.toArray(new Tag[0]));

		if (collisionOptions.get(CollisionOption.skip)) return oldItem;

		if (collisionOptions.get(CollisionOption.take_name)) oldItem.setName(newItem.getName());

		if (collisionOptions.get(CollisionOption.take_info_replace)) oldItem.setInfo(newItem.getInfo());

		if (collisionOptions.get(CollisionOption.take_info_append))
			oldItem.setInfo(oldItem.getInfo() + "\n" + newItem.getInfo());

		if (collisionOptions.get(CollisionOption.take_info_prepend))
			oldItem.setInfo(newItem.getInfo() + "\n" + oldItem.getInfo());

		if (collisionOptions.get(CollisionOption.take_privy)) oldItem.setPrivy(newItem.isPrivy());

		if (collisionOptions.get(CollisionOption.take_tags_replace)) oldItem.setTags(newItem.getTags());

		if (collisionOptions.get(CollisionOption.take_tags_add)) oldItem.addTags(newItem.getTags().toArray(new Tag[0]));

		if (collisionOptions.get(CollisionOption.take_username)) oldItem.setUsername(newItem.getUsername());

		if (collisionOptions.get(CollisionOption.take_password)) oldItem.setPassword(newItem.getPassword());

		if (collisionOptions.get(CollisionOption.take_image)) oldItem.setImage(newItem.getImage());

		return oldItem;
	}

	public void process(Item item) {
		if (skipFields.get(Field.name)) item.setName(getDefaultName());
		if (skipFields.get(Field.info)) item.setInfo(getDefaultInfo());
		if (skipFields.get(Field.privy)) item.setPrivy(getDefaultPrivy());
		if (skipFields.get(Field.username)) item.setUsername(getDefaultUsername());
		if (skipFields.get(Field.password)) item.setPassword(getDefaultPassword());
		if (skipFields.get(Field.dateadd)) item.setDateadd(Utils.getInstance().nowTs());
		if (skipFields.get(Field.datemod)) item.setDatemod(Utils.getInstance().nowTs());
		if (skipFields.get(Field.image)) item.setImage(null);
		if (skipFields.get(Field.tags)) item.setTags(new HashSet<Tag>());

		item.addTags(tags.toArray(new Tag[0]));
	}

	public enum CollisionOption {
		skip,
		take_name,
		take_info_replace, take_info_append, take_info_prepend,
		take_privy,
		take_tags_replace, take_tags_add,
		take_username,
		take_password,
		take_image
	}
}
