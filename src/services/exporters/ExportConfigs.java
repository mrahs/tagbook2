/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package services.exporters;

import datamodel.Field;

import java.util.Collection;
import java.util.HashSet;

/**
 * Author: Anas H. Sulaiman 
 */
public class ExportConfigs {
	Collection<Field> skipFields;

	public ExportConfigs() {
		skipFields = new HashSet<>(11);
	}

	public void addSkipField(Field f) {
		skipFields.add(f);
	}

	public void removeSkipField(Field f) {
		skipFields.remove(f);
	}

	public boolean isSkipField(Field f) {
		return skipFields.contains(f);
	}
}
