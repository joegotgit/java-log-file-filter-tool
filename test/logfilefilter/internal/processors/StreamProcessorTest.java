package logfilefilter.internal.processors;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.Assert;
import logfilefilter.internal.processors.StreamProcessor;

@RunWith(MockitoJUnitRunner.class)
public class StreamProcessorTest {
	@Mock
	BufferedReader bufferedInputMock;
	@Mock
	Consumer<String> consumer;
	@Mock
	Consumer<String> consumer2;

	private StreamProcessor testee;

	@Before
	public void setup() {
		testee = new StreamProcessor(bufferedInputMock, Pattern.compile("^[0-9-]{10} [0-9:,]{12}"));
	}

	@Test
	public void testIsLogLineResultTrue() throws Exception {
		String line = "2016-04-27 14:68:44,123";
		Assert.assertTrue(testee.isLogLine(line));
	}

	@Test
	public void testIsLogLineResultFalse() throws Exception {
		String line = "  at AnyClass.AnyMethod(...)";
		Assert.assertFalse(testee.isLogLine(line));
	}

	@Test
	public void testGetNextLogEntryAtTheBeginOfFile() throws Exception {
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine, secondLine);

		Assert.assertEquals(firstLine + StreamProcessor.LINE_SEPARATOR, testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryMovesToNextEntry() throws Exception {
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine, secondLine, null);

		testee.getNextLogEntry();
		Assert.assertEquals(secondLine + StreamProcessor.LINE_SEPARATOR, testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryReturnsNullAtTheEndOfFile() throws Exception {
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine, secondLine, null);

		testee.getNextLogEntry();
		testee.getNextLogEntry();
		Assert.assertNull(testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryForMultiLineLogEntryFollowedByAnotherEntry() throws Exception {
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		String thirdLine = "  at AnyClass.anyMethod(215)";
		String fourthLine = "2016-04-27 14:68:44,444 Fourth Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine, secondLine, thirdLine, fourthLine, null);

		testee.getNextLogEntry();
		Assert.assertEquals(secondLine + StreamProcessor.LINE_SEPARATOR + thirdLine + StreamProcessor.LINE_SEPARATOR,
				testee.getNextLogEntry());
		Assert.assertEquals(fourthLine + StreamProcessor.LINE_SEPARATOR, testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryForMultiLineLogEntryAtTheEndOfFile() throws Exception {
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		String thirdLine = "  at AnyClass.anyMethod(215)";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine, secondLine, thirdLine, null);

		testee.getNextLogEntry();
		Assert.assertEquals(secondLine + StreamProcessor.LINE_SEPARATOR + thirdLine + StreamProcessor.LINE_SEPARATOR,
				testee.getNextLogEntry());
		Assert.assertNull(testee.getNextLogEntry());
	}

	@Test
	public void testExecute() throws Exception {
		testee = Mockito.spy(testee);

		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(testee.getNextLogEntry()).thenReturn(firstLine, secondLine, null);

		testee.execute(Arrays.asList(consumer));
		Mockito.verify(consumer).accept(firstLine + StreamProcessor.LINE_SEPARATOR);
		Mockito.verify(consumer).accept(secondLine + StreamProcessor.LINE_SEPARATOR);
		Mockito.verify(consumer, Mockito.times(2)).accept(Mockito.anyString());
	}

	@Test
	public void testExecuteMoreThanOneConsumer() throws Exception {
		testee = Mockito.spy(testee);

		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(testee.getNextLogEntry()).thenReturn(firstLine, secondLine, null);

		testee.execute(Arrays.asList(consumer, consumer2));
		Mockito.verify(consumer).accept(firstLine + StreamProcessor.LINE_SEPARATOR);
		Mockito.verify(consumer).accept(secondLine + StreamProcessor.LINE_SEPARATOR);
		Mockito.verify(consumer, Mockito.times(2)).accept(Mockito.anyString());

		Mockito.verify(consumer2).accept(firstLine + StreamProcessor.LINE_SEPARATOR);
		Mockito.verify(consumer2).accept(secondLine + StreamProcessor.LINE_SEPARATOR);
		Mockito.verify(consumer2, Mockito.times(2)).accept(Mockito.anyString());
	}
}
