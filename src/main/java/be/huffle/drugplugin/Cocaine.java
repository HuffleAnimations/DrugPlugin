package be.huffle.drugplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Cocaine implements Listener
{
    private DrugPlugin plugin;
    private File file;
    private Set<SerializableLocation> locations = new HashSet<>();
    private Map<Player, Integer> takenCocainePerPlayer = new HashMap<>();
    private static final String COCAINEPLANT = ChatColor.RESET + "Cocaine Plant" + ChatColor.BLACK;
    private static final String COCAINE = ChatColor.RESET + "Cocaine" + ChatColor.BLACK;
    private static final String COCAINEDUST = ChatColor.RESET + "Cocaine Dust" + ChatColor.BLACK;

    public Cocaine(DrugPlugin plugin)
    {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(),"cocaineLocations.dat");

        //Recipe cocaine plant
        ItemStack result = new ItemStack(Material.SUGAR_CANE);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.setDisplayName(COCAINEPLANT);
        result.setItemMeta(resultMeta);
        Bukkit.addRecipe(new ShapedRecipe(new NamespacedKey(plugin, "cocainePlant"),
                result).shape("aaa", "aaa", "aaa").setIngredient('a', Material.SUGAR_CANE));

        //Recipe cocaine
        ItemStack result2 = new ItemStack(Material.SUGAR);
        ItemMeta result2Meta = result2.getItemMeta();
        result2Meta.setDisplayName(COCAINE);
        result2.setItemMeta(result2Meta);
        Bukkit.addRecipe(new ShapedRecipe(new NamespacedKey(plugin, "cocaine"), result2).shape("aaa")
                .setIngredient('a', Material.SUGAR));

        load();
    }

    /**
    *Called upon when the Cocaine Plant is being planted.
    *If the plant is indeed a Cocaine Plant, then its location will be saved.
     */
    @EventHandler
    public void placeCocainePlant(BlockPlaceEvent event)
    {
        Block placedBlock = event.getBlockPlaced();
        if (placedBlock.getType().equals(Material.SUGAR_CANE) && event.getPlayer().getInventory().getItemInMainHand()
                .getItemMeta().getDisplayName().equals(COCAINEPLANT))
        {
            locations.add(new SerializableLocation(placedBlock));
        }
    }

    /**
    *Called upon when a block is broken.
    *It will check if the broken block is a Cocaine Plant.
     */
    @EventHandler
    public void breakCocainePlant(BlockBreakEvent event)
    {
        Block brokenBlock = event.getBlock();
        if (locations.contains(new SerializableLocation(brokenBlock)))
        {
            event.setDropItems(false);
            checkCocaineDrop(brokenBlock);
        }
    }

    /**
    *This method calculates the chance of Cocaine Dust dropping, when a Cocaine Plant is broken.
    *The chance of Cocaine Dust dropping is 10%
    *It will also check if the block above the Cocaine plant, is also a Cocaine Plant and will then
    *calculate the chance of Cocaine Dust dropping.
     */
    private void checkCocaineDrop(Block block)
    {
        if (Math.random() <= 0.1)
        {
            ItemStack cocaineDust = new ItemStack(Material.SUGAR);
            ItemMeta cocaineDustMeta = cocaineDust.getItemMeta();
            cocaineDustMeta.setDisplayName(COCAINEDUST);
            cocaineDust.setItemMeta(cocaineDustMeta);
            block.getWorld().dropItemNaturally(block.getLocation(), cocaineDust);
        }
        Block blockAbove = block.getRelative(BlockFace.UP);
        if (locations.contains(new SerializableLocation(blockAbove)))
        {
            checkCocaineDrop(blockAbove);
        }
        locations.remove(new SerializableLocation(block));
    }

    /**
    *Called upon when a block is grown.
    *It will check if the block that has grown is sugar cane and if the block under it is the location
    *of a Cocaine Plant.
    *If this is true, the location of the grown sugar cane will be added to the locations of the Cocaine
    *Plants.
     */
    @EventHandler
    public void growCocaine(BlockGrowEvent event)
    {
        Block newBlock = event.getNewState().getBlock();
        Block oldBlock = newBlock.getRelative(BlockFace.DOWN);
        if (event.getNewState().getType().equals(Material.SUGAR_CANE) &&
                locations.contains(new SerializableLocation(oldBlock)))
        {
            locations.add(new SerializableLocation(newBlock));
        }
    }

    /**
    *Called upon when an item is being crafted.
    *Checks if the item being crafted is Cocaine and if the item being used Cocaine Dust is.
    *If the wrong item is being used to craft the Cocaine, the event will be cancelled.
     */
    @EventHandler
    public void checkCocaineRecipe(CraftItemEvent event)
    {
        ItemMeta itemMeta = event.getRecipe().getResult().getItemMeta();

        if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals(COCAINE))
        {
            CraftingInventory inventory = (CraftingInventory)event.getInventory();
            ItemStack[] content = inventory.getMatrix();
            for (ItemStack item : content)
            {
                if (item != null)
                {
                    if (!(item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName()
                            .equals(COCAINEDUST)))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
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
     * This event is called upon, when a player rightclicks an item in his hand.
     * There will be a check if there is something in the player his hand, if yes
     * the next check will be if that item is cocaine.
     * When that check passes, there will be a counter kept, to see how much cocaine the player
     * has taken.
     * When 5 pieces of cocaine is taken the player will die of an overdose.
     * This counter resets to 0 every 5 minutes.
     * The effect that the player will be getting is SPEED.
     */
    @EventHandler
    public void consumeCocaine(PlayerInteractEvent event)
    {
        if (event.getItem() != null)
        {
            ItemMeta item = event.getItem().getItemMeta();
            Player player = event.getPlayer();

            if (item.hasDisplayName() && item.getDisplayName().equals(COCAINE))
            {
                event.getItem().setAmount(event.getItem().getAmount() - 1);

                if (!takenCocainePerPlayer.containsKey(player))
                {
                    takenCocainePerPlayer.put(player, 0);
                }

                int takenCocaine = takenCocainePerPlayer.get(player).intValue() + 1;
                takenCocainePerPlayer.put(player, takenCocaine);

                if (takenCocaine > 0)
                {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> takenCocainePerPlayer.put(player, 0),
                            minutesToTicks(5));
                }

                if (takenCocaine >= 5)
                {
                    player.setHealth(0);
                    Bukkit.broadcastMessage("" + ChatColor.YELLOW + ChatColor.BOLD + player.getName() +
                            " has snorted too much cocaine");
                    takenCocainePerPlayer.put(player, 0);
                }
                PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, secondsToTicks(30),
                        5, false, false);
                player.addPotionEffect(speedEffect);
            }
        }
    }

    /**
    *This method will load in the file with the locations of where the Cocaine Plants are planted.
     */
    private void load()
    {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file)))
        {
            locations = (Set<SerializableLocation>)input.readObject();
        }
        catch (FileNotFoundException e)
        {
            plugin.getLogger().warning("Failed to load cocaine locations: " + e.getMessage());
        }
        catch (IOException | ClassNotFoundException e)
        {
            plugin.getLogger().severe("Failed to load cocaine locations");
            e.printStackTrace();
        }
    }

    /**
    *This method will save the locations of where the Cocaine Plants are planted.
     */
    public void save()
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
