/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
*           All Rights Reserved.
*/

--create_db
--create_table_image
CREATE TABLE image(
    id IDENTITY(1) NOT NULL UNIQUE,
    data BINARY
);

--create_table_tag_group
CREATE TABLE tag_group(
    id IDENTITY(1) NOT NULL UNIQUE,
    name VARCHAR NOT NULL UNIQUE
);

--create_table_item
CREATE TABLE item(
    id IDENTITY(1) NOT NULL UNIQUE,
    name VARCHAR,
    info VARCHAR,
    ref VARCHAR UNIQUE,
    privy BOOLEAN DEFAULT false NOT NULL NULL_TO_DEFAULT,
    username VARCHAR,
    password VARCHAR,
    dateadd TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
    datemod TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
    imageid BIGINT REFERENCES image(id) ON DELETE SET NULL
);

--create_table_tag
CREATE TABLE tag(
    id IDENTITY(1) NOT NULL UNIQUE,
    parentid BIGINT REFERENCES tag(id) ON DELETE SET NULL,
    groupid BIGINT REFERENCES tag_group(id) ON DELETE SET NULL,
    name VARCHAR NOT NULL UNIQUE,
    color VARCHAR,
    dateadd TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
    datemod TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
    CHECK (id <> parentid)
);

--create_table_tag_item
CREATE TABLE tag_item(
    itemid BIGINT REFERENCES item(id) ON DELETE CASCADE ON UPDATE CASCADE,
    tagid BIGINT REFERENCES tag(id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (itemid, tagid),
    UNIQUE (itemid, tagid)
);
--end

--clear_db
DROP ALL OBJECTS;
--end

--backup_db
SCRIPT TO ? COMPRESSION DEFLATE CIPHER AES PASSWORD ? CHARSET 'UTF-8';
--end

--restore_db
RUNSCRIPT FROM ? COMPRESSION DEFLATE CIPHER AES PASSWORD ? CHARSET 'UTF-8';
--end

--select_image_by_id
SELECT * FROM image WHERE id = ?;
--end

--select_item_by_id
SELECT * FROM item WHERE id = ?;
--end

--select_item_by_ref
SELECT * FROM item WHERE ref = ?;
--end

--select_all_items
SELECT * FROM item;
--end

--select_all_items_2
SELECT * FROM item LIMIT ? OFFSET ?;
--end

--select_items_by_id
SELECT * FROM item WHERE id IN (CSV);
--end

--select_item_tags
SELECT * FROM tag WHERE id IN (SELECT tagid FROM tag_item WHERE itemid = ?);
--end

--select_tags
SELECT * FROM tag;
--end

--select_tags_2
SELECT * FROM tag LIMIT ? OFFSET ?;
--end
--select_tags_by_id
SELECT * FROM tag WHERE id IN (CSV);
--end

--select_tag_by_id
SELECT * FROM tag WHERE id = ?;
--end

--select_tag_by_name
SELECT * FROM tag WHERE name = ?;
--end

--select_tag_group_by_id
SELECT * FROM tag_group WHERE id = ?;
--end

--select_most_used_tag
SELECT * FROM tag WHERE id IN (
    SELECT tagid FROM tag_item
    GROUP BY tagid
    ORDER BY COUNT(tagid) DESC
    LIMIT 1
);
--end

--select_most_5_tags
SELECT * FROM tag WHERE id IN (
    SELECT tagid FROM tag_item
    GROUP BY tagid
    ORDER BY COUNT(tagid) DESC
    LIMIT 5
);
--end

--select_most_tagged_item
SELECT * FROM item WHERE id IN (
    SELECT itemid FROM tag_item
    GROUP BY itemid
    ORDER BY COUNT(itemid) DESC
    LIMIT 1
);
--end

--insert_image
INSERT INTO image(data)
    SELECT
        ? AS data
    WHERE NOT EXISTS (SELECT id FROM image WHERE id = ?);
--end

--insert_tag_group
INSERT INTO tag_group(name)
	SELECT name FROM
		(SELECT
			TRIM(BOTH FROM ?) AS name)
		AS entry
	WHERE NOT EXISTS (SELECT id FROM tag_group WHERE name = entry.name);
--end

--insert_item
INSERT INTO item(name, info, ref, privy, username, password, dateadd, datemod, imageid)
SELECT * FROM
    (SELECT
        TRIM(BOTH FROM ?) AS name,
	    TRIM(BOTH FROM ?) AS info,
		TRIM(BOTH FROM ?) AS ref,
		CAST(? AS BOOLEAN) AS privy,
		TRIM(BOTH FROM ?) AS username,
		CAST(? AS VARCHAR) AS password,
		CAST(? AS TIMESTAMP) dateadd,
		CAST(? AS TIMESTAMP) AS datemod,
		CAST(? AS BIGINT) AS imageid)
	AS entry
WHERE NOT EXISTS (SELECT id FROM item WHERE ref = entry.ref);
--end

--insert_tag
INSERT INTO tag(name, color, dateadd, datemod, parentid, groupid)
SELECT * FROM
	(SELECT
        TRIM(BOTH FROM ?) AS name,
        TRIM(BOTH FROM ?) AS color,
        CAST(? AS TIMESTAMP) AS dateadd,
        CAST(? AS TIMESTAMP) AS datemod,
        CAST(? AS BIGINT) AS parentid,
        CAST(? AS BIGINT) AS groupid)
    AS entry
WHERE NOT EXISTS (SELECT id FROM tag WHERE name = entry.name);
--end

--insert_tag_item_map
INSERT INTO tag_item (itemid, tagid) values(?, (SELECT id FROM tag WHERE name = ?));
--end

--update_item
UPDATE item SET
    name = TRIM(BOTH FROM ?),
    info = TRIM(BOTH FROM ?),
    ref = TRIM(BOTH FROM ?),
    privy = ?,
    username = TRIM(BOTH FROM ?),
    password = ?,
    dateadd = ?,
    datemod = ?,
    imageid = ?
WHERE id = ?
AND NOT EXISTS (
	SELECT id
	FROM item
	WHERE
		ref = ? AND
		id <> ?
);
--end

--update_tag
UPDATE tag SET
    name = TRIM(BOTH FROM ?),
    color = TRIM(BOTH FROM ?),
    dateadd = ?,
    datemod = ?,
    parentid = ?,
    groupid = ?
WHERE id = ?
AND NOT EXISTS (
	SELECT id
	FROM tag
	WHERE
		name = ? AND
		id <> ?
);
--end

--remove_item
DELETE FROM item WHERE id = ?;
--end

--remove_item_tags
DELETE FROM tag_item WHERE itemid = ?;
--end

--remove_tag
DELETE FROM tag WHERE id = ?;
--end

--remove_tag_items
DELETE FROM item WHERE id IN (SELECT itemid FROM tag_item WHERE tagid = ?);
--end

--remove_unused_tags
DELETE FROM tag WHERE id NOT IN (SELECT DISTINCT tagid FROM tag_item);
--end

--remove_items
DELETE FROM item WHERE id IN (CSV);
--end

--replace_tag
UPDATE tag_item SET tagid = ? WHERE tagid = ?;
--end

--count_items
SELECT COUNT(id) AS TOTAL FROM item;
--end

--count_untagged_items
SELECT COUNT(id) AS TOTAL FROM item WHERE id NOT IN (SELECT DISTINCT itemid FROM tag_item);
--end

--count_tags
SELECT COUNT(id) AS TOTAL FROM tag;
--end

--count_tag_items
SELECT COUNT(itemid) AS TOTAL FROM tag_item WHERE tagid = ?;
--end

--count_item_tags
SELECT COUNT(tagid) AS TOTAL FROM tag_item WHERE itemid = ?;
--end

--count_unused_tags
SELECT COUNT(id) AS TOTAL FROM tag WHERE id NOT IN (SELECT DISTINCT tagid FROM tag_item);
--end
