/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.exporters;

import datamodel.Item;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;

/**
 * Author: Anas H. Sulaiman 
 */
public class TextExporter implements IExporter {

	Writer out;
	TextExportFormatter formatter;
	BufferedWriter writer;
	StringBuilder bf;
	boolean first;

	public TextExporter(Writer out, TextExportFormatter formatter) throws IOException {
		setFormatter(formatter);
		writer = new BufferedWriter(out);
		bf = new StringBuilder();
		first = true;

		init();
	}

	public Writer getOutput() {
		return out;
	}

	public TextExportFormatter getFormatter() {
		return this.formatter;
	}

	public void setFormatter(TextExportFormatter formatter) {
		this.formatter = Objects.requireNonNull(formatter);
	}

	private void init() throws IOException {
		writer.write(formatter.getHeader());
	}

	@Override
	public void put(Item item) throws Exception {
		if (first) {
			bf.append(formatter.formatItem(item));
			first = false;
		}
		bf.append(formatter.getItemSeparator()).append(formatter.formatItem(item));
	}

	@Override
	public void putAll(Collection<Item> items) throws Exception {
		for (Item i : items) {
			put(i);
		}
	}

	@Override
	public ExportConfigs getConfigs() {
		return formatter.getConfigs();
	}

	@Override
	public void setConfigs(ExportConfigs configs) {
		this.formatter.setConfigs(configs);
	}

	@Override
	public void close() throws Exception {
		writer.write(bf.toString());
		writer.write(formatter.getFooter());
		writer.close();
	}
}
