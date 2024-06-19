/**
 * (c) 2016 dmulloy2
 */
package net.dmulloy2.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.dmulloy2.BukkitTesting;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

/**
 * @author dmulloy2
 */
public class VersioningTest {

    @Rule
    public MockitoRule experimentRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Before
    public void beforeClass() {
        BukkitTesting.prepare();
    }

    @Test
    public void testSupported() {
        Versioning.setVersion(null);
        // assertEquals(Versioning.getVersion(), Version.MC_111);
        assertTrue(Versioning.isSupported());
    }

    @Test
    public void testUnsupported() {
        Versioning.setVersion(null);
        BukkitTesting.setBukkitVersion("4.2.0-R6.9-SNAPSHOT");
        assertFalse(Versioning.isSupported());
        BukkitTesting.resetBukkitVersion();
    }

    @Test
    public void testDropped() {
        Versioning.setVersion(null);
        BukkitTesting.setBukkitVersion("1.7.10-R0.1-SNAPSHOT");
        assertTrue(Versioning.getVersion().wasDropped());
        assertFalse(Versioning.isSupported());
        BukkitTesting.resetBukkitVersion();
    }
}
