package br.net.gmj.nobookie.LTItemMail.module.ext;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;

public final class LTPlugMan implements Listener {
	public LTPlugMan() {
		try {
			Plugin plugMan = null;
			if(Bukkit.getPluginManager().isPluginEnabled("PlugManX")) {
				 plugMan = Bukkit.getPluginManager().getPlugin("PlugManX");
			} else if(Bukkit.getPluginManager().isPluginEnabled("PlugMan")) plugMan = Bukkit.getPluginManager().getPlugin("PlugMan");
			if(plugMan != null) {
				ConsoleModule.warning("PlugMan detected! Reloading LT Item Mail with PlugMan can cause issues. Safe Guard activated!");
				if(!ConfigurationModule.devMode) Bukkit.getPluginManager().registerEvents(this, LTItemMail.getInstance());
				final FileConfiguration config = plugMan.getConfig();
				final List<String> ignored = config.getStringList("ignored-plugins");
				if(!ignored.contains(LTItemMail.getInstance().getDescription().getName())){
					ignored.add(LTItemMail.getInstance().getDescription().getName());
					config.set("ignored-plugins", ignored);
					config.save(new File(plugMan.getDataFolder(), "config.yml"));
				}
			}
		} catch(final Exception e) {}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onServerCommandEvent(final ServerCommandEvent event) {
		final String[] command = event.getCommand().split(" ");
		event.setCancelled(process(event.getSender(), command));
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlayerCommandEvent(final PlayerCommandPreprocessEvent event) {
		final String[] command = event.getMessage().replaceAll("/", "").split(" ");
		event.setCancelled(process(event.getPlayer(), command));
	}
	private final Boolean process(final CommandSender sender, final String[] command) {
		Boolean plugman = false;
		for(final String cmd : command) {
			if(cmd.equalsIgnoreCase("plugman")) plugman = true;
			if(plugman && cmd.equalsIgnoreCase(LTItemMail.getInstance().getDescription().getName())) {
				if(sender instanceof Player) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.DARK_RED + "PlugMan not allowed!");
				ConsoleModule.severe("PlugMan not allowed! My plugin works with some reflection methods and PlugMan will break everything if I let you continue.");
				ConsoleModule.severe("If you need to reload my plugin, please restart the server!");
				return true;
			}
		}
		return false;
	}
}