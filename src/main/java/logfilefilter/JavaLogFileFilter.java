package logfilefilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import logfilefilter.internal.processors.StreamProcessor;

/**
 * Filters java log files with the help of include and exclude {@link Pattern
 * Regex-Pattern}. The main feature is, that a log message which is splitted
 * into several lines, is handled as one log-message. The filters are applied
 * during the file is processed as a stream, so the file must not read
 * completely into the memory.
 * <p>
 * Preconditions: Each log message must begin with a timestamp. A log message
 * which is splitted into several lines, starts only with one timestamp.
 * <p>
 * This file filter is meant for log files like this:
 *
 * <pre>
 * 2016-04-27 14:38:44,123 INFO First log
 * 2016-04-27 14:38:44,400 ERROR An Exception occurred: Illegal state of object ...
 *    at MyClass.execute(83)
 *    at MyCallingClass.run(50)
 * 2016-04-27 14:38:44,400 INFO Service result: ERROR
 * </pre>
 *
 * Some examples about the include and exclude filters:<br>
 * Applying include filter "Service result: ERROR" and exlcude filter " INFO "
 * will give only the last line. Applying the exclude filter (for known
 * exceptions, which should be ignored) " MyClass.execute(83)" will return the
 * first and the last line.
 * <p>
 * If no source file is defined, {@link System#in} is used as source stream.<br>
 * If no target file is defined, {@link System#out} is used as target stream.
 * The default timestamp-format is {@link #DEFAULT_DATE_PATTERN}.
 *
 * @author joegotgit@outlook.de
 */
public class JavaLogFileFilter {
	/**
	 * The default pattern identify a log message when a line starts with
	 * "yyyy-MM-dddd HH:mm:ss,SSS". For example: 2016-04-27 14:38:44,123
	 */
	public String DEFAULT_DATE_PATTERN = "^[0-9-]{10} [0-9:,]{12}";

	private String includePattern = null;
	private String excludePattern = null;

	private String datePattern = DEFAULT_DATE_PATTERN;

	private String sourceFilePath = null;
	private String targetFilePath = null;

	public void execute() throws IOException {
		List<Consumer<String>> consumerList = new ArrayList<>();

		try (OutputStream out = getOutStream(); InputStream in = getInputStream()) {
			if (includePattern != null) {
				Pattern includePattern = Pattern.compile(this.includePattern);
				consumerList.add(logEntry -> {
					if (includePattern.matcher(logEntry).find()) {
						try {
							out.write(logEntry.getBytes());
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
				});
			}

			if (excludePattern != null) {
				Pattern excludePattern = Pattern.compile(this.excludePattern);
				consumerList.add(logEntry -> {
					if (!excludePattern.matcher(logEntry).find()) {
						try {
							out.write(logEntry.getBytes());
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
				});
			}

			if (!consumerList.isEmpty()) {
				newSteamProcessor(in).execute(consumerList);
			}
		}
	}

	public void setIncludePattern(String includePattern) {
		this.includePattern = includePattern;
	}

	public void setExcludePattern(String excludePattern) {
		this.excludePattern = excludePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public void setTargetFilePath(String targetFilePath) {
		this.targetFilePath = targetFilePath;
	}

	protected InputStream getInputStream() throws IOException {
		if (sourceFilePath == null) {
			return System.in;
		} else {
			return Files.newInputStream(Paths.get(sourceFilePath));
		}
	}

	protected OutputStream getOutStream() throws IOException {
		if (targetFilePath == null) {
			return System.out;
		} else {
			return Files.newOutputStream(Paths.get(targetFilePath));
		}
	}

	protected StreamProcessor newSteamProcessor(InputStream in) {
		return new StreamProcessor(in, datePattern);
	}
}
