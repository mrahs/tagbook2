/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.syncers;

import datamodel.Item;

import java.util.List;

/**
 * Author: Anas H. Sulaiman 
 */
public interface ISyncer {

    public List<Item> getNewItems();

    public boolean publishItem(Item item);

    public boolean publishItems(List<Item> items);
}
