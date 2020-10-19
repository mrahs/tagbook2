/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package utils;

/**
 * Author: Anas H. Sulaiman 
 */
public class D {
	public static void checkValidation(boolean state) {
		if (!state) throw new IllegalArgumentException("validation failed");
	}

	public static void checkPositive(long... nums) {
		for (long i : nums)
			if (i < 0) throw new IllegalArgumentException("negative numbers are not allowed here");
	}
}
