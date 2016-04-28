package processors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.function.Consumer;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StreamProcessorTest {
	@Mock
	BufferedReader bufferedInputMock;
	@Mock
	InputStream input;
	@Mock
	Consumer<String> consumer;

	@InjectMocks
	private StreamProcessor testee;

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

	protected void initBufferedMock() {
		testee.input = bufferedInputMock;
	}

	@Test
	public void testGetNextLogEntryAtTheBeginOfFile() throws Exception {
		initBufferedMock();
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine,
				secondLine);

		Assert.assertEquals(firstLine + "\n", testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryMovesToNextEntry() throws Exception {
		initBufferedMock();
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine,
				secondLine, null);

		testee.getNextLogEntry();
		Assert.assertEquals(secondLine + "\n", testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryReturnsNullAtTheEndOfFile() throws Exception {
		initBufferedMock();
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine,
				secondLine, null);

		testee.getNextLogEntry();
		testee.getNextLogEntry();
		Assert.assertNull(testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryForMultiLineLogEntryFollowedByAnotherEntry()
			throws Exception {
		initBufferedMock();
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		String thirdLine = "  at AnyClass.anyMethod(215)";
		String fourthLine = "2016-04-27 14:68:44,444 Fourth Line";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine,
				secondLine, thirdLine, fourthLine, null);

		testee.getNextLogEntry();
		Assert.assertEquals(secondLine + "\n" + thirdLine + "\n",
				testee.getNextLogEntry());
		Assert.assertEquals(fourthLine + "\n", testee.getNextLogEntry());
	}

	@Test
	public void testGetNextLogEntryForMultiLineLogEntryAtTheEndOfFile()
			throws Exception {
		initBufferedMock();
		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		String thirdLine = "  at AnyClass.anyMethod(215)";
		Mockito.when(bufferedInputMock.readLine()).thenReturn(firstLine,
				secondLine, thirdLine, null);

		testee.getNextLogEntry();
		Assert.assertEquals(secondLine + "\n" + thirdLine + "\n",
				testee.getNextLogEntry());
		Assert.assertNull(testee.getNextLogEntry());
	}

	@Test
	public void testExecute() throws Exception {
		initBufferedMock();
		testee = Mockito.spy(testee);

		String firstLine = "2016-04-27 14:68:44,123 First Line";
		String secondLine = "2016-04-27 14:68:44,333 Second Line";
		Mockito.when(testee.getNextLogEntry()).thenReturn(firstLine,
				secondLine, null);

		testee.execute(consumer);
		Mockito.verify(consumer).accept(firstLine + "\n");
		Mockito.verify(consumer).accept(secondLine + "\n");
		Mockito.verify(consumer, Mockito.times(2)).accept(Mockito.anyString());
	}
}
