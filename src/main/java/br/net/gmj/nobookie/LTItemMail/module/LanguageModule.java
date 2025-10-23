package br.net.gmj.nobookie.LTItemMail.module;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.DataModule.Version;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

public final class LanguageModule {
	private LanguageModule() {}
	private static File file;
	public static final void check() {
		file = FetchUtil.FileManager.get((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml");
		if(file == null) {
			ConsoleModule.info("Extracting " + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml...");
			if(LTItemMail.getInstance().getResource((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml") != null) {
				LTItemMail.getInstance().saveResource((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml", false);
				ConsoleModule.info("Done.");
			} else {
				if(!((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE)).equalsIgnoreCase("english")) ConsoleModule.warning("Language " + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + " not found!");
				ConsoleModule.warning("Generating a new one and all default translations will be added.");
				FetchUtil.FileManager.create((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml");
			}
		}
	}
	private static boolean update = false;
	public static final FileConfiguration load() {
		file = FetchUtil.FileManager.get((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml");
		if(file != null) {
			final FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				ConsoleModule.info((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml loaded.");
				if(((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE)).equalsIgnoreCase("portuguese")) ConsoleModule.br();
				try {
					final Version version = DataModule.Version.valueOf(((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE)).toUpperCase() + "_YML");
					if(configuration.getInt("language-version") < version.value()) {
						update = true;
						ConsoleModule.warning("Language " + (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE) + ".yml outdated!");
						ConsoleModule.warning("Missing translations will be added with default value.");
						if(configuration.getInt("language-version") < 11) ConsoleModule.severe("All \"%\" arguments from the language file were changed! You MUST manually edit the file and replace all \"%\" with the new format. Reference example: https://github.com/leothawne/LTItemMail/blob/dev/src/main/resources/portuguese.yml");
						configuration.set("language-version", version.value());
						configuration.save(file);
					}
				} catch(final IllegalArgumentException e) {
					configuration.set("language-version", 0);
					configuration.save(file);
				}
				return configuration;
			} catch(final IOException | InvalidConfigurationException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		return null;
	}
	public static final String get(final Type type) {
		String result = type.result();
		final String path = type.path();
		if(LTItemMail.getInstance().language.isSet(path)) {
			result = LTItemMail.getInstance().language.getString(path);
			if(type.equals(Type.MAILBOX_COST) || type.equals(Type.MAILBOX_LABEL) || type.equals(Type.ENTITY_PARROT_NAME)) result = BukkitUtil.Text.Color.format((String) result);
		} else {
			ConsoleModule.info("Language fallback [" + path + ":\"" + result + "\"]");
			LTItemMail.getInstance().language.set(path, result);
			try {
				LTItemMail.getInstance().language.save(file);
			} catch (final IOException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
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
		PLAYER_PERMISSIONERROR("player.permissionerror", "You do not have permission to do that."),
		PLAYER_MISSINGERROR("player.missingerror", "You must specify a player!"),
		PLAYER_NEVERPLAYEDERROR("player.neverplayederror", "The specified player has never played on this server."),
		PLAYER_ERROR("player.error", "You must be a player to do that!"),
		PLAYER_SYNTAXERROR("player.syntaxerror", "Syntax error!"),
		PLAYER_SELFERROR("player.selferror", "You can not send items to yourself."),
		PLAYER_OPENEDBOXES("player.openedboxes", "Deleted mails of"),
		PLAYER_BANNED("player.banned", "You are banned! Ban reason available in /itemmail info"),
		MAILBOX_CLOSED("mailbox.closed", "Mail closed."),
		MAILBOX_SENT("mailbox.sent", "Sending mail to %player%..."),
		MAILBOX_FROM("mailbox.from", "New mail from"),
		MAILBOX_SPECIAL("mailbox.special", "Special Mail!!!"),
		MAILBOX_ABORTED("mailbox.aborted", "Shipping canceled!"),
		MAILBOX_IDERROR("mailbox.iderror", "Mail ID must be a number."),
		MAILBOX_DELETED("mailbox.deleted", "Deleted"),
		MAILBOX_EMPTY("mailbox.empty", "Empty"),
		MAILBOX_NOLOST("mailbox.nolost", "There is no lost items in this mail."),
		MAILBOX_EMPTYLIST("mailbox.emptylist", "No opened mails for player"),
		MAILBOX_NONEW("mailbox.nonew", "No new mails."),
		MAILBOX_NEW("mailbox.new", "New mail found! Type /itemmail open <id>"),
		MAILBOX_BLOCKED("mailbox.blocked", "Something blocked this mail. Delivery cancelled."),
		MAILBOX_LABEL("mailbox.label", "&6Label:"),
		MAILBOX_COST("mailbox.cost", "&aCost:"),
		MAILBOX_COSTERROR("mailbox.costerror", "Economy plugin not found."),
		MAILBOX_COSTUPDATE("mailbox.costupdate", "Click to update the current mail price."),
		MAILBOX_RETURNED("mailbox.returned", "sent it back to you."),
		MAILBOX_ACCEPT("mailbox.accept", "Accept"),
		MAILBOX_DENY("mailbox.deny", "Deny"),
		TRANSACTION_PAID("transaction.paid", "You paid $%money% to the post office."),
		TRANSACTION_ERROR("transaction.error", "Transaction not succeeded!"),
		TRANSACTION_NOMONEY("transaction.nomoney", "You do not have $%money% to pay to the post office."),
		TRANSACTION_NOTINSTALLED("transaction.notinstalled", "Economy was not detected!"),
		TRANSACTION_COSTS("transaction.costs", "Price:"),
		BLOCK_PLACEERROR("block.placeerror", "You do not have permission to place a mailbox."),
		BLOCK_BREAKERROR("block.breakerror", "You do not have permission to break a mailbox."),
		BLOCK_USEERROR("block.useerror", "You do not have permission to use a mailbox."),
		BLOCK_OWNERERROR("block.ownererror", "You are not the mailbox owner to do that."),
		BLOCK_BELOWERROR("block.belowerror", "Below block must be a fence."),
		BLOCK_ADMINBROKE("block.adminbroke", "You broke the mailbox of"),
		BLOCK_NAME("block.name", "Mailbox"),
		BLOCK_OWNER("block.owner", "Owner:"),
		BLOCK_LIST("block.list.placed", "Mailboxes you placed:"),
		BLOCK_LIST_WORLD("block.list.world", "World"),
		BLOCK_LIST_SERVER("block.list.server", "Server"),
		BLOCK_ADMIN_LIST("block.adminlist.placed", "Mailboxes of"),
		BLOCK_NOTFOUND("block.notfound", "No mailbox block found."),
		COMMAND_INVALID("command.invalid", "Invalid command. Type %command% to see the command list."),
		COMMAND_PLAYER_ITEMMAIL("command.player.itemmail", "Lists player commands."),
		COMMAND_PLAYER_VERSION("command.player.version", "Shows the current plugin version."),
		COMMAND_PLAYER_LIST("command.player.list", "Lists all pending mailboxes received."),
		COMMAND_PLAYER_OPEN("command.player.open", "Opens a mail."),
		COMMAND_PLAYER_DELETE("command.player.delete", "Deletes a mail."),
		COMMAND_PLAYER_MAILITEM("command.player.mailitem", "Opens a new mail box to put items inside and send it to another player."),
		COMMAND_PLAYER_INFO_MAIN("command.player.info.info", "Shows relevant informations about the player."),
		COMMAND_PLAYER_INFO_REGISTRY("command.player.info.registry", "Registry date:"),
		COMMAND_PLAYER_INFO_BANNED_MAIN("command.player.info.banned.banned", "Banned:"),
		COMMAND_PLAYER_INFO_BANNED_NO("command.player.info.banned.nop", "No"),
		COMMAND_PLAYER_INFO_BANNED_YES("command.player.info.banned.yep", "Yes"),
		COMMAND_PLAYER_INFO_BANNED_REASON("command.player.info.banned.reason", "Ban reason:"),
		COMMAND_PLAYER_INFO_SENT("command.player.info.sent", "Mails sent:"),
		COMMAND_PLAYER_INFO_RECEIVED("command.player.info.received", "Mails received:"),
		COMMAND_PLAYER_BLOCKS("command.player.blocks", "Shows your placed mailboxes list."),
		COMMAND_PLAYER_COLOR("command.player.color", "Changes the color of the mailbox block in your main hand."),
		COMMAND_PLAYER_COSTS("command.player.costs", "Shows mail price."),
		COMMAND_ADMIN_ITEMMAILADMIN("command.admin.itemmailadmin", "Lists admin commands."),
		COMMAND_ADMIN_UPDATE_MAIN("command.admin.update.update", "Checks for new updates."),
		COMMAND_ADMIN_UPDATE_FOUND("command.admin.update.found", "New update available! You are %build% build(s) out of date. Download it now:"),
		COMMAND_ADMIN_UPDATE_NONEW("command.admin.update.nonew", "There is no new updates."),
		COMMAND_ADMIN_UPDATE_AUTOMATIC("command.admin.update.automatic", "The new update will be automatically downloaded and installed. Please restart the server as soon as possible to complete the update process!"),
		COMMAND_ADMIN_LIST("command.admin.list", "Lists deleted mails of a player."),
		COMMAND_ADMIN_RECOVER("command.admin.recover", "Recovers lost items from a deleted mail (if there is any)."),
		COMMAND_ADMIN_RELOAD("command.admin.reload", "Reloads plugin config and language settings."),
		COMMAND_ADMIN_BAN_MAIN("command.admin.ban.ban", "Bans a specific player."),
		COMMAND_ADMIN_BAN_BANNED("command.admin.ban.banned", "banned!"),
		COMMAND_ADMIN_BAN_ALREADY("command.admin.ban.already", "is banned already!"),
		COMMAND_ADMIN_UNBAN_MAIN("command.admin.unban.unban", "Unbans a specific player."),
		COMMAND_ADMIN_UNBAN_UNBANNED("command.admin.unban.unbanned", "unbanned!"),
		COMMAND_ADMIN_UNBAN_ALREADY("command.admin.unban.already", "is unbanned already!"),
		COMMAND_ADMIN_BANLIST_MAIN("command.admin.banlist.banlist", "Gets the banned list."),
		COMMAND_ADMIN_BANLIST_LIST("command.admin.banlist.list", "Players banned:"),
		COMMAND_ADMIN_BANLIST_EMPTY("command.admin.banlist.empty", "No players banned."),
		COMMAND_ADMIN_BLOCKS("command.admin.blocks", "Shows the list of placed mailboxes of a player."),
		COMMAND_ADMIN_DUMP("command.admin.dump", "Useful for debugging."),
		COMMAND_ADMIN_CHANGELOG_MAIN("command.admin.changelog.changelog", "Shows the changelog of the current build of the plugin."),
		COMMAND_ADMIN_CHANGELOG_NOTFOUND("command.admin.changelog.notfound", "No changelog found."),
		COMMAND_ADMIN_CHANGELOG_DETAILS("command.admin.changelog.details", "Details:"),
		COMMAND_ADMIN_GIVE_MAIN("command.admin.give.give", "Gives one mailbox to a specific player."),
		COMMAND_ADMIN_GIVE_OFFLINE("command.admin.give.offline", "The specified player is offline."),
		COMMAND_ADMIN_GIVE_ADDED("command.admin.give.added", "The Mailbox block has been added to the player's inventory."),
		COMMAND_ADMIN_GIVE_DROPPED("command.admin.give.dropped", "There was no space available in the player's inventory and the Mailbox block was dropped on the ground."),
		COMMAND_WIPE_WIPE("command.wipe.wipe", "Wipe every data from the database."),
		COMMAND_WIPE_WARNING("command.wipe.warning", "Every data from the database will be wiped and will not be possible to recover. Are you sure you want to continue? To agree and continue, type"),
		COMMAND_WIPE_WIPING("command.wipe.purging", "Wiping database now..."),
		COMMAND_WIPE_TURNOFF("command.wipe.turnoff", "Do not turn off the server!"),
		COMMAND_WIPE_WIPED("command.wipe.purged", "Database wiped!"),
		COMMAND_WIPE_RESTART("command.wipe.restart", "A server restart is recommended but not mandatory."),
		COMMAND_WIPE_UNABLE("command.wipe.unable", "Unable to wipe database."),
		ENTITY_PARROT_NAME("entity.parrot.name", "&6&lDelivery Parrot");
		private final String path;
		private final String result;
		Type(final String path, final String result){
			this.path = path;
			this.result = result;
		}
		public final String path() {
			return path;
		}
		public final String result() {
			return result;
		}
	}
}