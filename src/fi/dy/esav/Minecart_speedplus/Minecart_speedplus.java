package fi.dy.esav.Minecart_speedplus;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Minecart_speedplus extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");

	private final Minecart_speedplusVehicleListener VehicleListener = new Minecart_speedplusVehicleListener(this);
	private final Minecart_speedplusSignListener SignListener = new Minecart_speedplusSignListener(this);

	private double speedmultiplier = -1;

	/**
	 * Set/create metadata
	 * @param obj
	 * @param key
	 * @param value
	 */
	public void setMetadata(Metadatable obj, String key, Object value) {
		obj.setMetadata(key,new FixedMetadataValue(this,value));
	}
	/**
	 * Get metadata created by the current plugin
	 * @param obj
	 * @param key
	 * @return metadata object if available
	 */
	public Object getMetadata(Metadatable obj, String key) {
		for(MetadataValue val:obj.getMetadata(key)){
			if(val.getOwningPlugin()==this)
				return val.value();
		}
		return null;
	}

	public double getSpeedMultiplier() {
		if(speedmultiplier<0 && getConfig().get("multiplier")!=null)
			speedmultiplier=getConfig().getDouble("multiplier");
		return speedmultiplier;
	}

	public void setSpeedMultiplier(double multiplier) {
		if(multiplier>0.0 && multiplier<=4.00){
			speedmultiplier = multiplier;
			getConfig().set("multiplier",multiplier);
			return;
		}
		throw new RuntimeException("Invalid input");
	}

	public void onEnable() {
		this.log.info(getDescription().getName() + " version " + getDescription().getVersion() + " started.");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.VehicleListener, this);
		pm.registerEvents(this.SignListener, this);
		saveDefaultConfig();
		log.info("max speed multiplier="+getSpeedMultiplier());
	}

	public void onDisable() {
		this.log.info(getDescription().getName() + " version " + getDescription().getVersion() + " stopped.");
		saveConfig();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		double multiplier;
		if(cmd.getName().equalsIgnoreCase("mspdebug")){
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED+"You can only execute this command as a player");
				return true;
			}
			boolean dbg=false;
			try {
				if (args[0].equalsIgnoreCase("on"))
					dbg = true;
				else if (args[0].equalsIgnoreCase("off"))
					dbg = false;
				else
					return false;
			}catch (Exception ex){
				return false;
			}
			setMetadata((Player)sender,"debug",dbg);
			sender.sendMessage(ChatColor.GOLD+"Debug mode "+(dbg?"en":"dis")+"abled");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("msp")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				if (!player.hasPermission("msp.cmd")) {
					player.sendMessage("You don't have permission to do that");
					return true;
				}
			}
			try {
				multiplier = Double.parseDouble(args[0]);
				setSpeedMultiplier(multiplier);
				sender.sendMessage(ChatColor.GOLD + "Multiplier for new minecarts set to " + multiplier);
				return true;
			} catch (Exception e) {
				sender.sendMessage(ChatColor.RED + "Invalid input");
				return false;
			}
		}
		return false;
	}
}
