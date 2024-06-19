package hudson.plugins.chucknorris;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.junit.Before;
import org.junit.Test;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FactGeneratorTest extends TestCase {

	@Rule
	public MockitoRule experimentRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

	private FactGenerator generator;

	@Before
	public void setUp() {
		generator = new FactGenerator();
	}

	@Test
	public void testRandomGivesAtLeast2Facts() {
		String lastFact = null;
		for (int i = 0; i < 1000000; i++) {
			String currFact = generator.random();
			if (lastFact != null && !lastFact.equals(currFact)) {
				return;
			}
			lastFact = currFact;
		}
		fail("Random should give at least 2 different facts in 1000000 tries.");
	}
}
