package processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class StreamProcessor {
	BufferedReader input;
	// 2016-04-27 14:68:44,123
	Pattern logLinePattern = Pattern.compile("^[0-9-]{10} [0-9:,]{12}");

	String lastReadLine = null;

	public StreamProcessor(final InputStream input) {
		this.input = new BufferedReader(new InputStreamReader(input));
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

		if (lastReadLine == null)
			// This method is called the first time - Initialize the last read
			// line
			lastReadLine = input.readLine();

		if (lastReadLine != null) {
			result.append(lastReadLine).append("\n");
			lastReadLine = input.readLine();
			while (lastReadLine != null && !isLogLine(lastReadLine)) {
				result.append(lastReadLine).append("\n");
				lastReadLine = input.readLine();
			}

			// Return the current log-entry
			return result.toString();
		} else
			// End of the stream has been reached
			return null;
	}

	protected boolean isLogLine(final String line) {
		boolean result = false;

		if (line != null && logLinePattern.matcher(line).find())
			result = true;

		return result;
	}
}
