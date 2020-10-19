/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datamodel;

import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertTrue;

/**
 * Author: Anas H. Sulaiman 
 */
public class TagTest {

	@Test
	public void testTagDateSetting() {
		Tag tag = new Tag("");

		long time = System.currentTimeMillis();
		long offset = 3600000;
		// setting datemod to be after dateadd is ok
		tag.setDatemod(new Timestamp(time + offset));
		assertTrue(tag.getDateadd().compareTo(tag.getDatemod()) < 0);

		// setting datemod to be before dateadd will not change datemod
		tag.setDatemod(new Timestamp(time - offset));
		assertTrue(tag.getDateadd().compareTo(tag.getDatemod()) < 0);

		// setting dateadd to be after datemod will update datemod to be equal to dateadd
		tag.setDateadd(new Timestamp(time + 2 * offset));
		assertTrue(tag.getDateadd().compareTo(tag.getDatemod()) == 0);
	}

	@Test
	public void testName() {
		Tag tag = new Tag("");

		tag.setName("link");
		tag.setName("رابط");
		tag.setName("age_24");
		tag.setName("عمر_24");
		try {
			tag.setName("age.24");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}

		try {
			tag.setName("عمر.24");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testColor() {
		Tag tag = new Tag("");

		tag.setColor("#555");
		tag.setColor("#555555");
		tag.setColor("#abcdef");

		try {
			tag.setColor("#abcdeg");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}

		try {
			tag.setColor("#aat");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}
		try {
			tag.setColor("555");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}
	}
}
