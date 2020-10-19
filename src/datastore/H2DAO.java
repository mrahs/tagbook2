/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datastore;

import datamodel.Image;
import datamodel.Item;
import datamodel.Tag;
import datamodel.TagGroup;
import utils.D;
import utils.IOUtils;
import utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: Anas H. Sulaiman 
 */
public class H2DAO implements IDAO {
	private static final String DRIVER = "org.h2.Driver";
	private static final String DB_USER = "TagBook2";
	private static final String DB_PASS = "t49_B00k2;&";
	private static final String BK_PASS = "t49_b00k2Bu;&";
	private static final String DB_FILE_NAME = "tagbook2";
	private static final String DB_FILE_EXT = "h2.db";
	private static final String TEMP_DIR_SUFFIX = "_tb2";
	private final String filePath;
	private final AtomicBoolean cancel;
	private final AtomicBoolean working;
	private Connection con;
	private String tempDirPath;
	private String h2dbPath;
	private Utils u = Utils.getInstance();
	private IOUtils io = IOUtils.getInstance();
	private Map<String, String> sql;

	public H2DAO(String path) throws Exception {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			throwException("missing.driver", e);
		}
		loadSql();
		this.cancel = new AtomicBoolean(false);
		this.working = new AtomicBoolean(false);
		con = null;
		this.filePath = io.getAbsNormPath(path);
		this.tempDirPath = this.h2dbPath = "";
	}

	private void loadSql() {
		Scanner scan = new Scanner(getClass().getResourceAsStream("/datastore/sql.sql"), "UTF-8");
		this.sql = new HashMap<>();
		while (scan.hasNext()) {
			String line = scan.nextLine().trim();
			if (line.startsWith("--")) {
				String key = line.substring(2);
				StringBuilder stmnt = new StringBuilder();
				while (!(line = scan.nextLine()).startsWith("--end")) {
					stmnt.append(line).append("\n");
				}
				this.sql.put(key, stmnt.toString());
			}
		}
		scan.close();
	}

	@Override
	public String getPath() {
		return this.filePath;
	}

	private void connect(String dbUrl) throws SQLException {
		con = DriverManager.getConnection(dbUrl, DB_USER, DB_PASS);
		con.setAutoCommit(true);
	}

	private boolean verifyDb() throws SQLException {
		DatabaseMetaData dbmt = con.getMetaData();
		ResultSet rs = dbmt.getTables(null, null, "%", new String[]{"TABLE"});
		if (!rs.isBeforeFirst()) return false;
		boolean tableFound[] = {false, false, false, false, false};
		boolean foundAll = false;
		while (rs.next() && !foundAll) {
			String tableName = rs.getString("TABLE_NAME");
			if (tableName.equalsIgnoreCase("item")) tableFound[0] = true;
			else if (tableName.equalsIgnoreCase("tag")) tableFound[1] = true;
			else if (tableName.equalsIgnoreCase("tag_group")) tableFound[2] = true;
			else if (tableName.equalsIgnoreCase("tag_item")) tableFound[3] = true;
			else if (tableName.equalsIgnoreCase("image")) tableFound[4] = true;
			foundAll = tableFound[0] && tableFound[1] && tableFound[2] && tableFound[3] && tableFound[4];
		}

		rs.close();
		return foundAll;
	}

	@Override
	public boolean open(boolean create) throws Exception {
	    /*
	    Case A: Open
            1. Create temporary hidden directory
            2. Unzip file content inside the aforementioned directory
            3. Prepare H2 database file path and connect to database
            4. Verify database
        Case B: Create
            1. Create temporary hidden directory
            2. Prepare H2 database file path and connect to database
            3. Initialize database
         */

		// Step 1: Create temporary hidden directory
		try {
			this.tempDirPath = io.createHiddenDir(this.filePath + TEMP_DIR_SUFFIX);
		} catch (IOException e) {
			throwException("err.db.open", e);
		}

		// Step 2.A: Unzip file
		try {
			if (!create) {
				io.unzip(this.filePath, this.tempDirPath, true);
			}
		} catch (IOException e) {
			io.deleteFile(this.tempDirPath);
			throwException("err.db.open", e);
		}

		// Step 3.A(2.B): Prepare H2 database file path and connect to database
		this.h2dbPath = this.tempDirPath + u.NAME_SEPARATOR + DB_FILE_NAME;
		String dbUrl = "jdbc:h2:" + this.h2dbPath + ";TRACE_LEVEL_FILE=0" + (create ? "" : ";IFEXISTS=TRUE");
		try {
			connect(dbUrl);
		} catch (SQLException e) {
			io.deleteFile(this.tempDirPath);
			throwException("err.db.open", e);
		}

		// Step 4.A/3.B: Verify database / Initialize database
		try {
			if (create) {
				try (PreparedStatement ps = con.prepareStatement(sql.get("create_db"))) {
					ps.execute();
				}
			} else if (!verifyDb()) {   // opened but not a database of our own
				close();
				return false;
			}
		} catch (SQLException e) {
			close();
			return false;
		}
		return true;
	}

	@Override
	public boolean isOpened() {
		return con != null;
	}

	@Override
	public void close() throws Exception {
		if (!isOpened()) return;

        /*
        1. Close database connection
        2. Zip database file (and setting file)
        3. Delete temporary directory
         */

		try {
			con.close();
		} catch (SQLException e) {
			// ignored because we might still be able to zip files
		}

		try {
			io.zipFiles(true, filePath, h2dbPath + "." + DB_FILE_EXT);
		} catch (IOException e) {
			// this exception will be thrown because we don't wanna delete the files if we cannot save them
			throwException("err.db.open", e);
		}

		try {
			io.deleteFile(this.tempDirPath);
		} catch (IOException e) {
			// ignored because we were able to save the files and we need to reinitialize
		}

		this.tempDirPath = "";
		this.h2dbPath = "";
		con = null;
	}

	@Override
	public boolean isWorking() {
		return working.get();
	}

	private void start() {
		working.set(true);
	}

	private void finish() {
		working.set(false);
		cancel.set(false);
	}

	@Override
	public void cancel() {
		cancel.set(true);
	}

	@Override
	public void backup(String path) throws Exception {
		Objects.requireNonNull(path);
		try (PreparedStatement ps = con.prepareStatement(sql.get("backup_db"))) {
			ps.setString(1, path);
			ps.setString(2, BK_PASS);
			ps.execute();
		} catch (SQLException e) {
			throwException("err.backup", e);
		}
	}

	@Override
	public void restore(String path) throws Exception {
		Objects.requireNonNull(path);
		try (PreparedStatement psClear = con.prepareStatement(sql.get("clear_db"));
		     PreparedStatement psRestore = con.prepareStatement(sql.get("restore_db"))) {
			psRestore.setString(1, path);
			psRestore.setString(2, BK_PASS);
			psClear.execute();
			psRestore.execute();
		} catch (SQLException e) {
			throwException("err.backup", e);
		}
	}

	@Override
	public long importItems(String path) throws Exception {
		return 0; // TODO
	}

	@Override
	public long exportItems(String path) throws Exception {
		return 0;  // TODO
	}

	@Override
	public boolean itemExist(String ref) throws Exception {
		Objects.requireNonNull(ref);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_item_by_ref"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setString(1, ref);
			ResultSet rs = ps.executeQuery();
			boolean found = rs.isBeforeFirst();
			rs.close();
			return found;
		} catch (SQLException e) {
			throwException("err.db.access", e);
		}
		return false;
	}

	@Override
	public boolean itemExist(String ref, Item item) throws Exception {
		Objects.requireNonNull(ref);
		Objects.requireNonNull(item);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_item_by_ref"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setString(1, ref);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) return false;
			rs.next();
			Item i = fetchItem(rs);
			i.copyTo(item);
			rs.close();
			return true;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false; // will not get here
		}
	}

	public boolean tagExist(String name) throws Exception {
		Objects.requireNonNull(name);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_tag_by_name"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			boolean found = rs.isBeforeFirst();
			rs.close();
			return found;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false;
		}
	}

	public boolean tagExist(String name, Tag tag) throws Exception {
		Objects.requireNonNull(name);
		Objects.requireNonNull(tag);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_tag_by_name"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) return false;
			rs.next();
			Tag t = fetchTag(rs);
			t.copyTo(tag);
			rs.close();
			return true;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false;
		}
	}

	@Override
	public Item getItem(long id) throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_item_by_id"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) return new Item(null);
			rs.next();
			Item i = fetchItem(rs);
			rs.close();
			return i;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return null; // will not get here
		}
	}

	@Override
	public void loadAllItems(Collection<Item> items) throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_all_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			fetchItems(rs, items);
			rs.close();
		} catch (SQLException e) {
			finish();
			throwException("err.db.access", e);
		}
	}

	@Override
	public void loadAllItems(Collection<Item> items, long limit, long offset) throws Exception {
		D.checkPositive(limit, offset);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_all_items_2"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, limit);
			ps.setLong(2, offset);
			ResultSet rs = ps.executeQuery();
			fetchItems(rs, items);
			rs.close();
		} catch (SQLException e) {
			finish();
			throwException("err.db.access", e);
		}
	}

	@Override
	public void loadItemsById(Collection<Item> items, long... ids) throws Exception {
		// prepare ids list
		String stmnt = sql.get("select_items_by_id").replace("CSV", longArr2CsvString(ids));

		if (cancel.get()) return;

		try (Statement s = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = s.executeQuery(stmnt);
			fetchItems(rs, items);
			rs.close();
		} catch (SQLException e) {
			finish();
			throwException("err.db.access", e);
		}
	}

	public void getItemTags(long id, Collection<Tag> tags) throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_item_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {
				rs.close();
				return;
			}
			while (rs.next()) {
				tags.add(fetchTag(rs));
			}
			rs.close();
		} catch (SQLException e) {
			throwException("err.db.access", e);
		}
	}

	@Override
	public int getItemTagCount(long id) throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("count_item_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			int count =  (int) fetchCount(rs);
			rs.close();
			return count;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return 0; // will not get here
		}
	}

	@Override
	public boolean addUpdateItem(Item item, boolean update) throws Exception {
		// insert image if exists
		Image img;
		if ((img = item.getImage()) != null && !img.isNull()) {
			try (PreparedStatement ps = con.prepareStatement(sql.get("insert_image"),  ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,PreparedStatement.RETURN_GENERATED_KEYS)) {
				ps.setBinaryStream(1, new ByteArrayInputStream(img.getData()));
				ps.setLong(2, img.getId());
				ps.executeUpdate();

				ResultSet rs = ps.getGeneratedKeys();
				if (rs.isBeforeFirst()) {
					rs.next();
					item.getImage().setId(rs.getLong(1));
				}
				rs.close();
			} catch (SQLException e) {
				throwException("err.db.access", e);
			}
		}

		// insert item
		try (PreparedStatement ps = con.prepareStatement(update ? sql.get("update_item") : sql.get("insert_item"),  ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,PreparedStatement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, item.getName());
			ps.setString(2, item.getInfo());
			if (item.getRef() == null) ps.setNull(3, Types.VARCHAR);
			else ps.setString(3, item.getRef());
			ps.setBoolean(4, item.isPrivy());
			ps.setString(5, item.getUsername());
			ps.setString(6, item.getPassword());
			ps.setTimestamp(7, item.getDateadd());
			ps.setTimestamp(8, item.getDatemod());
			if (item.getImage() != null) ps.setLong(9, item.getImage().getId());
			else ps.setNull(9, Types.BIGINT);

			if (update) {
				ps.setLong(10, item.getId());
				if (item.getRef() == null) ps.setNull(11, Types.VARCHAR);
				else ps.setString(11, item.getRef());
				ps.setLong(12, item.getId());
			}

			int ar = ps.executeUpdate(); // affected rows

			if (update) {
				if (ar == 0) {
					// update rejected (updated ref. already exists)
					return false;
				} else {
					PreparedStatement ps2 = con.prepareStatement(sql.get("remove_item_tags"));
					ps2.setLong(1, item.getId());
					ps2.execute();
					ps2.close();
				}
			} else {
				if (ar == 0) {
					// nothing was inserted (already exists)
					return false;
				} else {
					ResultSet rs = ps.getGeneratedKeys();
					rs.next();
					item.setId(rs.getLong(1));
					rs.close();
				}
			}

			// insert tags
			tagItems(new long[]{item.getId()}, item.getTags().toArray(new Tag[item.getTags().size()]));
			return true;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false; // will not get here
		}
	}

	@Override
	public long addUpdateItems(Collection<Item> items, boolean update) throws Exception {
		start();
		Iterator<Item> itr = items.iterator();
		boolean commitState = con.getAutoCommit();
		con.setAutoCommit(false);
		int i = 0;
		for (; i < items.size(); ++i) {
			addUpdateItem(itr.next(), update);
			if (cancel.get()) break;
		}
		con.commit();
		con.setAutoCommit(commitState);
		finish();
		return i;
	}

	@Override
	public boolean addUpdateTag(Tag tag, boolean update) throws Exception {
		if (tag.isNull()) return false;

		// insert tag group if exists
		TagGroup tg;
		if ((tg = tag.getGroup()) != null && !tg.isNull()) {
			try (PreparedStatement ps = con.prepareStatement(sql.get("insert_tag_group"),  ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,PreparedStatement.RETURN_GENERATED_KEYS)) {
				ps.setString(1, tg.getName());
				ps.executeUpdate();

				ResultSet rs = ps.getGeneratedKeys();
				if (rs.isBeforeFirst()) {
					rs.next();
					tag.getGroup().setId(rs.getLong(1));
				}
				rs.close();
			} catch (SQLException e) {
				throwException("err.db.access", e);
			}
		}

		// insert tag
		try (PreparedStatement psTag = con.prepareStatement(update ? sql.get("update_tag") : sql.get("insert_tag"),  ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,PreparedStatement.RETURN_GENERATED_KEYS)) {
			psTag.setString(1, tag.getName());
			psTag.setString(2, tag.getColor());
			psTag.setTimestamp(3, tag.getDateadd());
			psTag.setTimestamp(4, tag.getDatemod());
			if (tag.getParentId() == null) psTag.setNull(5, Types.BIGINT);
			else psTag.setLong(5, tag.getParentId());
			if (tag.getGroup() == null) psTag.setNull(6, Types.BIGINT);
			else psTag.setLong(6, tag.getGroup().getId());

			if (update) {
				// update
				psTag.setLong(7, tag.getId());
				psTag.setString(8, tag.getName());
				psTag.setLong(9, tag.getId());
			}

			int ar = psTag.executeUpdate(); // affected rows

			if (update) {
				if (ar == 0) {
					// update rejected (updated name already exists)
					return false;
				}
			} else {
				if (ar == 0) {
					// nothing was inserted (already exists)
					return false;
				} else {
					ResultSet rs = psTag.getGeneratedKeys();
					rs.next();
					tag.setId(rs.getLong(1));
					rs.close();
				}
			}
			return true;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false; // will not get here
		}
	}

	@Override
	public long tagItems(long[] ids, Tag[] tags) throws Exception {
		if (ids.length == 0 || tags.length == 0) return 0;
		boolean commitState = con.getAutoCommit();
		con.setAutoCommit(false);
		try (PreparedStatement psMap = con.prepareStatement(sql.get("insert_tag_item_map"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			start();
			for (int i = 0; i < ids.length; ++i) {
				for (Tag t : tags) {
					addUpdateTag(t, false);
					psMap.setLong(1, ids[i]);
					psMap.setString(2, t.getName());
					psMap.addBatch();
					if (cancel.get()) {
						psMap.executeBatch();
						finish();
						return i;
					}
				}
			}
			psMap.executeBatch();
		} catch (SQLException e) {
			finish();
			throwException("err.db.access", e);
		} finally {
			con.commit();
			con.setAutoCommit(commitState);
		}
		finish();
		return ids.length;
	}

	@Override
	public boolean removeItem(long id) throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("remove_item"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false; // will not get here
		}
	}

	@Override
	public long removeItems(long... ids) throws Exception {
		String stmnt = sql.get("remove_items").replace("CSV", longArr2CsvString(ids));

		try (Statement s = con.createStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			return s.executeUpdate(stmnt);
		}
	}

	@Override
	public Tag getTag(long id) throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_tag_by_id"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return new Tag("");}
			rs.next();
			Tag t =  fetchTag(rs);
			rs.close();
			return t;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return null; // will not get here
		}
	}

	@Override
	public void loadAllTags(Collection<Tag> tags) throws Exception {
		Objects.requireNonNull(tags);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return;}
			start();
			while (rs.next()) {
				tags.add(fetchTag(rs));
				if (cancel.get()) break;
			}
			finish();
			rs.close();
		} catch (SQLException e) {
			throwException("err.db.access", e);
		}
	}

	@Override
	public void loadAllTags(Collection<Tag> tags, long limit, long offset) throws Exception {
		Objects.requireNonNull(tags);
		D.checkPositive(limit, offset);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_tags_2"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, limit);
			ps.setLong(2, offset);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return;}
			start();
			while (rs.next()) {
				tags.add(fetchTag(rs));
				if (cancel.get()) break;
			}
			finish();
			rs.close();
		} catch (SQLException e) {
			throwException("err.db.access", e);
		}
	}

	@Override
	public void loadTagsById(Collection<Tag> tags, long... ids) throws Exception {
		Objects.requireNonNull(tags);
		Objects.requireNonNull(ids);
		if (ids.length == 0) return;

		try (PreparedStatement ps = con.prepareStatement(sql.get("select_tags_by_id").replace("CSV", longArr2CsvString(ids)), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return;}
			start();
			while (rs.next()) {
				tags.add(fetchTag(rs));
				if (cancel.get()) break;
			}
			finish();
			rs.close();
		} catch (SQLException e) {
			finish();
			throwException("err.db.access", e);
		}
	}

	@Override
	public long getTagItemCount(Tag tag) throws Exception {
		Objects.requireNonNull(tag);
		try (PreparedStatement ps = con.prepareStatement(sql.get("count_tag_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, tag.getId());
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return 0;}
			rs.next();
			long count =  rs.getLong(1);
			rs.close();
			return count;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return 0; // will not get here
		}
	}

	@Override
	public Map<Long, Long> getTagItemCounts(Collection<Tag> tags) throws Exception {
		Objects.requireNonNull(tags);
		Map<Long, Long> counts = new LinkedHashMap<>(tags.size());
		start();
		for (Tag t : tags) {
			counts.put(t.getId(), getTagItemCount(t));
			if (cancel.get()) break;
		}
		finish();
		return counts;
	}

	@Override
	public void loadItemTags(long id, Collection<Tag> tags) throws Exception {
		Objects.requireNonNull(tags);
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_item_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return;}
			start();
			while (rs.next()) {
				tags.add(fetchTag(rs));
				if (cancel.get()) break;
			}
			finish();
			rs.close();
		} catch (SQLException e) {
			finish();
			throwException("err.db.access", e);
		}
	}

	@Override
	public boolean removeTag(long id) throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("remove_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false; // will not get here
		}
	}

	@Override
	public boolean removeTagWithItems(long id) throws Exception {
		try (PreparedStatement psItems = con.prepareStatement(sql.get("remove_tag_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		     PreparedStatement psTag = con.prepareStatement(sql.get("remove_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			psItems.setLong(1, id);
			psItems.execute();
			psTag.setLong(1, id);
			psTag.execute();
			return true;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false; // will not get here
		}
	}

	@Override
	public long removeUnusedTags() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("remove_unused_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			return ps.executeUpdate();
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return 0; // will not get here
		}
	}

	@Override
	public boolean replaceTag(long removeId, long keepId) throws Exception {
		try (PreparedStatement psRep = con.prepareStatement(sql.get("replace_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		     PreparedStatement psDel = con.prepareStatement(sql.get("remove_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			psRep.setLong(1, keepId);
			psRep.setLong(2, removeId);
			psRep.execute();
			psDel.setLong(1, removeId);
			psDel.execute();
			return true;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return false; // will not get here
		}

	}

	@Override
	public boolean replaceTag(long removeId, Tag tag) throws Exception {
		Objects.requireNonNull(tag);
		addUpdateTag(tag, false);
		long keepId = tag.getId();
		if (keepId == 0) keepId = tag.getId();
		replaceTag(removeId, keepId);
		return true;
	}

	@Override
	public long getItemCount() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("count_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			long count = fetchCount(rs);
			rs.close();
			return count;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return 0; // will not get here
		}
	}

	@Override
	public long getTagCount() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("count_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			long count = fetchCount(rs);
			rs.close();
			return count;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return 0; // will not get here
		}
	}

	@Override
	public long getUnusedTagCount() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("count_unused_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			long count = fetchCount(rs);
			rs.close();
			return count;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return 0; // will not get here
		}
	}

	@Override
	public long getUntaggedItemCount() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("count_untagged_items"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			long count = fetchCount(rs);
			rs.close();
			return count;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return 0; // will not get here
		}
	}

	@Override
	public Tag getMostUsedTag() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_most_used_tag"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return new Tag("");}
			rs.next();
			Tag t = fetchTag(rs);
			rs.close();
			return t;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return null; // will not get here
		}
	}

	@Override
	public Item getMostTaggedItem() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_most_tagged_item"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return new Item(null);}
			rs.next();
			Item i = fetchItem(rs);
			rs.close();
			return i;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return null; // will not get here
		}
	}

	@Override
	public Collection<Tag> getTop5Tags() throws Exception {
		try (PreparedStatement ps = con.prepareStatement(sql.get("select_most_5_tags"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {rs.close();return new HashSet<>();}
			Collection<Tag> top = new LinkedHashSet<>(5);
			while (rs.next()) {
				top.add(fetchTag(rs));
			}
			rs.close();
			return top;
		} catch (SQLException e) {
			throwException("err.db.access", e);
			return null; // will not get here
		}
	}

	@Override
	public long searchItems(Collection<Item> items, String searchQuery) {
		return 0;  // TODO
	}

	@Override
	public long searchTags(Collection<Tag> tags, String searchQuery) {
		return 0;  // TODO
	}

	private Item fetchItem(ResultSet rs) throws SQLException {
		long imageId = rs.getLong("imageid");
		Image img;
		if (imageId == 0) img = null;
		else {
			img = Image.getIfExists(imageId);
			if (img == null) {
				PreparedStatement ps2 = con.prepareStatement(sql.get("select_image_by_id"), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				ps2.setLong(1, imageId);
				ResultSet rs2 = ps2.executeQuery();
				if (rs2.isBeforeFirst()) {
					rs2.next();
					img = new Image();
					img.setId(rs2.getLong("id"));
					img.setData(rs2.getBytes("data"));
				}
				rs2.close();
				ps2.close();
			}
		}

		long id = rs.getLong("id");
		Collection<Tag> tags = new HashSet<>();
		try {
			getItemTags(id, tags);
		} catch (Exception e) {
			throw (SQLException) e.getCause();
		}

		return new Item(rs.getLong("id"), rs.getString("name"), rs.getString("info"), rs.getString("ref"), rs.getBoolean("privy"), rs.getString("username"), rs.getString("password"), tags, img, rs.getTimestamp("dateadd"), rs.getTimestamp("datemod"));
	}

	private Tag fetchTag(ResultSet rs) throws SQLException {
		long id = rs.getLong("id");
		Tag t = Tag.getIfExists(id);
		if (t != null) return t;

		long groupId = rs.getLong("groupid");
		TagGroup tg;
		if (groupId == 0) tg = null;
		else {
			tg = TagGroup.getIfExists(groupId);
			if (tg == null) {
				PreparedStatement ps = con.prepareStatement(sql.get("select_tag_group_by_id"));
				ps.setLong(1, groupId);
				ResultSet rs2 = ps.executeQuery();

				if (rs2.isBeforeFirst()) {
					rs2.next();
					tg = new TagGroup();
					tg.setId(rs2.getLong("id"));
					tg.setName(rs2.getString("name"));
				}
				rs2.close();
				ps.close();
			}
		}
		long parentId = rs.getLong("parentid");
		Long pid = parentId == 0 ? null : parentId;
		Tag tag =  new Tag(id, rs.getString("name"), rs.getString("color"), pid, tg, rs.getTimestamp("dateadd"), rs.getTimestamp("datemod"));
		rs.close();
		return tag;
	}

	private void fetchItems(ResultSet rs, Collection<Item> items) throws SQLException {
		if (!rs.isBeforeFirst()) return;
		start();
		while (rs.next()) {
			items.add(fetchItem(rs));
			if (cancel.get()) break;
		}
		finish();
		rs.close();
	}

	private long fetchCount(ResultSet rs) throws SQLException {
		if (!rs.isBeforeFirst()) return 0;
		rs.next();
		return rs.getLong(1);
	}

	private String longArr2CsvString(long[] arr) {
		StringBuilder str = new StringBuilder();
		for (Long id : arr) {
			str.append(id).append(",");
		}
		str.deleteCharAt(str.length() - 1);
		return str.toString();
	}

	private void throwException(String msgKey, Throwable cause) throws Exception {
		throw new Exception(u.i18n().getString(msgKey), cause);
	}
}
