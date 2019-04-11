package be.huffle.drugplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.*;

public class Heroin implements Listener
{
	private static final String HEROINSEED = ChatColor.RESET + "Heroin Seed" + ChatColor.BLACK;
	private static final String HEROINPLANT = ChatColor.RESET + "Heroin Plant" + ChatColor.BLACK;
	private static final String HEROIN = ChatColor.RESET + "Heroin" + ChatColor.BLACK;
	private Set<SerializableLocation> locations = new HashSet<>();
	private File file;
	private DrugPlugin plugin;
	private Random random = new Random();
	private Map<Location, BukkitTask> taskMap = new HashMap<>();
	private Map<Player, Integer> takenHeroinPerPlayer = new HashMap<>();
	private boolean hasPlayerDied = false;
	private Map<Player, BukkitTask> effectTaskMap = new HashMap<>();
	private Map<Player, BukkitTask> teleportTaskMap = new HashMap<>();

	public Heroin(DrugPlugin plugin)
	{
		this.plugin = plugin;
		file = new File(plugin.getDataFolder(), "heroinLocations.dat");

		// Heroin Seed recipe
		ItemStack result = new ItemStack(Material.POPPY);
		ItemMeta resultMeta = result.getItemMeta();
		resultMeta.setDisplayName(HEROINSEED);
		result.setItemMeta(resultMeta);
		Bukkit.addRecipe(new ShapedRecipe(new NamespacedKey(plugin, "heroinSeed"), result)
				.shape("aaa", "aaa", "aaa").setIngredient('a', Material.POPPY));

		// Recipe Heroin
		ItemStack melt = new ItemStack(Material.GHAST_TEAR);
		ItemMeta meltMeta = melt.getItemMeta();
		meltMeta.setDisplayName(HEROIN);
		melt.setItemMeta(meltMeta);
		FurnaceRecipe heroin = new FurnaceRecipe(new NamespacedKey(plugin, "heroin"), melt,
				Material.ROSE_BUSH, 10, 20);
		Bukkit.addRecipe(heroin);

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
	 * This event is called upon when a block is being placed.
	 * There will be a check if the placed block is a Heroin Seed is being placed.
	 * After 15-20 minutes the poppy will become a rose bush, this indicates that the Heroin Plant is ready
	 * to be harvested.
	 * The location of this plant will also be saved.
	 */
	@EventHandler
	public void placeHeroinSeed(BlockPlaceEvent event)
	{
		Block placedBlock = event.getBlockPlaced();

		if(placedBlock.getType().equals(Material.POPPY) && event.getPlayer().getInventory()
				.getItemInMainHand().getItemMeta().getDisplayName().equals(HEROINSEED))
		{
			BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () ->
			{
				String blockStringLowerBlock = "minecraft:rose_bush[half=lower]";
				BlockData lowerBlockData = Bukkit.createBlockData(blockStringLowerBlock);
				placedBlock.setType(Material.DANDELION);
				placedBlock.setBlockData(lowerBlockData);
				Block above = placedBlock.getRelative(BlockFace.UP);
				String blockStringUpperBlock = "minecraft:rose_bush[half=upper]";
				BlockData upperBlockData = Bukkit.createBlockData(blockStringUpperBlock);
				above.setBlockData(upperBlockData);
				locations.add(new SerializableLocation(above));
			}, secondsToTicks(random.nextInt(5) + 15));
			locations.add(new SerializableLocation(placedBlock));
			taskMap.put(placedBlock.getLocation(), task);
		}
	}

	/**
	 * This event is called upon when a block is broken.
	 * There will be a check if at that location there is a Heroin Plant
	 * If it is, then the task to grow to a Heroin Plant get cancelled and the player gets the Heroin Seed back.
	 */
	@EventHandler
	public void breakHeroinSeed(BlockBreakEvent event)
	{
		Block brokenBlock = event.getBlock();

		if (locations.contains(new SerializableLocation(brokenBlock)) && brokenBlock.getType()
				.equals(Material.POPPY))
		{
			BukkitTask task = taskMap.get(brokenBlock.getLocation());
			task.cancel();
			taskMap.remove(brokenBlock.getLocation());

			event.setDropItems(false);

			ItemStack heroinSeed = new ItemStack(Material.POPPY);
			ItemMeta heroinSeedMeta = heroinSeed.getItemMeta();
			heroinSeedMeta.setDisplayName(HEROINSEED);
			heroinSeed.setItemMeta(heroinSeedMeta);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), heroinSeed);

			Block above = brokenBlock.getRelative(BlockFace.UP);
			locations.remove(new SerializableLocation(brokenBlock));
			locations.remove(new SerializableLocation(above));
		}
	}

	/**
	 * This event is called upon when a block is broken.
	 * There will be a check if at that location there is a Heroin Plant
	 * If it is a Heroin Plant then, when broken there is a 20% chance to get the Heroin Plant
	 * and a 60% cnance to get a Heroin Seed.
	 */
	@EventHandler
	public void breakHeroinPlant(BlockBreakEvent event)
	{
		Block brokenBlock = event.getBlock();

		if (locations.contains(new SerializableLocation(brokenBlock)) && brokenBlock.getType()
				.equals(Material.ROSE_BUSH))
		{
			event.setDropItems(false);
			if (Math.random() <= 0.2)
			{
				ItemStack heroinPlant = new ItemStack(Material.ROSE_BUSH);
				ItemMeta heroinPlantMeta = heroinPlant.getItemMeta();
				heroinPlantMeta.setDisplayName(HEROINPLANT);
				heroinPlant.setItemMeta(heroinPlantMeta);
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), heroinPlant);
			}
			if (Math.random() <= 0.6)
			{
				ItemStack heroinSeed = new ItemStack(Material.POPPY);
				ItemMeta heroinSeedMeta = heroinSeed.getItemMeta();
				heroinSeedMeta.setDisplayName(HEROINSEED);
				heroinSeed.setItemMeta(heroinSeedMeta);
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), heroinSeed);
			}
			Block above = brokenBlock.getRelative(BlockFace.UP);
			locations.remove(new SerializableLocation(brokenBlock));
			locations.remove(new SerializableLocation(above));
		}
	}

	/**
	 * This event is called upon when a player tries to move an item in his inventory.
	 *
	 */
	@EventHandler
	public void checkHeroinRecipe(InventoryClickEvent event)
	{
		Player player = (Player)event.getWhoClicked();
		Inventory inventory = event.getInventory();
		ItemStack currentItem = event.getCurrentItem();

		if (currentItem != null)
		{
			if (!isHeroinPlant(currentItem) && inventory.getType()
					.equals(InventoryType.FURNACE) && currentItem.getType()
					.equals(Material.ROSE_BUSH))
			{
				player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Why would you burn a rose bush?");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void consumeHeroin(PlayerInteractEvent event)
	{
		if (event.getItem() != null)
		{
			ItemMeta item = event.getItem().getItemMeta();
			Player player = event.getPlayer();

			if (item.hasDisplayName() && item.getDisplayName().equals(HEROIN))
			{
				if (!hasTakenHeroin(player))
				{
					event.getItem().setAmount(event.getItem().getAmount() - 1);

					if (!takenHeroinPerPlayer.containsKey(player) || hasPlayerDied)
					{
						takenHeroinPerPlayer.put(player, 0);
					}

					int takenHeroin = takenHeroinPerPlayer.get(player).intValue() + 1;
					takenHeroinPerPlayer.put(player,takenHeroin);

					if (takenHeroin > 0)
					{
						Bukkit.getScheduler().runTaskLater(plugin, () -> takenHeroinPerPlayer.put(player,0),
								minutesToTicks(5));
					}

					if (takenHeroin >= 3)
					{
						player.setHealth(0);
						Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName() +
								" has overdosed on heroin");
						takenHeroinPerPlayer.put(player, 0);
					}
					else
					{
						Location playerLocation = player.getLocation();

						BukkitTask effectTask = Bukkit.getScheduler().runTaskLater(plugin, () ->
						{
							PotionEffect slowFallingEffect = new PotionEffect(PotionEffectType.SLOW_FALLING, secondsToTicks(8),
									10, false, false);
							player.addPotionEffect(slowFallingEffect);
						}, secondsToTicks(8));

						BukkitTask teleportTask = Bukkit.getScheduler().runTaskLater(plugin, () ->
						{
							player.teleport(playerLocation);
							player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "But it was just a dream...");
						}, secondsToTicks(16));

						PotionEffect invisibleEffect = new PotionEffect(PotionEffectType.INVISIBILITY, secondsToTicks(16),
								5, false, false);
						player.addPotionEffect(invisibleEffect);

						PotionEffect levitateEffect = new PotionEffect(PotionEffectType.LEVITATION, secondsToTicks(8),
								5, false, false);
						player.addPotionEffect(levitateEffect);
						player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "It starts to feel like you're flying");
						effectTaskMap.put(player, effectTask);
						teleportTaskMap.put(player, teleportTask);
					}
				}
				else
				{
					player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD +
							"You can't take heroin while you are levitating");
				}
			}
		}
	}

	private boolean hasTakenHeroin(Player player)
	{
		return (player.hasPotionEffect(PotionEffectType.LEVITATION) ||
				player.hasPotionEffect(PotionEffectType.SLOW_FALLING));
	}

	@EventHandler
	public void playerDied(PlayerDeathEvent event)
	{
		Player player = (Player)event.getEntity();
		hasPlayerDied = true;
		BukkitTask effectTask = effectTaskMap.remove(player);
		BukkitTask teleportTask = teleportTaskMap.remove(player);

		effectTask.cancel();
		teleportTask.cancel();
	}

	/**
	 *
	 * @param stack
	 * @return {@code true} when the itemstack is a Heroin Plant {@code false} when the
	 * itemstack is not a Heroin Plant
	 */
	private boolean isHeroinPlant(ItemStack stack)
	{
		return stack.hasItemMeta() && stack.getItemMeta().hasDisplayName() && stack.getItemMeta()
				.getDisplayName().equals(HEROINPLANT);

	}

	/**
	 * This method will load in the file with the locations of where the Heroin Seeds are planted.
	 */
	private void load()
	{
		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file)))
		{
			locations = (Set<SerializableLocation>)input.readObject();
		}
		catch (FileNotFoundException e)
		{
			plugin.getLogger().warning("Failed to load heroin locations: " + e.getMessage());
		}
		catch (IOException | ClassNotFoundException e)
		{
			plugin.getLogger().severe("Failed to load heroin locations");
			e.printStackTrace();
		}
	}

	/**
	 * This method will save the locations of where the Heroin Seeds are planted.
	 */
	public  void save()
	{
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file)))
		{
			output.writeObject(locations);
		}
		catch (IOException e)
		{
			plugin.getLogger().severe("Failed to save cocaine locations");
			e.printStackTrace();
		}
	}
}
