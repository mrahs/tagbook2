/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.*;

/**
 * Flexible log class whose features includes:
 * <ul>
 * <li>Each entry is represented as a line</li>
 * <li>Traversable lines list</li>
 * <li>Convert to string with custom formats</li>
 * <li>Write log to file</li>
 * <li>Auto-write to disk upon reaching a customized limit with the option to
 * clear the log from memory</li>
 * <li>Register listeners to get notifications for every new entry</li>
 * <li>Set log status to control what's logged and what's not</li>
 * </ul>
 *
 * @author Anas H. Sulaiman 
 */
public class Log {
	/**
	 * default format for LogLine: [Flag]@"Timestamp" Title\tDetails
	 */
	private static final String defaultFormat = "[%1$s]@\"%4$s\" %2$s\t%3$s";
	/**
	 * default timestamp format is {@code DateFormat.MEDIUM} using default
	 * locale
	 */
	private static final DateFormat defaultTimestampFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
	private LogStatus status;
	private int autoCommitOn;
	private int numOfEntry;
	private boolean clearOnCommit;
	private String path;
	private List<LogLine> lines;
	private List<LogListener> listeners;

	/**
	 * Constructs a new Log instance as in-memory log
	 */
	public Log() {
		this("");
	}

	/**
	 * Constructs a new Log instance with the following defaults:
	 * <pre>
	 * status = LogStatus.NONE;
	 * autoCommitOn = 100;
	 * clearOnCommit = false;
	 * </pre>
	 *
	 * @param path the log file path
	 * @throws NullPointerException if argument is null
	 */
	public Log(String path) {
		this.path = Objects.requireNonNull(path);
		status = LogStatus.NONE;
		autoCommitOn = 100;
		numOfEntry = 0;
		clearOnCommit = false;
		lines = new ArrayList<>();
		listeners = new ArrayList<>();
	}

	private static Timestamp nowTs() {
		return new Timestamp(System.currentTimeMillis());
	}

	public LogStatus getStatus() {
		return status;
	}

	/**
	 * @throws NullPointerException if argument is null
	 */
	public void setStatus(LogStatus status) {
		this.status = Objects.requireNonNull(status);
	}

	public int getAutoCommitOn() {
		return autoCommitOn;
	}

	/**
	 * If {@code autoCommitOn} is 0, the auto commit feature is disabled
	 *
	 * @param autoCommitOn the number of entries before attempting to commit
	 * @throws IllegalArgumentException if {@code autoCommitOn} is negative
	 */
	public void setAutoCommitOn(int autoCommitOn) {
		if (autoCommitOn < 0) throw new IllegalArgumentException("negative value is not allowed");
		this.autoCommitOn = autoCommitOn;
	}

	public boolean isClearOnCommit() {
		return clearOnCommit;
	}

	public void setClearOnCommit(boolean clearOnCommit) {
		this.clearOnCommit = clearOnCommit;
	}

	public String getPath() {
		return path;
	}

	/**
	 * @throws NullPointerException if argument is null
	 */
	public void setPath(String path) {
		this.path = path.trim();
	}

	/**
	 * Returns an unmodifiable view of the lines.
	 *
	 * @return an unmodifiable view of the lines.
	 */
	public List<LogLine> getLines() {
		return Collections.unmodifiableList(lines);
	}

	/**
	 * Formats the entire log and returns it as a String
	 *
	 * @param format          specifies how each line is formatted
	 * @param timestampFormat specifies how timestamps should be formatted
	 * @return a formatted String of the entire log
	 * @throws NullPointerException     if one of arguments is null
	 * @throws IllegalArgumentException if string argument is blank
	 * @see {@link java.util.Formatter}
	 */
	public String getFormatted(String format, DateFormat timestampFormat) {
		StringBuilder text = new StringBuilder();
		for (LogLine ll : lines) {
			ll.setFormat(format);
			ll.setTimestampFormat(timestampFormat);
			text.append(ll.toString()).append(System.lineSeparator());
		}
		return text.toString();
	}

	/**
	 * A convenience method for {@link #getFormatted(String, java.text.DateFormat)} that
	 * uses default formatters
	 *
	 * @see #defaultFormat
	 * @see #defaultTimestampFormat
	 */
	public String getFormatted() {
		return getFormatted(defaultFormat, defaultTimestampFormat);
	}

	/**
	 * @throws NullPointerException if argument is null
	 */
	public void addListener(LogListener l) {
		Objects.requireNonNull(l);
		listeners.add(l);
	}

	/**
	 * @throws NullPointerException if argument is null
	 */
	public void removeListener(LogListener l) {
		Objects.requireNonNull(l);
		listeners.remove(l);
	}

	/**
	 * Logs a new entry using current time.
	 *
	 * @throws NullPointerException if one of arguments is null
	 */
	public void log(LogFlag flag, String title, String details) {
		switch (status) {
			case NONE:
				return;
			case CRITICAL:
				if (flag != LogFlag.ERROR) return;
				break;
			case ALERT_ONLY:
				if (flag != LogFlag.WARNING) return;
				break;
			case INFO:
				if (flag != LogFlag.INFO) return;
				break;
			case NON_CRITICAL:
				if (flag == LogFlag.ERROR) return;
				break;
			case ALL:
				break;
			default:
				break;
		}
		LogLine loggedLine = new LogLine(flag, title, details, nowTs());
		lines.add(loggedLine);
		numOfEntry++;
		for (LogListener l : listeners) {
			l.onLog(loggedLine);
		}
		if (autoCommitOn == 0) return;
		if (numOfEntry >= autoCommitOn) {
			commit();
			numOfEntry = 0;
		}
	}

	/**
	 * Logs a new INFO entry using current time.
	 *
	 * @throws NullPointerException if one of arguments is null
	 */
	public void i(String title, String details) {
		log(LogFlag.INFO, title, details);
	}

	/**
	 * Logs a new INFO entry without a title using current time.
	 *
	 * @throws NullPointerException if argument is null
	 */
	public void i(String details) {
		i("", details);
	}

	/**
	 * Logs a new ERROR entry using current time.
	 *
	 * @throws NullPointerException if one of arguments is null
	 */
	public void e(String title, String details) {
		log(LogFlag.ERROR, title, details);
	}

	/**
	 * Logs a new ERROR entry without a title using current time.
	 *
	 * @throws NullPointerException if argument is null
	 */
	public void e(String details) {
		e("", details);
	}

	/**
	 * Logs a new WARNING entry using current time.
	 *
	 * @throws NullPointerException if one of arguments is null
	 */
	public void w(String title, String details) {
		log(LogFlag.WARNING, title, details);
	}

	/**
	 * Logs a new WARNING entry without a title using current time.
	 *
	 * @throws NullPointerException if argument is null
	 */
	public void w(String details) {
		w("", details);
	}

	/**
	 * Formats the entire log and writes to disk.
	 *
	 * @return true if write operation was successful, false otherwise
	 * @throws NullPointerException     if one of arguments is null
	 * @throws IllegalArgumentException if string argument is blank
	 * @see #getFormatted(String, java.text.DateFormat)
	 */
	public boolean commit(String format, DateFormat timestampFormat) {
		if (path.isEmpty()) return false;
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), Charset.forName("UTF-8"))) {
			writer.write(getFormatted(format, timestampFormat));
		} catch (IOException e) {
			return false;
		}
		if (clearOnCommit) {
			clear();
		}
		return true;
	}

	/**
	 * A convenience method for {@link #commit(String, java.text.DateFormat)} that uses
	 * default formatters
	 *
	 * @see #defaultFormat
	 * @see #defaultTimestampFormat
	 */
	public boolean commit() {
		return commit(defaultFormat, defaultTimestampFormat);
	}

	/**
	 * Clears everything.
	 */
	public void clear() {
		lines.clear();
	}

	/**
	 * Simply calls {@link #getFormatted()}
	 */
	@Override
	public String toString() {
		return getFormatted();
	}

	public static enum LogStatus {
		/**
		 * Nothing will be logged
		 */
		NONE,
		/**
		 * {@link LogFlag#INFO} and {@link LogFlag#WARNING} and
		 * {@link LogFlag#ERROR}
		 */
		ALL,

		/**
		 * {@link LogFlag#ERROR} only
		 */
		CRITICAL,

		/**
		 * {@link LogFlag#INFO} and {@link LogFlag#WARNING}
		 */
		NON_CRITICAL,

		/**
		 * {@link LogFlag#INFO} only
		 */
		INFO,

		/**
		 * {@link LogFlag#WARNING} only
		 */
		ALERT_ONLY
	}

	public static enum LogFlag {
		INFO, WARNING, ERROR
	}

	public static interface LogListener {
		public void onLog(LogLine loggedLine);
	}

	public static class LogLine {
		private LogFlag flag;
		private String title;
		private String details;
		private Timestamp timestamp;
		private String format;
		private DateFormat timestampFormat;

		/**
		 * Constructs a new LogLine instance with the following defaults:
		 * <pre>
		 * flag = INFO
		 * title = ""
		 * details = ""
		 * timestamp = current time
		 * format = defaultFormat
		 * timestampFormat = defaultTimestampFormat
		 * </pre>
		 */
		public LogLine() {
			this(LogFlag.INFO, "", "", nowTs());
		}

		/**
		 * Constructs a new LogLine instance using default formats and current
		 * time
		 *
		 * @throws NullPointerException if one of the arguments is null
		 */
		public LogLine(LogFlag flag, String title, String details) {
			this(flag, title, details, nowTs());
		}

		/**
		 * Constructs a new LogLine instance using default formats
		 *
		 * @throws NullPointerException if one of the arguments is null
		 */
		public LogLine(LogFlag flag, String title, String details, Timestamp timestamp) {
			this.flag = Objects.requireNonNull(flag);
			this.title = title.trim();
			this.details = details.trim();
			this.timestamp = Objects.requireNonNull(timestamp);
			format = defaultFormat;
			timestampFormat = defaultTimestampFormat;
		}

		public LogFlag getFlag() {
			return flag;
		}

		/**
		 * @throws NullPointerException if
		 */
		public void setFlag(LogFlag flag) {
			this.flag = Objects.requireNonNull(flag);
		}

		public String getTitle() {
			return title;
		}

		/**
		 * @throws NullPointerException if {@code title} is null
		 */
		public void setTitle(String title) {
			this.title = title.trim();
		}

		public String getDetails() {
			return details;
		}

		public void setDetails(String details) {
			this.details = details.trim();
		}

		public Timestamp getTimestamp() {
			return timestamp;
		}

		/**
		 * @throws NullPointerException if argument is null
		 */
		public void setTimestamp(Timestamp timestamp) {
			this.timestamp = Objects.requireNonNull(timestamp);
		}

		public String getFormat() {
			return format;
		}

		/**
		 * @param format a format string. Must be valid format string or
		 *               {@link #toString()} will throw an exception
		 * @throws NullPointerException     if argument is null
		 * @throws IllegalArgumentException if string is blank
		 * @see {@link java.util.Formatter}
		 */
		public void setFormat(String format) {
			if (format.trim().isEmpty()) throw new IllegalArgumentException("blank string is not allowed");
			this.format = format;
		}

		public DateFormat getTimestampFormat() {
			return timestampFormat;
		}

		/**
		 * @throws NullPointerException if argument is null
		 */
		public void setTimestampFormat(DateFormat timestampFormat) {
			this.timestampFormat = Objects.requireNonNull(timestampFormat);
		}

		/**
		 * Returns a String representation of this LogLine as defined by the
		 * format string
		 *
		 * @return a String representation of this LogLine
		 * @throws java.util.IllegalFormatException
		 *          if the specified format is invalid
		 * @see #setFormat(String)
		 */
		@Override
		public String toString() {
			return String.format(format, flag, title, details, timestampFormat.format(timestamp));
		}
	}
}
