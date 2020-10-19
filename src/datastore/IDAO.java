/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datastore;

import datamodel.Item;
import datamodel.Tag;

import java.util.Collection;
import java.util.Map;

/**
 * Author: Anas H. Sulaiman 
 */
public interface IDAO extends AutoCloseable {

	// storage

	/**
	 * @return a string represents the path to the where data is stored.
	 */
	public String getPath();

	/**
	 * Opens the storage for read/write operations.
	 * Implementations may require the user to initialize some variables (e.g. encryption password).
	 *
	 * @param create create the file if it doesn't exist
	 * @return true if the storage is opened successfully, false otherwise.
	 */
	public boolean open(boolean create) throws Exception;

	/**
	 * @return true if the storage if currently open.
	 */
	public boolean isOpened();

	public void close() throws Exception;

	// working

	/**
	 * @return true if the there is an ongoing operation with the storage, false otherwise.
	 */
	public boolean isWorking();

	/**
	 * Stops whatever operation is ongoing with the storage. This may not have an immediate effect.
	 */
	public void cancel();


	// data

	public void backup(String path) throws Exception;

	public void restore(String path) throws Exception;

	public long importItems(String path) throws Exception;

	public long exportItems(String path) throws Exception;

	public boolean itemExist(String ref) throws Exception;

	/**
	 * @param ref  the reference field to validate upon
	 * @param item an Item instance to fill it with data if found
	 * @return true if an item with the same reference already exists, false otherwise.
	 */
	public boolean itemExist(String ref, Item item) throws Exception;

	public boolean tagExist(String name) throws Exception;

	public boolean tagExist(String name, Tag tag) throws Exception;

	public Item getItem(long id) throws Exception;

	public void loadAllItems(Collection<Item> items) throws Exception;

	public void loadAllItems(Collection<Item> items, long limit, long offset) throws Exception;

	public void loadItemsById(Collection<Item> items, long... ids) throws Exception;

	public void getItemTags(long id, Collection<Tag> tags) throws Exception;

	public int getItemTagCount(long id) throws Exception;

	/**
	 * Note: the item's id is updated to match the assigned one in the data store
	 *
	 * @return true if insert/update was successful; false if the item's ref. already exists
	 * @throws Exception
	 */
	public boolean addUpdateItem(Item item, boolean update) throws Exception;

	public long addUpdateItems(Collection<Item> items, boolean update) throws Exception;

	/**
	 * @return id of the inserted/updated tag
	 * @throws Exception
	 */
	public boolean addUpdateTag(Tag tag, boolean update) throws Exception;

	public long tagItems(long[] ids, Tag[] tags) throws Exception;

	public boolean removeItem(long id) throws Exception;

	public long removeItems(long... ids) throws Exception;

	public Tag getTag(long id) throws Exception;

	public void loadAllTags(Collection<Tag> tags) throws Exception;

	public void loadAllTags(Collection<Tag> tags, long limit, long offset) throws Exception;

	public void loadTagsById(Collection<Tag> tags, long... ids) throws Exception;

	public long getTagItemCount(Tag tag) throws Exception;

	public Map<Long, Long> getTagItemCounts(Collection<Tag> tags) throws Exception;

	public void loadItemTags(long id, Collection<Tag> tags) throws Exception;

	public boolean removeTag(long id) throws Exception;

	public boolean removeTagWithItems(long id) throws Exception;

	public long removeUnusedTags() throws Exception;

	public boolean replaceTag(long removeId, long keepId) throws Exception;

	public boolean replaceTag(long removeId, Tag tag) throws Exception;

	// stats
	public long getItemCount() throws Exception;

	public long getTagCount() throws Exception;

	public long getUnusedTagCount() throws Exception;

	public long getUntaggedItemCount() throws Exception;

	public Tag getMostUsedTag() throws Exception;

	public Item getMostTaggedItem() throws Exception;

	public Collection<Tag> getTop5Tags() throws Exception;

	// search

	public long searchItems(Collection<Item> items, String searchQuery);

	public long searchTags(Collection<Tag> tags, String searchQuery);
}
