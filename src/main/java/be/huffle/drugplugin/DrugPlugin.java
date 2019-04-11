package be.huffle.drugplugin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class DrugPlugin extends JavaPlugin implements Listener
{
	private Cocaine cocaine;
	private Heroin heroin;

	public DrugPlugin()
	{
		super();
	}
	
	protected DrugPlugin(final JavaPluginLoader loader, final PluginDescriptionFile description, final File dataFolder, final File file)
	{
		super(loader, description, dataFolder, file);
	}
	
	@Override
	public void onLoad()
	{

	}
	
	@Override
	public void onEnable()
	{
		getDataFolder().mkdir();
		cocaine = new Cocaine(this);
		heroin = new Heroin(this);
		Bukkit.getPluginManager().registerEvents(cocaine, this);
		Bukkit.getPluginManager().registerEvents(heroin, this);
	}
	
	@Override
	public void onDisable()
	{
		cocaine.save();
		heroin.save();
	}
}
































