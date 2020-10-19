import datamodel.Image;
import datamodel.Item;
import datamodel.Tag;
import datamodel.TagGroup;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

/**
 * Author: Anas H. Sulaiman 
 */
public class MiscTest {

	@Test
	public void testImageCaching() {
		Image img = new Image();
		img.setId(50);
		Image i = Image.getIfExists(50);
		assertEquals(50, i.getId());
		img.setId(4);
		i = Image.getIfExists(4);
		assertEquals(4, i.getId());
	}

	// ignored
	public void testStringReps() {
		TagGroup tg = new TagGroup();
		tg.setId(6);
		tg.setName("TG1");

		Collection<Tag> tags = new LinkedHashSet<>(5);
		Tag t1, t2;
		tags.add((t1 = new Tag("link", "#555", null, tg)));
		tags.add((t2 = new Tag("personal", "#555", null, null)));
		tags.add(new Tag("ahs", "#555", null, tg));
		tags.add(new Tag("web", "#555", null, null));
		tags.add(new Tag("net", "#555", null, null));

		System.out.println(new Item(999, "Anas H. Sulaiman Personal Website", "Anas H. Sulaiman (aka AHS) is an application developer and he's the author of TagBook2.", "http://ahs.pw", false, "ahs", "asdf", tags, null));

		System.out.println(t1);
		System.out.println(t2);
		System.out.println(tg);
	}

	@Test
	public void miscTest() {
		//        System.out.println(IOUtils.getInstance().getFileName("test/test/test.lol"));
	}
}
