package br.net.gmj.nobookie.LTItemMail;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import br.net.gmj.nobookie.LTItemMail.listener.MailboxVirtualListener;
import br.net.gmj.nobookie.LTItemMail.listener.PlayerListener;
import br.net.gmj.nobookie.LTItemMail.module.BungeeModule;
import br.net.gmj.nobookie.LTItemMail.module.CommandModule;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DataModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.ModelsModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.module.RegistrationModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTExtension;
import br.net.gmj.nobookie.LTItemMail.task.UpdateTask;
import br.net.gmj.nobookie.LTItemMail.task.VersionControlTask;
import br.net.gmj.nobookie.LTItemMail.util.BStats;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

/**
 * 
 * Main class of the plugin. This is typically of no use to developers.
 * 
 * @author Nobookie
 * 
 */
public final class LTItemMail extends JavaPlugin {
	private static LTItemMail instance;
	public FileConfiguration configuration;
	public FileConfiguration language;
	public FileConfiguration models;
	public Connection connection = null;
	public List<Integer> boardsForPlayers = new ArrayList<>();
	public Map<String, List<Integer>> boardsPlayers = new HashMap<>();
	private Long startup;
	@Override
	public final void onLoad() {
		startup = Calendar.getInstance().getTimeInMillis();
	}
	@Override
	public final void onEnable() {
		instance = this;
		final BStats metrics = new BStats(this, 3647);
		loadConfig();
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_ENABLE)) {
			if(isDevBuild()) {
				ConsoleModule.warning("You are running a development build! Be aware that bugs may occur.");
				final File dev = new File(getDataFolder(), ".dev");
				ConfigurationModule.devMode = (dev.exists() && dev.isFile());
			}
			metrics.addCustomChart(new BStats.SimplePie("builds", () -> {
		        return String.valueOf((Integer) ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER));
		    }));
			metrics.addCustomChart(new BStats.SimplePie("language", () -> {
		        return ((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE)).toUpperCase();
		    }));
			ConsoleModule.hello();
			loadLang();
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) {
				getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
				getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeModule());
			}
			loadModels();
			loadDatabase();
			ExtensionModule.getInstance().load();
			for(final ExtensionModule.Function function : ExtensionModule.getInstance().reg().keySet()) {
				final LTExtension extension = (LTExtension) ExtensionModule.getInstance().reg().get(function);
				metrics.addCustomChart(new BStats.SimplePie("extensions", () -> {
			        return extension.getBasePlugin().getDescription().getName();
			    }));
			}
			PermissionModule.load();
			registerListeners();
			runTasks();
			RegistrationModule.setupItems();
			RegistrationModule.setupBlocks();
			new CommandModule();
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.RESOURCE_PACK_DOWNLOAD)) new BukkitRunnable() {
				@Override
				public final void run() {
					FetchUtil.FileManager.download(DataModule.getResourcePackURL(), "LTItemMail-ResourcePack.zip", false);
				}
			}.runTask(this);
			final Long done = Calendar.getInstance().getTimeInMillis() - startup;
			String took = done + "ms";
			if(done >= 1000.0) took = (done / 1000.0) + "s";
			MailboxModule.ready();
			new FetchUtil.Stats().reg();
			ConsoleModule.raw(ChatColor.GREEN + "Plugin took " + took + " to load.");
		} else {
			new BukkitRunnable() {
				@Override
				public final void run() {
					ConsoleModule.severe("Plugin disabled in config.yml.");
					Bukkit.getPluginManager().disablePlugin(instance);
				}
			}.runTaskTimer(this, 10, 10);
		}
	}
	@Override
	public final void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		ExtensionModule.getInstance().unload();
		DatabaseModule.disconnect();
		getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
		getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
	}
	public final void reload() {
		PermissionModule.unload();
		ExtensionModule.getInstance().unload();
		DatabaseModule.disconnect();
		loadConfig();
		loadLang();
		loadModels();
		loadDatabase();
		ExtensionModule.reload().load();
		PermissionModule.load();
	}
	private final void loadConfig() {
		ConfigurationModule.check();
		configuration = ConfigurationModule.load();
		ConfigurationModule.addMissing();
	}
	private final void loadLang() {
		LanguageModule.check();
		language = LanguageModule.load();
		LanguageModule.addMissing();
	}
	private final void loadModels() {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_TEXTURES)) {
			ModelsModule.check();
			models = ModelsModule.load();
			ModelsModule.addMissing();
		}
	}
	private final void loadDatabase() {
		connection = DatabaseModule.connect();
		DatabaseModule.checkForUpdates();
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_CONVERT)) DatabaseModule.convert();
	}
	private final void registerListeners() {
		new PlayerListener();
		new MailboxVirtualListener();
	}
	private final void runTasks() {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_UPDATE_CHECK)) new UpdateTask();
		new VersionControlTask();
	}
	public final Boolean isDevBuild() {
		return (Integer) ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER) > DataModule.getLatestStable();
	}
	public final ClassLoader getLTClassLoader() {
		return getClassLoader();
	}
	public static final LTItemMail getInstance() {
		return instance;
	}
}