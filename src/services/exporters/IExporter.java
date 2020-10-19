/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.exporters;

import datamodel.Item;

import java.util.Collection;

/**
 * Author: Anas H. Sulaiman 
 */
public interface IExporter extends AutoCloseable {
	public void put(Item item) throws Exception;

	public void putAll(Collection<Item> items) throws Exception;

	public ExportConfigs getConfigs();

	public void setConfigs(ExportConfigs configs);
}
