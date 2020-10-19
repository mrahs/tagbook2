/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.exporters;

import datamodel.Field;
import datamodel.Item;
import utils.Utils;

import java.util.Objects;

/**
 * Author: Anas H. Sulaiman 
 */
public class TextExportFormatter {
	ExportConfigs configs;
	String header;
	String footer;
	String itemPattern;
	String itemSeparator;
	String tagSeparator;

	public TextExportFormatter(String header, String footer, String itemPattern, String itemSeparator, String tagSeparator) {
		this(new ExportConfigs(), header, footer, itemPattern, itemSeparator, tagSeparator);
	}

	public TextExportFormatter(ExportConfigs configs, String header, String footer, String itemPattern, String itemSeparator, String tagSeparator) {
		setConfigs(configs);
		setHeader(header);
		setFooter(footer);
		setItemPattern(itemPattern);
		setItemSeparator(itemSeparator);
		setTagSeparator(tagSeparator);
	}

	public static TextExportFormatter getDiigoFormatter() {
		String header = "title,url,tags,comments,annotations";
		String footer = "";
		String itemPattern = "\"{name}\",\"{ref}\",\"{tags}\",\"{info}\",\"\"";
		String itemSep = "\n";
		String tagSep = " ";

		return new TextExportFormatter(header, footer, itemPattern, itemSep, tagSep);
	}

	public static TextExportFormatter getTagBook2Formatter() {
		String header = "name,info,ref,privy,username,password,tags,dateadd,datemod,image";
		String footer = "";
		String itemPattern = "\"{name}\",\"{info}\",\"{ref}\",\"{privy}\",\"{username}\",\"{password}\",\"{tags}\",\"{dateadd}\",\"{datemod}\",\"{image}\"";
		String itemSep = "\n";
		String tagSep = " ";

		return new TextExportFormatter(header, footer, itemPattern, itemSep, tagSep);
	}

	public static TextExportFormatter getJsonFormatter() {
		String header = "[\n";
		String footer = "\n]";
		String itemPattern = "\t{" +
				                     "\n\t\"name\":\"{name}\"," +
				                     "\n\t\"info\":\"{info}\"," +
				                     "\n\t\"ref\":\"{ref}\"," +
				                     "\n\t\"privy\":\"{privy}\"," +
				                     "\n\t\"username\":\"{username}\"," +
				                     "\n\t\"password\":\"{password}\"," +
				                     "\n\t\"tags\":\"{tags}\"," +
				                     "\n\t\"dateadd\":\"{dateadd}\"," +
				                     "\n\t\"datemod\":\"{datemod}\"," +
				                     "\n\t\"image\":\"{image}\"" +
				                     "\n\t}";
		String itemSep = ",\n";
		String tagSep = " ";

		return new TextExportFormatter(header, footer, itemPattern, itemSep, tagSep);
	}

	public ExportConfigs getConfigs() {
		return configs;
	}

	public void setConfigs(ExportConfigs configs) {
		this.configs = Objects.requireNonNull(configs);
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = Objects.requireNonNull(header);
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = Objects.requireNonNull(footer);
	}

	public String getItemPattern() {
		return itemPattern;
	}

	public void setItemPattern(String itemPattern) {
		this.itemPattern = Objects.requireNonNull(itemPattern);
	}

	public String getItemSeparator() {
		return itemSeparator;
	}

	public void setItemSeparator(String itemSeparator) {
		this.itemSeparator = Objects.requireNonNull(itemSeparator);
	}

	public String getTagSeparator() {
		return tagSeparator;
	}

	public void setTagSeparator(String tagSeparator) {
		this.tagSeparator = Objects.requireNonNull(tagSeparator);
	}

	public String formatItem(Item item) {
		String i = itemPattern;
		i = i.replace("{id}", (configs.isSkipField(Field.id) ? "" : String.valueOf(item.getId())));
		i = i.replace("{name}", (configs.isSkipField(Field.name) ? "" : item.getName()));
		i = i.replace("{info}", (configs.isSkipField(Field.info) ? "" : item.getInfo()));
		i = i.replace("{ref}", (configs.isSkipField(Field.ref) ? "" : item.getRef()));
		i = i.replace("{privy}", (configs.isSkipField(Field.privy) ? "" : String.valueOf(item.isPrivy())));
		i = i.replace("{username}", (configs.isSkipField(Field.username) ? "" : item.getUsername()));
		i = i.replace("{password}", (configs.isSkipField(Field.password) ? "" : item.getPassword()));
		i = i.replace("{tags}", (configs.isSkipField(Field.tags) ? "" : item.getTagsAsString(tagSeparator)));
		i = i.replace("{dateadd}", (configs.isSkipField(Field.dateadd) ? "" : String.valueOf(item.getDateadd().getTime())));
		i = i.replace("{datemod}", (configs.isSkipField(Field.datemod) ? "" : String.valueOf(item.getDatemod().getTime())));
		i = i.replace("{image}", (configs.isSkipField(Field.image) || item.getImage() == null || item.getImage().isNull() ? "" : Utils.getInstance().encodeBase64(item.getImage().getData())));
		return i;
	}
}
