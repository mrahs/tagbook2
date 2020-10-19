/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Anas H. Sulaiman 
 */
public class Image {
	private static final List<Image> instances = new ArrayList<>();
	long id;
	byte[] data;

	public Image() {
		this(-1, null);
	}

	public Image(long id, byte[] data) {
		this.id = id;
		this.data = data;

		instances.add(this);
	}

	public static Image getIfExists(long id) {
		for (Image img : instances) {
			if (img.getId() == id) return img;
		}
		return null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isNull() {
		return data == null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Image image = (Image) o;

		return id == image.id;

	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}
}
