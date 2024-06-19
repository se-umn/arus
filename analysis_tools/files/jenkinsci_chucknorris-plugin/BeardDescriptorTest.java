package hudson.plugins.chucknorris;

import static org.mockito.Mockito.mock;
import hudson.model.AbstractProject;
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
public class BeardDescriptorTest extends TestCase {

	@Rule
	public MockitoRule experimentRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

	private BeardDescriptor descriptor;

	@Before
	public void setUp() {
		descriptor = new BeardDescriptor();
	}

	@Test
	public void testGetDisplayName() {
		assertEquals("Activate Chuck Norris", descriptor.getDisplayName());
	}

	@Test
	public void testIsApplicableGivesTrue() {
		assertTrue(descriptor.isApplicable(mock(AbstractProject.class)
				.getClass()));
	}
}
