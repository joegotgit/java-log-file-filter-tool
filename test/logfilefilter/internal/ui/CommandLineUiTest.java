package logfilefilter.internal.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CommandLineUiTest {
	public static final String SIMPLE_LOG_FILE = "./test/resource/testLogs/simple_test.logfile";
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	ByteArrayOutputStream out = new ByteArrayOutputStream();

	@InjectMocks
	private CommandLineUi testee;

	@Before
	public void setUp() {
		System.setOut(new PrintStream(out));
	}

	@Test
	public void shouldExcludeMultiLineLog() throws Exception {
		String[] args = { "-e", "MyClass", "-f", SIMPLE_LOG_FILE };
		testee.execute(args);
		Assert.assertEquals("2016-04-27 14:38:44,123 INFO First log" + LINE_SEPARATOR
				+ "2016-04-27 14:38:44,400 INFO Service result: ERROR" + LINE_SEPARATOR, out.toString());
	}

	@Test
	public void shouldIncludeLineWhichIsAlsoExcluded() throws Exception {
		String[] args = { "-e", "(INFO|ERROR)", "-i", "Service result: ERROR", "-f", SIMPLE_LOG_FILE };
		testee.execute(args);
		Assert.assertEquals("2016-04-27 14:38:44,400 INFO Service result: ERROR" + LINE_SEPARATOR, out.toString());
	}
}
