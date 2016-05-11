package processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class StreamProcessor {
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	protected BufferedReader input;
	private Pattern logLinePattern;
	private String lastReadLine = null;

	public StreamProcessor(InputStream input, String patternStartOfLogLine) {
		this.input = new BufferedReader(new InputStreamReader(input));
		logLinePattern = Pattern.compile(patternStartOfLogLine);
	}

	protected StreamProcessor(BufferedReader input, Pattern patternStartOfLogLine) {
		this.input = input;
		this.logLinePattern = patternStartOfLogLine;
	}

	public void execute(final Consumer<String> consumer) {
		try {
			String logEntry = getNextLogEntry();
			while (logEntry != null) {
				consumer.accept(logEntry);
				logEntry = getNextLogEntry();
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	protected String getNextLogEntry() throws IOException {
		StringBuilder result = new StringBuilder();

		if (lastReadLine == null) {
			// This method is called the first time - Initialize the last read
			// line
			lastReadLine = input.readLine();
		}

		if (lastReadLine != null) {
			result.append(lastReadLine).append(LINE_SEPARATOR);
			lastReadLine = input.readLine();
			while (lastReadLine != null && !isLogLine(lastReadLine)) {
				result.append(lastReadLine).append(LINE_SEPARATOR);
				lastReadLine = input.readLine();
			}

			// Return the current log-entry
			return result.toString();
		} else {
			// End of the stream has been reached
			return null;
		}
	}

	protected boolean isLogLine(final String line) {
		boolean result = false;

		if (line != null && logLinePattern.matcher(line).find()) {
			result = true;
		}

		return result;
	}
}
