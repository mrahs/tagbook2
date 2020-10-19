/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package utils;

import java.nio.file.FileSystems;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Author: Anas H. Sulaiman 
 */
public class Utils {
	private static Utils ourInstance = new Utils();
	public final String NAME_SEPARATOR = FileSystems.getDefault().getSeparator();
	private final Pattern pattern;
	private ResourceBundle stringsEn;
	private ResourceBundle stringsAr;

	private Utils() {
		String HEX_COLOR_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
		pattern = Pattern.compile(HEX_COLOR_PATTERN);
		stringsEn = stringsAr = null;
	}

	public static Utils getInstance() {
		return ourInstance;
	}

	// Misc

	/**
	 * @return a new {@link java.sql.Timestamp} instance using the {@link System#currentTimeMillis()}
	 */
	public Timestamp nowTs() {
		return new Timestamp(System.currentTimeMillis());
	}

	public boolean isHexColor(String hexColor) {
		return pattern.matcher(hexColor).matches();
	}

	public String encodeBase64(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public byte[] decodeBase64(String data) {
		return Base64.getDecoder().decode(data);
	}
	// URL

	public String urlGetDomainName(String url) {
		// TODO
		return "";
	}

	public String urlGetProtocolName(String url) {
		// TODO
		return "";
	}

	public String urlIsBroken(String url) {
		// TODO
		return "";
	}

	// i18n
	public ResourceBundle i18n() {
		if (stringsEn == null) {
			//            stringsEn = ResourceBundle.getBundle("i18n.strings", new Locale("ar", "SA"));
			stringsEn = ResourceBundle.getBundle("i18n.strings", Locale.US);
			return stringsEn;
		}
		return stringsEn;
	}
}
