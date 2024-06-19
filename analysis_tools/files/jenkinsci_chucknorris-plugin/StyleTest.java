package hudson.plugins.chucknorris;

import hudson.model.Result;
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
public class StyleTest extends TestCase {

	@Rule
	public MockitoRule experimentRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

	@Test
	public void testGetWithFailureResultGivesBadAssStyle() {
		assertEquals(Style.BAD_ASS, Style.get(Result.FAILURE));
	}

	@Test
	public void testGetWithSuccessResultGivesSuitupStyle() {
		assertEquals(Style.THUMB_UP, Style.get(Result.SUCCESS));
	}

	@Test
	public void testGetWithAbortedResultGivesAlertStyle() {
		assertEquals(Style.ALERT, Style.get(Result.ABORTED));
	}

	@Test
	public void testGetWithNotBuiltResultGivesAlertStyle() {
		assertEquals(Style.ALERT, Style.get(Result.NOT_BUILT));
	}

	@Test
	public void testGetWithUnstableResultGivesAlertStyle() {
		assertEquals(Style.ALERT, Style.get(Result.UNSTABLE));
	}
}
