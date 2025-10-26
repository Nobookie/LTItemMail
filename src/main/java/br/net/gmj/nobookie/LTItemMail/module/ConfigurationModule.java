/*
 * Copyright (C) 2025  Murilo Amaral Nappi
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
package br.net.gmj.nobookie.LTItemMail.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

public final class ConfigurationModule {
	private ConfigurationModule() {}
	private static File file;
	public static final void check() {
		file = FetchUtil.FileManager.get("config.yml");
		if(file == null) {
			ConsoleModule.info("Extracting config.yml...");
			LTItemMail.getInstance().saveDefaultConfig();
			ConsoleModule.info("Done.");
		}
	}
	private static boolean update = false;
	public static final FileConfiguration load() {
		file = FetchUtil.FileManager.get("config.yml");
		if(file != null) {
			final FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				ConsoleModule.info("Configuration loaded.");
				if(configuration.getInt("config-version") < DataModule.Version.CONFIG_YML.value()) {
					if(configuration.getInt("config-version") < 18 && configuration.getString(Type.DATABASE_TYPE.path()).equalsIgnoreCase("flatfile")) {
						configuration.set(Type.DATABASE_TYPE.path(), "sqlite");
						configuration.set(Type.DATABASE_SQLITE_FILE.path(), configuration.getString("database.flatfile.file"));
					}
					if(configuration.getInt("config-version") < 21 && configuration.isBoolean("hook.towny")) configuration.set(Type.PLUGIN_HOOK_TOWNYADVANCED_ENABLE.path(), configuration.getBoolean("hook.towny"));
					if(configuration.getInt("config-version") < 23) {
						if(configuration.isBoolean("plugin.bungee-mode")) configuration.set(Type.PLUGIN_MULTI_SERVER_SUPPORT_ENABLE.path(), configuration.getBoolean("plugin.bungee-mode"));
						if(configuration.isString("plugin.bungee-server-id")) configuration.set(Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID.path(), configuration.getString("plugin.bungee-server-id"));
					}
					update = true;
					ConsoleModule.warning("Configuration outdated!");
					ConsoleModule.warning("New settings will be added.");
					configuration.set("config-version", DataModule.Version.CONFIG_YML.value());
				}
				if(configuration.isSet(Type.VERSION_NUMBER.path())) if(!configuration.getString(Type.VERSION_NUMBER.path()).equals(FetchUtil.Version.get())) configuration.set("boards-read", new ArrayList<Integer>());
				configuration.set(Type.VERSION_NUMBER.path(), FetchUtil.Version.get());
				configuration.set(Type.BUILD_NUMBER.path(), FetchUtil.Build.get());
				configuration.save(file);
				return configuration;
			} catch (final IOException | InvalidConfigurationException e) {
				if((Boolean) get(Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		return null;
	}
	public static final void setBoardRead(final Integer id) {
		final List<Integer> boards = getBoardsRead();
		if(!boards.contains(id)) {
			boards.add(id);
			LTItemMail.getInstance().configuration.set("boards-read", boards);
			try {
				LTItemMail.getInstance().configuration.save(file);
			} catch (final IOException e) {
				if((Boolean) get(Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
	}
	public static final void disableDatabaseConversion() {
		LTItemMail.getInstance().configuration.set(Type.DATABASE_CONVERT.path(), false);
		try {
			LTItemMail.getInstance().configuration.save(file);
		} catch (final IOException e) {
			if((Boolean) get(Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
	}
	public static final List<Integer> getBoardsRead(){
		List<Integer> boards;
		if(LTItemMail.getInstance().configuration.isSet("boards-read")) {
			boards = LTItemMail.getInstance().configuration.getIntegerList("boards-read");
		} else boards = new ArrayList<>();
		return boards;
	}
	public static Boolean devMode = false;
	public static final Object get(final Type type) {
		Object result = type.result();
		final String path = type.path();
		if(LTItemMail.getInstance().configuration.isSet(path)) {
			if(type.equals(Type.PLUGIN_DEBUG) && devMode) {
				result = true;
			} else result = LTItemMail.getInstance().configuration.get(path);
			if(type.equals(Type.PLUGIN_TAG) || type.equals(Type.MAILBOX_NAME) || type.equals(Type.MAILBOX_NAME)) result = BukkitUtil.Text.Color.format((String) result);
		} else {
			ConsoleModule.info("Configuration fallback [" + path + ":\"" + result + "\"]");
			LTItemMail.getInstance().configuration.set(path, result);
			try {
				LTItemMail.getInstance().configuration.save(file);
			} catch (final IOException e) {
				if((Boolean) get(Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		return result;
	}
	public static final void addMissing() {
		if(update) {
			for(final Type type : Type.values()) get(type);
			update = false;
		}
	}
	public enum Type {
		BUILD_NUMBER("build-number", FetchUtil.Build.get()),
		VERSION_NUMBER("version-number", FetchUtil.Version.get()),
		PLUGIN_ENABLE("plugin.enable", true),
		PLUGIN_LANGUAGE("plugin.language", "english"),
		PLUGIN_TAG("plugin.tag", "&6[LTIM]"),
		PLUGIN_MULTI_SERVER_SUPPORT_ENABLE("plugin.multi-server-support.enable", false),
		PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID("plugin.multi-server-support.server-id", "server1"),
		PLUGIN_MULTI_SERVER_SUPPORT_MODE("plugin.multi-server-support.mode", "bungee"),
		PLUGIN_MULTI_SERVER_SUPPORT_REDIS_HOST("plugin.multi-server-support.redis.host", "127.0.0.1"),
		PLUGIN_MULTI_SERVER_SUPPORT_REDIS_PORT("plugin.multi-server-support.redis.port", 6379),
		PLUGIN_MULTI_SERVER_SUPPORT_REDIS_PASSWORD("plugin.multi-server-support.redis.password", new String()),
		PLUGIN_MULTI_SERVER_SUPPORT_REDIS_DATABASE("plugin.multi-server-support.redis.database", 0),
		RESOURCE_PACK_DOWNLOAD("plugin.resource-pack-download", false),
		PLUGIN_DEBUG("plugin.debug", false),
		DATABASE_TYPE("database.type", "flatfile"),
		DATABASE_CONVERT("database.convert", false),
		DATABASE_SQLITE_FILE("database.sqlite.file", "mailboxes.db"),
		DATABASE_MYSQL_HOST("database.mysql.host", "127.0.0.1:3306"),
		DATABASE_MYSQL_USER("database.mysql.user", "root"),
		DATABASE_MYSQL_PASSWORD("database.mysql.password", ""),
		DATABASE_MYSQL_NAME("database.mysql.database", "ltitemmail"),
		DATABASE_MYSQL_FLAGS("database.mysql.flags", "?verifyServerCertificate=false&useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8"),
		DATABASE_MYSQL_MAX_POOL_SIZE("database.mysql.max_pool_size", 5),
		DATABASE_MYSQL_MAX_LIFETIME("database.mysql.max_lifetime", 180000),
		DATABASE_MYSQL_CONNECTION_TIMEOUT("database.mysql.connection_timeout", 5000),
		PLUGIN_HOOK_ECONOMY_ENABLE("hook.economy.enable", false),
		PLUGIN_HOOK_ECONOMY_TYPE("hook.economy.type", "Vault"),
		PLUGIN_HOOK_DYNMAP("hook.dynmap", true),
		PLUGIN_HOOK_BLUEMAP("hook.bluemap", true),
		PLUGIN_HOOK_DECENTHOLOGRAMS("hook.decentholograms", true),
		PLUGIN_HOOK_GRIEFPREVENTION("hook.griefprevention", true),
		PLUGIN_HOOK_REDPROTECT("hook.redprotect", true),
		PLUGIN_HOOK_TOWNYADVANCED_ENABLE("hook.towny.enable", true),
		PLUGIN_HOOK_TOWNYADVANCED_TAXES_ENABLE("hook.towny.taxes-per-mailbox.enable", true),
		PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST("hook.towny.taxes-per-mailbox.cost", 15),
		PLUGIN_HOOK_WORLDGUARD("hook.worldguard", true),
		PLUGIN_HOOK_ULTIMATEADVANCEMENTAPI("hook.ultimateadvancementapi", true),
		PLUGIN_HOOK_HEADDATABASE("hook.headdatabase", true),
		PLUGIN_HOOK_SKULLS("hook.skulls", true),
		PLUGIN_HOOK_CITIZENS("hook.citizens", true),
		MAILBOX_DISPLAY("mail.display", "CHAT"),
		MAILBOX_TEXTURES("mail.textures", false),
		MAILBOX_TYPE_COST("mail.cost.per-item", false),
		MAILBOX_COST("mail.cost.value", 30.0),
		MAILBOX_NAME("mail.name", "&3&lMailbox&r&4"),
		AUTORUN_MAIL_NEW_ONCLOSE("autorun.mail.new.on-close", new ArrayList<String>()),
		AUTORUN_MAIL_PENDING_WHENACCEPTED("autorun.mail.pending.when-accepted", new ArrayList<String>()),
		AUTORUN_MAIL_PENDING_WHENDENIED("autorun.mail.pending.when-denied", Arrays.asList("PLAYER:/itemmail list")),
		AUTORUN_MAIL_CLAIMED_ONCLOSE("autorun.mail.claimed.on-close", Arrays.asList("PLAYER:/itemmail list")),
		PLUGIN_UPDATE_CHECK("update.check", true),
		PLUGIN_UPDATE_PERIODIC_NOTIFICATION("update.periodic-notification", true),
		PLUGIN_UPDATE_AUTOMATIC("update.automatic", true),
		BOARDS_CONSOLE_ONLY("boards.console-only", true);
		private final String path;
		private final Object result;
		Type(final String path, final Object result){
			this.path = path;
			this.result = result;
		}
		public final String path() {
			return path;
		}
		public final Object result() {
			return result;
		}
	}
}