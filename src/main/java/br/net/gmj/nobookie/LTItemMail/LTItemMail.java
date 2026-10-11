/*
 * Copyright (C) 2026  Murilo Amaral Nappi
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package br.net.gmj.nobookie.LTItemMail;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.zaxxer.hikari.HikariDataSource;

import br.net.gmj.nobookie.LTItemMail.listener.MailboxVirtualListener;
import br.net.gmj.nobookie.LTItemMail.listener.PlayerListener;
import br.net.gmj.nobookie.LTItemMail.module.CommandModule;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DataModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.MetricsModule;
import br.net.gmj.nobookie.LTItemMail.module.ModelsModule;
import br.net.gmj.nobookie.LTItemMail.module.MultiServerModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.module.RegistrationModule;
import br.net.gmj.nobookie.LTItemMail.task.ManifestTask;
import br.net.gmj.nobookie.LTItemMail.task.UpdateTask;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

public final class LTItemMail extends JavaPlugin {
	private static LTItemMail instance;
	public FileConfiguration configuration;
	public FileConfiguration language;
	public FileConfiguration models;
	public HikariDataSource dataSource = null;
	public Connection connection = null;
	public final ArrayList<Integer> boardsForPlayers = new ArrayList<>();
	public final HashMap<String, List<Integer>> boardsPlayers = new HashMap<>();
	public final ArrayList<JavaPlugin> apiHandlers = new ArrayList<>();
	private Long startup;
	private Integer serverVer;
	private Integer serverRev;
	@Override
	public final void onLoad() {
		instance = this;
		startup = Calendar.getInstance().getTimeInMillis();
		loadConfig();
		ConsoleModule.welcome();
		if(Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[0]) >= 26) {
			serverVer = Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[0]);
			serverRev = Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[1]);
		} else {
			serverVer = Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[1]);
			serverRev = Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[2]);
		}
		ConsoleModule.warning("Server version: " + serverVer + "." + serverRev);
	}
	@Override
	public final void onEnable() {
		if(!(Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_ENABLE)) {
			ConsoleModule.severe("😢 Plugin disabled in config.yml.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		if(isDevBuild()) {
			ConsoleModule.warning("⚠️ You are running a development build! Be aware that bugs may occur.");
			final File dev = new File(getDataFolder(), ".dev");
			ConfigurationModule.devMode = (dev.exists() && dev.isFile());
		}
		loadLang();
		MultiServerModule.load();
		loadModels();
		loadDatabase();
		ExtensionModule.getInstance().load();
		new MetricsModule();
		PermissionModule.load();
		registerListeners();
		runTasks();
		RegistrationModule.setupItems();
		RegistrationModule.setupBlock();
		new CommandModule();
		MailboxModule.ready();
		new FetchUtil.Stats();
		final Long done = Calendar.getInstance().getTimeInMillis() - startup;
		String took = done + "ms" + ChatColor.GREEN;
		if(done >= 1000.0) took = (done / 1000.0) + "s" + ChatColor.GREEN;
		ConsoleModule.raw(ChatColor.GREEN + "👍🏼 Plugin took " + ChatColor.WHITE + took + " to load.");
	}
	@Override
	public final void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		MetricsModule.shutdown();
		PermissionModule.unload();
		MultiServerModule.unload();
		ExtensionModule.getInstance().unload();
		DatabaseModule.disconnect();
		ConsoleModule.info("Goodbye!");
	}
	public final void reload() {
		ExtensionModule.getInstance().unload();
		DatabaseModule.disconnect();
		loadConfig();
		loadLang();
		loadModels();
		loadDatabase();
		ExtensionModule.reload().load();
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
		dataSource = DatabaseModule.connect();
		if(dataSource != null) try {
			connection = dataSource.getConnection();
			DatabaseModule.checkForUpdates();
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.DATABASE_CONVERT)) DatabaseModule.convert();
		} catch(final SQLException e) {
			ConsoleModule.debug(getClass(), "Could not retrieve SQL connection.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
	}
	private final void registerListeners() {
		new PlayerListener();
		new MailboxVirtualListener();
	}
	private final void runTasks() {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_UPDATE_CHECK)) new UpdateTask();
		new ManifestTask();
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.RESOURCE_PACK_DOWNLOAD)) new BukkitRunnable() {
			@Override
			public final void run() {
				FetchUtil.FileManager.download(DataModule.RESOURCE_ARTIFACT, "LTItemMail-ResourcePack.zip", false);
			}
		}.runTaskAsynchronously(this);
	}
	public final Boolean isDevBuild() {
		return (Integer) ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER) > DataModule.STABLE;
	}
	public final ClassLoader getLTClassLoader() {
		return getClassLoader();
	}
	public final Integer getServerVer() {
		return serverVer;
	}
	public final Integer getServerRev() {
		return serverRev;
	}
	public static final LTItemMail getInstance() {
		return instance;
	}
}