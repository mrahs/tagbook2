/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services;

import java.util.List;

/**
 * Reads file metadata.
 * Author: Anas H. Sulaiman 
 */
public interface IMetaReader {
    public List<String> readMetadata(String filename);
}
