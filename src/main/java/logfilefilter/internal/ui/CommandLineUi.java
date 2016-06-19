package logfilefilter.internal.ui;

import logfilefilter.JavaLogFileFilter;

public class CommandLineUi {
	private final JavaLogFileFilter processor = new JavaLogFileFilter();

	public void parseCommandLineParameters(String[] args) {
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "-i":
				processor.setIncludePattern(args[++i]);
				break;
			case "-e":
				processor.setExcludePattern(args[++i]);
				break;
			case "-d":
				processor.setDatePattern(args[++i]);
				break;
			case "-f":
				processor.setSourceFilePath(args[++i]);
				break;
			case "-t":
				processor.setTargetFilePath(args[++i]);
				break;
			default:
				throw new IllegalArgumentException("Parameter '" + args[i] + "' is unknown.\n" //
						+ "The following parameters can be used:\n" //
						+ "  -i  Include regex pattern\n" //
						+ "  -e  Exclude regex pattern\n" //
						+ "  -d  Date pattern used to identify the begin of a log entry\n" //
						+ "  -f  Source file\n" //
						+ "  -t  Target file, to write the resulting logs");
			}
		}
	}

	public void execute(String[] args) throws Exception {
		parseCommandLineParameters(args);
		processor.execute();
	}

	public static void main(String[] args) {
		CommandLineUi clUi = new CommandLineUi();
		try {
			clUi.execute(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
