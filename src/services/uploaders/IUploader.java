/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.uploaders;

/**
 * Uploads a given file to a supported online service.
 * Author: Anas H. Sulaiman 
 */
public interface IUploader {
    public boolean upload(String file);
}
