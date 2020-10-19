/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datastore;

import datamodel.Item;
import datamodel.Tag;
import datamodel.TagGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Author: Anas H. Sulaiman 
 */

// TODO
public class H2DAOTest {

	String filePath;
	IDAO dao;

	@Before
	public void setUp() throws Exception {
		filePath = System.getProperty("user.home") + File.separator + "tb2test";
		dao = new H2DAO(filePath);
		dao.open(true);
	}

	@After
	public void tearDown() throws Exception {
		dao.close();
		Files.deleteIfExists(Paths.get(dao.getPath()));
	}

	@Test
	public void testOpenDatabase() throws Exception {
		dao.close();

		assertTrue(dao.open(false));
	}

	@Test
	public void testAddItems() throws Exception {
		/**
		 * Covers:
		 *      addUpdateItem
		 *      itemExists
		 *      tagItems
		 */
		List<Item> items = (List<Item>) makeItems();
		Item item1, item2;
		assertTrue(dao.addUpdateItem((item1 = items.get(0)), false));
		assertTrue(dao.addUpdateItem((item2 = items.get(1)), false));

		assertTrue(dao.itemExist("http://ahs.pw"));
		assertTrue(dao.itemExist("http://google.com"));

		Item i1 = dao.getItem(item1.getId());
		Item i2 = dao.getItem(item2.getId());

		assertEquals(i1.getName(), item1.getName());
		assertEquals(i1.getInfo(), item1.getInfo());
		assertEquals(i1.getRef(), item1.getRef());
		assertEquals(i1.isPrivy(), item1.isPrivy());
		assertEquals(i1.getUsername(), item1.getUsername());
		assertEquals(i1.getPassword(), item1.getPassword());
		assertEquals(i1.getDateadd(), item1.getDateadd());
		assertEquals(i1.getTags().size(), item1.getTags().size());
		assertTrue(i1.getTags().containsAll(item1.getTags())); // equality check is causing problems due to the use of generic referencing (Collection<E>)
		assertEquals(i1.getImage(), item1.getImage());

		assertEquals(i2.getName(), item2.getName());
		assertEquals(i2.getInfo(), item2.getInfo());
		assertEquals(i2.getRef(), item2.getRef());
		assertEquals(i2.isPrivy(), item2.isPrivy());
		assertEquals(i2.getUsername(), item2.getUsername());
		assertEquals(i2.getPassword(), item2.getPassword());
		assertEquals(i2.getDateadd(), item2.getDateadd());
		assertEquals(i2.getTags().size(), item2.getTags().size());
		assertTrue(i2.getTags().containsAll(item2.getTags()));
		assertEquals(i2.getImage(), item2.getImage());

		i1.setId(-1);
		assertFalse(dao.addUpdateItem(i1, false));

		// add null ref.
		Item null1, null2;
		assertTrue(dao.addUpdateItem((null1 = new Item("null1", "")), false));
		assertTrue(dao.addUpdateItem((null2 = new Item("null2", "")), false));

		null1 = dao.getItem(null1.getId());
		null2 = dao.getItem(null2.getId());

		assertEquals(null1.getName(), "null1");
		assertEquals(null2.getName(), "null2");
	}

	private Collection<Tag> makeTags() {
		Collection<Tag> tags = new LinkedHashSet<>(5);
		TagGroup tg = new TagGroup();
		tg.setName("TG1");
		tags.add(new Tag("link", "#555", null, tg));
		tags.add(new Tag("net", "#555", 1L, tg));
		tags.add(new Tag("web", "#555", 2L, tg));
		tags.add(new Tag("personal"));
		tags.add(new Tag("todo"));
		return tags;
	}

	private Collection<Item> makeItems() {
		Collection<Item> items = new ArrayList<>(2);
		Collection<Tag> tags = makeTags();
		items.add(new Item("AHS", "AHS Personal Website", "http://ahs.pw", false, "ahs", "asdf", tags, null));
		items.add(new Item("Google", "Google Search", "http://google.com", false, "google", "googleasdf", tags, null));
		return items;
	}

	@Test
	public void testBackupRestore() throws Exception {
		/**
		 * Covers:
		 *      addUpdateItems
		 *      tagItems
		 *      backup
		 *      restore
		 *      itemExists
		 */
		Collection<Item> items = makeItems();
		dao.addUpdateItems(items, false);

		String backupPath = System.getProperty("user.home") + File.separator + "tb2bkup.tbk";
		dao.backup(backupPath);
		dao.restore(backupPath);

		assertTrue(dao.itemExist("http://ahs.pw"));
		assertTrue(dao.itemExist("http://google.com"));

		Files.deleteIfExists(Paths.get(backupPath));
	}

	@Test
	public void testLoadingItems() throws Exception {
		/**
		 * Covers:
		 *      addUpdateItems
		 *      getItem
		 *      loadAllItems x2
		 *      loadItemsById x2
		 */
		dao.addUpdateItems(makeItems(), false);

		Item i = dao.getItem(1);
		assertFalse(i.isNull());

		Collection<Item> items = new ArrayList<>(2);
		dao.loadAllItems(items);
		assertTrue(items.size() == 2);

		items.clear();
		dao.loadAllItems(items, 1, 0);
		assertTrue(items.size() == 1);

		items.clear();
		dao.loadAllItems(items, 1, 1);
		assertTrue(items.size() == 1);

		items.clear();
		dao.loadItemsById(items, 1, 2);
		assertTrue(items.size() == 2);
	}

	@Test
	public void testUpdatingItems() throws Exception {
		/**
		 * Covers:
		 *      addUpdateItems
		 *      addUpdateItem
		 *      getItem
		 */
		List<Item> items = (List<Item>) makeItems();
		dao.addUpdateItems(items, false);

		items.get(0).setName("AHS2");
		assertTrue(dao.addUpdateItem(items.get(0), true));
		Item i = dao.getItem(1);
		assertEquals(i.getName(), "AHS2");

		i.setRef("http://google.com");
		assertFalse(dao.addUpdateItem(i, true));

		// update null ref
	}

	@Test
	public void testRemovingItems() throws Exception {
		/**
		 * Covers:
		 *      addUpdateItems
		 *      removeItem
		 *      itemExists
		 */
		dao.addUpdateItems(makeItems(), false);

		assertTrue(dao.removeItem(1));
		assertFalse(dao.itemExist("http://ahs.pw"));

		assertTrue(dao.removeItem(2));
		assertFalse(dao.itemExist("http://google.com"));

		dao.addUpdateItems(makeItems(), false);

		assertTrue(dao.removeItem(3));
		assertTrue(dao.removeItem(4));

		assertFalse(dao.itemExist("http://ahs.pw"));
		assertFalse(dao.itemExist("http://google.com"));
	}

	@Test
	public void testReplacingTag() throws Exception {
		/**
		 * Covers:
		 *      addUpdateItems
		 *      replaceTag
		 *      getTag
		 *      getTagItemCount
		 */
		dao.addUpdateItems(makeItems(), false);
		Tag t = dao.getTag(1);
		String tname = t.getName();

		Tag t2 = new Tag("replaced", "#444", null, null);

		long c = dao.getTagItemCount(t);

		dao.replaceTag(t.getId(), t2);

		assertTrue(dao.getTag(t.getId()).isNull());
		assertFalse(dao.tagExist(tname));

		assertEquals(c, dao.getTagItemCount(t2));
	}

	@Test
	public void testStats() throws Exception {
		/**
		 * Covers:
		 *      getItemsCount
		 *      getTagsCount
		 *      getUnusedTagCount
		 *      getUntaggedItemCount
		 *      getMostUsedTag
		 *      getMostTaggedItem
		 *      getTop5Tags
		 *      getItemTagCount
		 *      getTagItemCount
		 */

		dao.addUpdateItems(makeItems(), false);

		assertEquals(2, dao.getItemCount());
		assertEquals(5, dao.getTagCount());
		assertEquals(0, dao.getUnusedTagCount());
		assertEquals(0, dao.getUntaggedItemCount());
		assertEquals(5, dao.getItemTagCount(1));
		assertEquals(2, dao.getTagItemCount(dao.getTag(1)));
		Collection<Tag> tags = dao.getTop5Tags();
		assertEquals(5, tags.size());
		assertFalse(dao.getMostTaggedItem().isNull());
		assertFalse(dao.getMostUsedTag().isNull());
	}

	@Test
	public void testLoadingTags() throws Exception {
		/**
		 * Covers:
		 *      tagExist
		 *      loadAllTags
		 *      loadTagsById
		 *      getTag
		 *      getItemTags
		 */

		dao.addUpdateItems(makeItems(), false);

		assertTrue(dao.tagExist("link"));

		Collection<Tag> tags = new LinkedHashSet<>(5);

		dao.loadAllTags(tags);
		assertEquals(5, tags.size());

		tags.clear();
		dao.loadAllTags(tags, 2, 3);
		assertEquals(2, tags.size());

		tags.clear();
		dao.loadTagsById(tags, 2, 3, 4);
		assertEquals(3, tags.size());

		assertFalse(dao.getTag(4).isNull());

		tags.clear();
		dao.getItemTags(1, tags);
		assertEquals(5, tags.size());
	}

	@Test
	public void testRemovingTag() throws Exception {
		/**
		 * Covers:
		 *      addUpdateItems
		 *      removeTag
		 *      removeTagWithItems
		 *      removeUnusedTags
		 *      getItemCount
		 *      getTagCount
		 */
		dao.addUpdateItems(makeItems(), false);

		Tag t = dao.getTag(1);

		assertTrue(dao.removeTag(1));
		assertFalse(dao.tagExist(t.getName()));

		assertTrue(dao.removeTagWithItems(2));
		assertEquals(0, dao.getItemCount());

		assertEquals(3, dao.removeUnusedTags());
	}

	@Test
	public void testUpdatingTag() throws Exception {
		dao.addUpdateItems(makeItems(), false);

		Tag t = dao.getTag(1);
		String tname = t.getName();
		t.setName("lol");
		assertTrue(dao.addUpdateTag(t, true));

		assertTrue(dao.tagExist("lol"));
		assertFalse(dao.tagExist(tname));
	}

	@Test
	public void testSearchItems() throws Exception {

	}

	@Test
	public void testSearchTags() throws Exception {

	}

	@Test
	public void testImport() throws Exception {

	}

	@Test
	public void testExport() throws Exception {

	}
}
