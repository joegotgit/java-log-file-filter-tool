import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import processors.StreamProcessor;

public class JavaLogFileFilter {
	private String includePattern = null;
	private String excludePattern = null;

	// This pattern will find: 2016-04-27 14:68:44,123
	private String datePattern = "^[0-9-]{10} [0-9:,]{12}";

	private String sourceFilePath = null;

	public void execute(final InputStream inputStream) {
		List<Consumer<String>> consumerList = new ArrayList<>();

		if (includePattern != null) {
			Pattern includePattern = Pattern.compile(this.includePattern);
			consumerList.add(logEntry -> {
				if (includePattern.matcher(logEntry).find()) {
					try {
						System.out.write(logEntry.getBytes());
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
						System.out.write(logEntry.getBytes());
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			});
		}

		if (!consumerList.isEmpty()) {
			new StreamProcessor(inputStream, datePattern).execute(consumerList);
		}
		System.out.close();
	}

	public InputStream getInputStream() throws IOException {
		if (sourceFilePath == null) {
			return System.in;
		} else {
			return Files.newInputStream(Paths.get(sourceFilePath));
		}
	}

	public void parseCommandLineParameters(String[] args) {
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-i":
				includePattern = args[++i];
				break;
			case "-e":
				excludePattern = args[++i];
				break;
			case "-d":
				datePattern = args[++i];
				break;
			case "-f":
				sourceFilePath = args[++i];
				break;
			default:
				throw new IllegalArgumentException("Parameter '" + args[i] + "' is unknown.\n" //
						+ "The following parameters can be used:\n" //
						+ "  -i  Include regex pattern\n" //
						+ "  -e  Exclude regex pattern\n" //
						+ "  -d  Date pattern used to identify the begin of a log entry\n" //
						+ "  -f  Source file");
			}
		}
	}

	public static void main(String[] args) {
		JavaLogFileFilter processor = new JavaLogFileFilter();

		processor.parseCommandLineParameters(args);
		try (InputStream iStream = processor.getInputStream()) {

			processor.execute(iStream);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
