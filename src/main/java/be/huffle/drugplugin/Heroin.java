package be.huffle.drugplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

public class Heroin extends Drug implements Listener
{
	private static final String HEROINSEED = ChatColor.RESET + "Heroin Seed" + ChatColor.BLACK;
	private static final String HEROINPLANT = ChatColor.RESET + "Heroin Plant" + ChatColor.BLACK;
	private static final String HEROIN = ChatColor.RESET + "Heroin" + ChatColor.BLACK;
	private Random random = new Random();
	private Map<Location, BukkitTask> taskMap = new HashMap<>();
	private Map<Player, Integer> takenHeroinPerPlayer = new HashMap<>();
	private Map<Player, BukkitTask> effectTaskMap = new HashMap<>();
	private Map<Player, BukkitTask> teleportTaskMap = new HashMap<>();

	public Heroin(DrugPlugin plugin)
	{
		super(plugin, "heroin");

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
	}

	/**
	 * This event is called upon when a block is being placed.
	 * There will be a check if the placed block is a Heroin Seed is being placed..
	 */
	@EventHandler
	public void placeHeroinSeed(BlockPlaceEvent event)
	{
		Block placedBlock = event.getBlockPlaced();
		ItemMeta itemMeta = event.getPlayer().getInventory().getItemInMainHand().getItemMeta();

		if(isMaterialEqual(placedBlock, Material.POPPY) && hasCorrectName(itemMeta, HEROINSEED))
		{
			changePoppyToRoseBush(placedBlock);
		}
	}

	/**
	 * This method will change the given block to a rose bush after 15-20 minutes
	 * @param block The block that will change to a rose bush
	 */
	public void changePoppyToRoseBush(Block block)
	{
		BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () ->
		{
			String blockStringLowerBlock = "minecraft:rose_bush[half=lower]";
			BlockData lowerBlockData = Bukkit.createBlockData(blockStringLowerBlock);
			block.setType(Material.DANDELION);
			block.setBlockData(lowerBlockData);
			Block above = block.getRelative(BlockFace.UP);
			String blockStringUpperBlock = "minecraft:rose_bush[half=upper]";
			BlockData upperBlockData = Bukkit.createBlockData(blockStringUpperBlock);
			above.setBlockData(upperBlockData);
			addLocation(above);
		}, secondsToTicks(random.nextInt(5) + 15));
		addLocation(block);
		taskMap.put(block.getLocation(), task);
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

		if (containsLocation(brokenBlock) && isMaterialEqual(brokenBlock, Material.POPPY))
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
			removeLocation(brokenBlock);
			removeLocation(above);
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

		if (containsLocation(brokenBlock) && isMaterialEqual(brokenBlock, Material.ROSE_BUSH))
		{
			event.setDropItems(false);

			calculateDropChance(event);

			Block above = brokenBlock.getRelative(BlockFace.UP);
			removeLocation(brokenBlock);
			removeLocation(above);
		}
	}

	/**
	 * There is a 20% chance to get the Heroin Plant and a 60% cnance to get a Heroin Seed.
	 * @param event
	 */
	public void calculateDropChance(BlockBreakEvent event)
	{
		if (isChance(0.2))
		{
			ItemStack heroinPlant = new ItemStack(Material.ROSE_BUSH);
			ItemMeta heroinPlantMeta = heroinPlant.getItemMeta();
			heroinPlantMeta.setDisplayName(HEROINPLANT);
			heroinPlant.setItemMeta(heroinPlantMeta);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), heroinPlant);
		}
		if (isChance(0.6))
		{
			ItemStack heroinSeed = new ItemStack(Material.POPPY);
			ItemMeta heroinSeedMeta = heroinSeed.getItemMeta();
			heroinSeedMeta.setDisplayName(HEROINSEED);
			heroinSeed.setItemMeta(heroinSeedMeta);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), heroinSeed);
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

		if (hasItemInHand(currentItem))
		{
			notifyWhenRoseBushIsBeingBurned(player, currentItem, inventory, event);
		}
	}

	/**
	 * The player will be notified when they try to burn a normal rose bush
	 * @param player
	 * @param currentItem
	 * @param inventory
	 * @param event
	 */
	public void notifyWhenRoseBushIsBeingBurned(Player player, ItemStack currentItem, Inventory inventory,
												InventoryClickEvent event)
	{
		if (!isHeroinPlant(currentItem) && inventory.getType()
				.equals(InventoryType.FURNACE) && currentItem.getType()
				.equals(Material.ROSE_BUSH))
		{
			player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Why would you burn a rose bush?");
			event.setCancelled(true);
		}
	}

	/**
	 * This event is called upon when the player interacts with an item, there will be a check if there is an item
	 * in the player's hand.
	 * @param event
	 */
	@EventHandler
	public void consumeHeroin(PlayerInteractEvent event)
	{
		if (hasItemInHand(event.getItem()) && isRightClick(event))
		{
			ItemMeta item = event.getItem().getItemMeta();
			Player player = event.getPlayer();

			effectHeroin(player, item, event);
		}
	}

	/**
	 * There will be a check if the drug is Heroin and if  the player is already high, if he/she is, then they
	 * will get notified that it is impossible to tak another drug when high.
	 * @param player
	 * @param item
	 * @param event
	 */
	public void effectHeroin(Player player, ItemMeta item, PlayerInteractEvent event)
	{
		if (hasCorrectName(item, HEROIN))
		{
			if (!hasPlayerTakenDrug(player))
			{
				event.getItem().setAmount(event.getItem().getAmount() - 1);

				resultWhenPlayerConsumesHeroin(player);
			}
			else
			{
				player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD +
						"You can't take heroin while you're high");
			}
		}
	}

	/**
	 * This method shows what happens when the drug is correctly consumed.
	 * The player will start levitating in the air for 8 seconds, while invisible for 16 seconds.
	 * @param player
	 */
	public void resultWhenPlayerConsumesHeroin(Player player)
	{
		if (!takenHeroinPerPlayer.containsKey(player) || hasPlayerDied())
		{
			takenHeroinPerPlayer.put(player, 0);
			setHasPlayerDied(false);
			if (!effectTaskMap.isEmpty() && !teleportTaskMap.isEmpty())
			{
				BukkitTask effectTask = effectTaskMap.remove(player);
				BukkitTask teleportTask = teleportTaskMap.remove(player);

				effectTask.cancel();
				teleportTask.cancel();
			}
		}

		int takenHeroin = takenHeroinPerPlayer.get(player).intValue() + 1;
		takenHeroinPerPlayer.put(player,takenHeroin);

		if (takenHeroin == 1)
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
			createTasks(player);

			PotionEffect invisibleEffect = new PotionEffect(PotionEffectType.INVISIBILITY, secondsToTicks(16),
					5, false, false);
			player.addPotionEffect(invisibleEffect);

			PotionEffect levitateEffect = new PotionEffect(PotionEffectType.LEVITATION, secondsToTicks(8),
					5, false, false);
			player.addPotionEffect(levitateEffect);
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "It starts to feel like you're flying");
			setHasPlayerTakenDrug(true, player);
			Bukkit.getScheduler().runTaskLater(plugin, () -> setHasPlayerTakenDrug(false,
					player), secondsToTicks(16));
		}
	}

	/**
	 * This method shows extra effect while high on Heroin, after the 8 seconds of levitating, the player starts to
	 * slowly float towards the ground for 8 seconds.
	 * After those 8 seconds, the player gets teleported to the place where he/she consumed the heroin.
	 * @param player
	 */
	public void createTasks(Player player)
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

		effectTaskMap.put(player, effectTask);
		teleportTaskMap.put(player, teleportTask);
	}

	/**
	 *
	 * @param stack
	 * @return {@code true} when the itemstack is a Heroin Plant {@code false} when the
	 * itemstack is not a Heroin Plant
	 */
	private boolean isHeroinPlant(ItemStack stack)
	{
		return (stack.hasItemMeta() && hasCorrectName(stack.getItemMeta(), HEROINPLANT));

	}
}
