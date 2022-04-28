package hh.nitor.slackbot;

import hh.nitor.slackbot.util.NameCompare;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NameCompareTests {

	@Autowired
	private NameCompare comparer;

	@Test
	@DisplayName("Name comparer finds small typos, test 1")
	public void comparerFindsTypos1() {
		String name = "javascpirt";
		String expectation = "javascript";
		List<String> names = new ArrayList<String>(Arrays.asList(expectation, "typescript", "azure"));

		List<String> result = comparer.compareToList(name, names);

		Assertions.assertTrue(result.size() == 1, "returns 1 item");
		Assertions.assertEquals(result.get(0), expectation, String.format("item is %s", expectation));
	}

	@Test
	@DisplayName("Name comparer finds small typos, test 2")
	public void comparerFindsTypos2() {
		String name = "typoscrpit";
		String expectation = "typescript";
		List<String> names = new ArrayList<String>(Arrays.asList("javascript", expectation, "azure"));

		List<String> result = comparer.compareToList(name, names);

		Assertions.assertTrue(result.size() == 1, "returns 1 item");
		Assertions.assertEquals(result.get(0), expectation, String.format("item is %s", expectation));
	}

	@Test
	@DisplayName("Name comparer finds small typos, test 3")
	public void comparerFindsTypos3() {
		String name = "azere";
		String expectation = "azure";
		List<String> names = new ArrayList<String>(Arrays.asList("javascript", "typescript", expectation));

		List<String> result = comparer.compareToList(name, names);

		Assertions.assertTrue(result.size() == 1, "returns 1 item");
		Assertions.assertEquals(result.get(0), expectation, String.format("item is %s", expectation));
	}

}
