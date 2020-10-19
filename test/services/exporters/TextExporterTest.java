/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.exporters;

import datamodel.Field;
import datamodel.Item;
import datamodel.Tag;
import datamodel.TagGroup;
import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import static junit.framework.Assert.assertEquals;

/**
 * Author: Anas H. Sulaiman 
 */
public class TextExporterTest {

	public void testTextExporter() throws Exception {
		StringWriter writer = new StringWriter();

//		ExportConfigs configs = new ExportConfigs();
//		configs.addSkipField(Field.id);
//
//		TextExportFormatter formatter = new TextExportFormatter(configs, "Header:D\n", "Footer:D\n", "\"{id}\",\"{name}\",\"{ref}\",\"{tags}\"", "\n", " ");
//		TextExporter exporter = new TextExporter(writer, formatter);
//
//		exporter.put(new Item("AHS", "http://ahs.pw"));
//		exporter.close();
//
////		assertEquals(formatter.getHeader() + "\"-1\",\"AHS\",\"http://ahs.pw\"\n" + formatter.getFooter(), writer.toString());
//
//		StringWriter writer2 = new StringWriter();
//		TextExporter exporter2 = new TextExporter(writer2, formatter);
//		exporter2.putAll(makeItems());
//		exporter2.close();
//		System.out.println(writer2.toString());

		TextExporter exporter = new TextExporter(writer, TextExportFormatter.getJsonFormatter());
		exporter.putAll(makeItems());
		exporter.close();

		System.out.print(writer.toString());
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
}
