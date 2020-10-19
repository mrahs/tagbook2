/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.sharers;

import datamodel.Item;

import java.util.List;

/**
 * Author: Anas H. Sulaiman 
 */
public interface ISharer {

    public boolean share(Item item);

    public boolean share(List<Item> items);
}
