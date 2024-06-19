/**
 * (c) 2016 dmulloy2
 */
package net.dmulloy2;

import java.util.logging.Logger;

import net.minecraft.server.v1_16_R3.DispenserRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_16_R3.util.Versioning;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

/**
 * @author dmulloy2
 */

public class BukkitTesting
{
	private static String fakeVersion;

	public static void prepare()
	{
			resetSingleton(Bukkit.class,"server");
			System.setProperty("swornapi.debug", "true");

			DispenserRegistry.init(); // Basically registers everything

			// Mock the server object
			Server mockedServer = mock(Server.class);

			when(mockedServer.getLogger()).thenReturn(Logger.getLogger("Minecraft"));
			when(mockedServer.getName()).thenReturn("Mock Server");
			when(mockedServer.getVersion()).thenReturn(CraftServer.class.getPackage().getImplementationVersion());
			when(mockedServer.getBukkitVersion()).thenAnswer(
					(Answer<String>) invocation -> fakeVersion != null ? fakeVersion : Versioning.getBukkitVersion());
			when(mockedServer.getUnsafe()).thenReturn(CraftMagicNumbers.INSTANCE);

			when(mockedServer.getItemFactory()).thenReturn(CraftItemFactory.instance());
			when(mockedServer.isPrimaryThread()).thenReturn(true);

			// Inject this fake server
			Bukkit.setServer(mockedServer);
	}


    public static void resetSingleton(Class clazz, String fieldName) {
        Field instance;
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

	public static void setBukkitVersion(String version)
	{
		fakeVersion = version;
	}

	public static void resetBukkitVersion()
	{
		setBukkitVersion(null);
	}
}