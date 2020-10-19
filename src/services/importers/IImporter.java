/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.importers;

import datamodel.Item;

import java.util.Collection;

/**
 * Author: Anas H. Sulaiman 
 */
public interface IImporter extends AutoCloseable {

	public Item nextItem() throws Exception;

	public boolean hasNext() throws Exception;

	public Collection<Item> getAllItems() throws Exception;

	public void loadAllItems(Collection<Item> items) throws Exception;

	public ImportConfigs getConfigs();

	public void setConfigs(ImportConfigs configs);
}
