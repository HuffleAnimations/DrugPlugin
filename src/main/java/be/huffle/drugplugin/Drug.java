package be.huffle.drugplugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Drug implements Listener
{
	protected final DrugPlugin plugin;
	private File file;
	private Set<SerializableLocation> locations = new HashSet<>();
	private static Set<Player> playersThatHaveDied = new HashSet<>();
	private static Set<Player> playersThatAreHigh = new HashSet<>();

	public Drug(DrugPlugin plugin, String name)
	{
		this.plugin = plugin;
		file = new File(plugin.getDataFolder(), name + "Locations.dat");

		load();
	}

	/**
	 * @param seconds
	 * @return the seconds converted to the amount of ticks
	 */
	public int secondsToTicks(int seconds)
	{
		return 20 * seconds;
	}

	/**
	 * @param minutes
	 * @return the minutes converted to the amount of ticks
	 */
	public int minutesToTicks(int minutes)
	{
		return secondsToTicks(minutes * 60);
	}

	/**
	 * @param chance of certain event to happen
	 * @return {@code true} when a random generated number between 0 and 1 is lower then the given chance
	 */
	public boolean isChance(double chance)
	{
		return (Math.random() <= chance);
	}

	/**
	 * @param itemStack
	 * @return {@code true} when the player is holding an item in his/her hand
	 */
	public boolean hasItemInHand(ItemStack itemStack)
	{
		return (itemStack != null);
	}

	/**
	 * @param item
	 * @param name The name of the Item
	 * @return {@code true} when the item has a displayname that is the given name
	 */
	public boolean hasCorrectName(ItemMeta item, String name)
	{
		return (item.hasDisplayName() && item.getDisplayName().equals(name));
	}

	/**
	 * Called upon when the player dies, it will put the property hasPlayerDied on true
	 */
	@EventHandler
	public void playerDied(PlayerDeathEvent event)
	{
		Player player = (Player)event.getEntity();
		setHasPlayerDied(true, player);
	}

	/**
	 * @param block The block that you want to compare
	 * @param material The material that the block has to equal
	 * @return {@code true} when the material equals the block
	 */
	public boolean isMaterialEqual(Block block, Material material)
	{
		return (block.getType().equals(material));
	}

	public boolean isRightClick(PlayerInteractEvent event)
	{
		return (event.getAction().equals(Action.RIGHT_CLICK_AIR) ||
				event.getAction().equals(Action.RIGHT_CLICK_BLOCK));
	}

	/**
	 * This method will load in the file with the locations of where the drug plants are planted.
	 */
	private void load()
	{
		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file)))
		{
			locations = (Set<SerializableLocation>)input.readObject();
		}
		catch (FileNotFoundException e)
		{
			plugin.getLogger().warning("Failed to load drug locations: " + e.getMessage());
		}
		catch (IOException | ClassNotFoundException e)
		{
			plugin.getLogger().severe("Failed to load drug locations");
			e.printStackTrace();
		}
	}

	/**
	 * This method will save the locations of where the drug plants are planted.
	 */
	public void save()
	{
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file)))
		{
			output.writeObject(locations);
		}
		catch (IOException e)
		{
			plugin.getLogger().severe("Failed to save drug locations");
			e.printStackTrace();
		}
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public Set<SerializableLocation> getLocations()
	{
		return locations;
	}

	public void addLocation(Block block)
	{
		locations.add(new SerializableLocation(block));
	}

	public void removeLocation(Block block)
	{
		locations.remove(new SerializableLocation(block));
	}

	public boolean containsLocation(Block block)
	{
		return (locations.contains(new SerializableLocation(block)));
	}

	public boolean hasPlayerDied(Player player)
	{

		return playersThatHaveDied.contains(player);
	}

	public void setHasPlayerDied(boolean hasPlayerDied, Player player)
	{
		if (hasPlayerDied)
		{
			playersThatHaveDied.add(player);
		}
		else
		{
			playersThatHaveDied.remove(player);
		}
	}

	public boolean hasPlayerTakenDrug(Player player)
	{
		return playersThatAreHigh.contains(player);
	}

	public void setHasPlayerTakenDrug(boolean hasPlayerTakenDrug, Player player)
	{
		if (hasPlayerTakenDrug)
		{
			playersThatAreHigh.add(player);
		}
		else
		{
			playersThatAreHigh.remove(player);
		}
	}
}
