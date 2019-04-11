package be.huffle.drugplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.Objects;

public class SerializableLocation implements Serializable
{
	private final int x;
	private final int y;
	private final int z;
	private final String world;

	public SerializableLocation(Location location)
	{
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
		world = location.getWorld().getName();
	}

	public SerializableLocation(Block placedBlock)
	{
		this(placedBlock.getLocation());
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public String getWorldName()
	{
		return world;
	}

	public World getWorld()
	{
		return Bukkit.getWorld(world);
	}

	public Block getBlock()
	{
		return getWorld().getBlockAt(x, y, z);
	}

	public Location getLocation()
	{
		return getBlock().getLocation();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SerializableLocation that = (SerializableLocation) o;
		return x == that.x &&
				y == that.y &&
				z == that.z &&
				Objects.equals(world, that.world);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(x, y, z, world);
	}
}
