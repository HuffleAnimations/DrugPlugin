package be.huffle.drugplugin;

import org.bukkit.configuration.file.FileConfiguration;

public class DrugConfig
{
	private double droprateHeroinPlant;
	private double droprateHeroinSeed;
	private double droprateCocaineDust;
	private double droprateCocainePlant;

	public DrugConfig(DrugPlugin plugin)
	{
		FileConfiguration fileConfiguration = plugin.getConfig();
		droprateHeroinPlant = fileConfiguration.getDouble("droprateHeroinPlant");
		droprateHeroinSeed = fileConfiguration.getDouble("droprateHeroinSeed");
		droprateCocaineDust = fileConfiguration.getDouble("droprateCocaineDust");
		droprateCocainePlant = fileConfiguration.getDouble("droprateCocainePlant");
	}

	public double getDroprateHeroinPlant()
	{
		return droprateHeroinPlant;
	}

	public void setDroprateHeroinPlant(double droprateHeroinPlant)
	{
		this.droprateHeroinPlant = droprateHeroinPlant;
	}

	public double getDroprateHeroinSeed()
	{
		return droprateHeroinSeed;
	}

	public void setDroprateHeroinSeed(double droprateHeroinSeed)
	{
		this.droprateHeroinSeed = droprateHeroinSeed;
	}

	public double getDroprateCocaineDust()
	{
		return droprateCocaineDust;
	}

	public void setDroprateCocaineDust(double droprateCocaineDust)
	{
		this.droprateCocaineDust = droprateCocaineDust;
	}

	public double getDroprateCocainePlant()
	{
		return droprateCocainePlant;
	}

	public void setDroprateCocainePlant(double droprateCocainePlant)
	{
		this.droprateCocainePlant = droprateCocainePlant;
	}
}
