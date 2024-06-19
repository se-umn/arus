package hudson.plugins.chucknorris;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Result;

import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.junit.Before;
import org.junit.Test;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CordellWalkerRecorderTest extends TestCase {

	@Rule
	public MockitoRule experimentRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

	private FactGenerator mockGenerator;
	private CordellWalkerRecorder recorder;

	@Before
	@Override
	public void setUp() {
		mockGenerator = mock(FactGenerator.class);
		recorder = new CordellWalkerRecorder(mockGenerator);
	}

	@Test
	public void testGetProjectActionWithNoLastBuildGivesNullAction() {
		AbstractProject mockProject = mock(AbstractProject.class);
		when(mockProject.getLastBuild()).thenReturn(null);
		assertNull(recorder.getProjectAction(mockProject));
	}

	@Test
	public void testGetProjectActionHavingLastBuildGivesRoundhouseAction() {
		AbstractProject mockProject = mock(AbstractProject.class);
		Build mockBuild = mock(Build.class);

		when(mockProject.getLastBuild()).thenReturn(mockBuild);
		when(mockBuild.getResult()).thenReturn(Result.SUCCESS);
		when(mockGenerator.random()).thenReturn(
				"Chuck Norris burst the dot com bubble.");

		Action action = recorder.getProjectAction(mockProject);

		assertTrue(action instanceof RoundhouseAction);
		assertEquals(Style.THUMB_UP, ((RoundhouseAction) action).getStyle());
		assertNotNull(((RoundhouseAction) action).getFact());
	}

	@Test
	public void testPerformWithFailureResultAddsRoundHouseActionWithBadAssStyleAndExpectedFact()
			throws Exception {
		AbstractBuild mockBuild = mock(AbstractBuild.class);
		when(mockBuild.getResult()).thenReturn(Result.FAILURE);

		ArgumentCaptor<RoundhouseAction> actionCaptor = ArgumentCaptor.forClass(RoundhouseAction.class);
		doNothing().when(mockBuild).addAction(actionCaptor.capture());

		when(mockGenerator.random()).thenReturn(
				"Chuck Norris burst the dot com bubble.");

		recorder.perform(mockBuild, mock(Launcher.class),
				mock(BuildListener.class));

		RoundhouseAction action = actionCaptor.getValue();

		verify(mockBuild, times(1)).addAction(same(action));
		assertEquals(Style.BAD_ASS, (action).getStyle());
		assertEquals("Chuck Norris burst the dot com bubble.", action.getFact());
	}
}
