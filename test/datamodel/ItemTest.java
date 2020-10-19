/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datamodel;

import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author: Anas H. Sulaiman 
 */
public class ItemTest {

	@Test
	public void testItemDateSetting() {
		Item item = new Item("");

		long time = System.currentTimeMillis();
		long offset = 3600000;
		// setting datemod to be after dateadd is ok
		item.setDatemod(new Timestamp(time + offset));
		assertTrue(item.getDateadd().compareTo(item.getDatemod()) < 0);

		// setting datemod to be before dateadd will not change datemod
		item.setDatemod(new Timestamp(time - offset));
		assertTrue(item.getDateadd().compareTo(item.getDatemod()) < 0);

		// setting dateadd to be after datemod will update datemod to be equal to dateadd
		item.setDateadd(new Timestamp(time + 2 * offset));
		assertTrue(item.getDateadd().compareTo(item.getDatemod()) == 0);
	}

	@Test
	public void testRefType() {
		Item item = new Item("");

		// regular use cases
		item.setRef("");
		//        assertEquals(item.getRefType(), Item.RefType.NULL);
		assertTrue(item.isNull());

		item.setRef("http://ahs.pw");
		//        assertEquals(item.getRefType(), Item.RefType.URL);
		assertTrue(item.isUrl());

		item.setRef("anas@ahs.pw");
		//        assertEquals(item.getRefType(), Item.RefType.EMAIL);
		assertTrue(item.isEmail());

		item.setRef("0992093758");
		//        assertEquals(item.getRefType(), Item.RefType.PHONE);
		assertTrue(item.isPhone());

		// text only: should throw exception
		try {
			item.setRef("AHS");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}

		// mixing letters with numbers: numbers must be more than letters
		item.setRef("+963-992-093758");
		assertEquals(item.getRefType(), Item.RefType.PHONE);
		try {
			item.setRef("111-abcd");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}
		try {
			item.setRef("111-abc");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}
	}
}
