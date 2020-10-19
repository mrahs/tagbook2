/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package utils;

/**
 * Author: Anas H. Sulaiman 
 */
public class PhoneNumberValidator {
    private static PhoneNumberValidator ourInstance = new PhoneNumberValidator();

    private PhoneNumberValidator() {
    }

    public static PhoneNumberValidator getInstance() {
        return ourInstance;
    }

    /**
     * Currently this is rather a strange implementation!
     * <p>
     * If the number of digits is greater than the number of letters (not including punctuations), then it's a phone number!
     * </p>
     * <p>
     * This is based on several facts:
     * <ol>
     * <li>The user may use his own formatting.</li>
     * <li>The user must not be restricted to enter country code, city code or special characters such as plus sign (+)</li>
     * <li>Some phone numbers are special in one or another (e.g. 4 digits phone numbers and numbers that contain letters)</li>
     * </ol>
     * </p>
     * <p>
     * Given the above requirements, it's a very complex validation process. However, <a href="http://libphonenumber.googlecode.com/">libphonenumber</a> seems to have it all covered, though, it requires more handling by the interface.
     * <p/>
     * </p>
     *
     */
    public boolean isValid(String phoneNumber) {
        int digitCount = 0;
        int letterCount = 0;
        for (int i = 0; i < phoneNumber.length(); ++i) {
            char c = phoneNumber.charAt(i);
            if (Character.isDigit(c))
                digitCount++;
            if (Character.isLetter(c))
                letterCount++;
        }
	    return digitCount > letterCount;
    }
}
