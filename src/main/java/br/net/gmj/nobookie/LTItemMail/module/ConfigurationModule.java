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
			ConsoleModule.info("Configuration fallback: [" + path + ":" + result + "]");
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
		BUNGEE_MODE("plugin.bungee-mode", false),
		BUNGEE_SERVER_ID("plugin.bungee-server-id", "server1"),
		RESOURCE_PACK_DOWNLOAD("plugin.resource-pack-download", false),
		PLUGIN_DEBUG("plugin.debug", false),
		DATABASE_TYPE("database.type", "flatfile"),
		DATABASE_CONVERT("database.convert", false),
		DATABASE_SQLITE_FILE("database.sqlite.file", "mailboxes.db"),
		DATABASE_MYSQL_HOST("database.mysql.host", "127.0.0.1:3306"),
		DATABASE_MYSQL_USER("database.mysql.user", "root"),
		DATABASE_MYSQL_PASSWORD("database.mysql.password", ""),
		DATABASE_MYSQL_NAME("database.mysql.database", "ltitemmail"),
		PLUGIN_HOOK_ECONOMY_ENABLE("hook.economy.enable", false),
		PLUGIN_HOOK_ECONOMY_TYPE("hook.economy.type", "Vault"),
		PLUGIN_HOOK_DYNMAP("hook.dynmap", false),
		PLUGIN_HOOK_BLUEMAP("hook.bluemap", false),
		PLUGIN_HOOK_DECENTHOLOGRAMS("hook.decentholograms", true),
		PLUGIN_HOOK_GRIEFPREVENTION("hook.griefprevention", false),
		PLUGIN_HOOK_REDPROTECT("hook.redprotect", false),
		PLUGIN_HOOK_TOWNYADVANCED("hook.towny", false),
		PLUGIN_HOOK_WORLDGUARD("hook.worldguard", false),
		PLUGIN_HOOK_ULTIMATEADVANCEMENTAPI("hook.ultimateadvancementapi", false),
		PLUGIN_HOOK_HEADDATABASE("hook.headdatabase", false),
		PLUGIN_HOOK_SKULLS("hook.skulls", false),
		MAILBOX_DISPLAY("mail.display", "CHAT"),
		MAILBOX_TEXTURES("mail.textures", false),
		MAILBOX_TYPE_COST("mail.cost.per-item", false),
		MAILBOX_COST("mail.cost.value", 30.0),
		MAILBOX_NAME("mail.name", "&3&lMailbox&r&4"),
		AUTORUN_MAIL_NEW_ONCLOSE("autorun.mail.new.on-close", new ArrayList<String>()),
		AUTORUN_MAIL_PENDING_ONCLOSE("autorun.mail.pending.on-close", Arrays.asList("PLAYER:/itemmail list")),
		AUTORUN_MAIL_CLAIMED_ONCLOSE("autorun.mail.claimed.on-close", Arrays.asList("PLAYER:/itemmail list")),
		PLUGIN_UPDATE_CHECK("update.check", true),
		PLUGIN_UPDATE_PERIODIC_NOTIFICATION("update.periodic-notification", true),
		PLUGIN_UPDATE_AUTOMATIC("update.automatic", true),
		BOARDS_CONSOLE_ONLY("boards.console-only", false);
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