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
package br.net.gmj.nobookie.LTItemMail.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.inventory.MailboxInventory;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DataModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.MultiServerModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;
import br.net.gmj.nobookie.LTItemMail.util.TabUtil;

@LTCommandInfo(
	name = "itemmailadmin",
	description = "Lists admin commands.",
	aliases = "imad,imadmin",
	permission = "ltitemmail.admin",
	usage = "/<command> [help|update|list|recover|reload|info|ban|unban|banlist|blocks|dump|changelog]"
)
public final class ItemMailAdminCommand extends LTCommandExecutor {
	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
		Boolean hasPermission = false;
		if(args.length == 0) {
			hasPermission = true;
			Bukkit.dispatchCommand(sender, "ltitemmail:itemmailadmin help");
		} else if(args[0].equalsIgnoreCase("help")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_MAIN)) {
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "[ " + LTItemMail.getInstance().getDescription().getName() + " " + ConfigurationModule.get(ConfigurationModule.Type.VERSION_NUMBER) + " #" + ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER) + " ]");
				sender.sendMessage(ChatColor.GREEN + "/itemmailadmin help " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_ITEMMAILADMIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_UPDATE)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin update " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_UPDATE_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_LIST)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin list <player> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_LIST));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_RECOVER)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin recover <id> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_RECOVER));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_RELOAD)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin reload " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_RELOAD));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BAN)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin ban <player> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_BAN_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_UNBAN)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin unban <player> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_UNBAN_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BANLIST)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin banlist " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_BANLIST_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_INFO)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin info <player> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BLOCKS)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin blocks <player> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_BLOCKS));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_DUMP)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin dump " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_DUMP));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_CHANGELOG)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin changelog " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_CHANGELOG_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_GIVE)) sender.sendMessage(ChatColor.GREEN + "/itemmailadmin give " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_WIPE)) sender.sendMessage(ChatColor.GREEN + "/itemmailwipe " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_WIPE_WIPE));
			}
		} else if(args[0].equalsIgnoreCase("update")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_UPDATE)) {
				if(args.length >= 1) {
					Boolean silent = false;
					if(args.length == 2 && args[1].equalsIgnoreCase("-s")) silent = true;
					final Boolean s = silent;
					new BukkitRunnable() {
						@Override
						public final void run() {
							final Integer localBuild = (Integer) ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER);
							final String response = FetchUtil.URL.get(DataModule.UPDATE, null).replaceAll(System.lineSeparator(), "");
							if(response != null) {
								final Integer remoteBuild = Integer.parseInt(response);
								if(remoteBuild > localBuild) {
									final Integer outOfDate = remoteBuild - localBuild;
									sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ((String) LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_UPDATE_FOUND)).replaceAll("%build%", "" + ChatColor.RED + outOfDate + ChatColor.YELLOW) + ChatColor.GREEN + " https://jenkins.gmj.net.br/job/LTItemMail/" + remoteBuild + "/");
									if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_UPDATE_AUTOMATIC)) {
										if(!new File(Bukkit.getUpdateFolderFile(), LTItemMail.getInstance().getDescription().getName() + ".jar").exists()) FetchUtil.FileManager.download(DataModule.ARTIFACT, Bukkit.getUpdateFolderFile(), LTItemMail.getInstance().getDescription().getName() + ".jar", false);
										sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ((String) LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_UPDATE_AUTOMATIC)));
									}
								} else if(!s) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_UPDATE_NONEW));
							} else if(!s) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.DARK_RED + "Update server is down! Please, try again later.");
						}
					}.runTaskAsynchronously(LTItemMail.getInstance());
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("list")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_LIST)) {
				if(args.length == 2) {
					final LTPlayer ltPlayer = LTPlayer.fromName(args[1]);
					if(ltPlayer != null) {
						final HashMap<Integer, String> deleted = DatabaseModule.Virtual.getDeletedMailboxesList(ltPlayer.getUniqueId());
						final HashMap<Integer, String> mailboxes = new HashMap<>();
						for(final Integer mailboxID : deleted.keySet()) if(!DatabaseModule.Virtual.getStatus(mailboxID).equals(DatabaseModule.Virtual.Status.DENIED)) mailboxes.put(mailboxID, deleted.get(mailboxID));
						if(mailboxes.size() > 0) {
							sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_OPENEDBOXES) + " " + ltPlayer.getName() + ":");
							for(final Integer mailboxID : mailboxes.keySet()) {
								String from = "CONSOLE";
								final UUID uuidFrom = DatabaseModule.Virtual.getMailboxFrom(mailboxID);
								if(uuidFrom != null) from = Bukkit.getOfflinePlayer(uuidFrom).getName();
								String label = "";
								if(!DatabaseModule.Virtual.getMailboxLabel(mailboxID).isEmpty()) label = ": " + DatabaseModule.Virtual.getMailboxLabel(mailboxID);
								String empty = "";
								final LinkedList<ItemStack> items = DatabaseModule.Virtual.getMailbox(mailboxID);
								if(items.size() == 0) empty = " [" + LanguageModule.get(LanguageModule.Type.MAILBOX_EMPTY) + "]";
								sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + " #" + mailboxID + "" + ChatColor.RESET + " <= " + mailboxes.get(mailboxID) + " (@" + from + ")" + empty + label); 
							}
						} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.MAILBOX_EMPTYLIST) + " " + ltPlayer.getName());
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_NEVERPLAYEDERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("info")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_INFO)) {
				if(args.length == 2) {
					final LTPlayer ltPlayer = LTPlayer.fromName(args[1]);
					if(ltPlayer != null) {
						if(!ltPlayer.isRegistered()) DatabaseModule.User.register(ltPlayer);
						sender.sendMessage("");
						sender.sendMessage(ChatColor.YELLOW + ltPlayer.getName());
						String divider = "";
						for(int i = 0; i < ltPlayer.getName().toCharArray().length; i++) divider = divider + "-";
						sender.sendMessage(ChatColor.YELLOW + divider);
						sender.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_REGISTRY) + " " + ChatColor.DARK_GREEN + ltPlayer.getRegistryDate());
						String banned = LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_NO);
						String banreason = "";
						if(ltPlayer.isBanned()) {
							banned = LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_YES);
							banreason = ltPlayer.getBanReason();
						}
						sender.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_MAIN) + " " + ChatColor.DARK_GREEN + banned);
						if(banreason != null && !banreason.isEmpty()) sender.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_REASON) + " " + ChatColor.DARK_GREEN + banreason);
						sender.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_SENT) + " " + ChatColor.DARK_GREEN + ltPlayer.getMailSentCount());
						sender.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_RECEIVED) + " " + ChatColor.DARK_GREEN + ltPlayer.getMailReceivedCount());
						sender.sendMessage("");
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_NEVERPLAYEDERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("recover")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_RECOVER)) {
				if(sender instanceof Player) {
					final Player player = (Player) sender;
					if(args.length == 2) {
						try {
							final Integer mailboxID = Integer.valueOf(args[1].replace("#", ""));
							final LinkedList<ItemStack> items = DatabaseModule.Virtual.getMailbox(mailboxID);
							if(DatabaseModule.Virtual.isMailboxDeleted(mailboxID) && !DatabaseModule.Virtual.getStatus(mailboxID).equals(DatabaseModule.Virtual.Status.DENIED) && items.size() > 0) {
								player.openInventory(MailboxInventory.getInventory(MailboxInventory.Type.IN, mailboxID, null, items, DatabaseModule.Virtual.getMailboxFrom(mailboxID), DatabaseModule.Virtual.getMailboxLabel(mailboxID), true));
								MailboxModule.log(LTPlayer.fromUUID(player.getUniqueId()), null, MailboxModule.Action.RECOVERED, mailboxID, null, null, null);
							} else player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_NOLOST));
						} catch (final NumberFormatException e) {
							player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_IDERROR));
						}
					} else player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
			}
		} else if(args[0].equalsIgnoreCase("reload")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_RELOAD)) {
				if(args.length == 1) {
					LTItemMail.getInstance().reload();
					ConsoleModule.warning(LTItemMail.getInstance().getDescription().getName() + " reloaded!");
					sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "Reloaded!");
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("ban")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BAN)) {
				if(args.length > 1) {
					final LTPlayer ltPlayer = LTPlayer.fromName(args[1]);
					String banreason = "";
					for(int i = 2; i < args.length; i++) banreason = banreason + args[i] + " ";
					if(ltPlayer != null) {
						if(!ltPlayer.isRegistered()) DatabaseModule.User.register(ltPlayer);
						if(!ltPlayer.isBanned()) {
							DatabaseModule.User.ban(ltPlayer.getUniqueId(), banreason);
							banreason = " => " + banreason;
							sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + ltPlayer.getName() + " " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_BAN_BANNED) + banreason);
						} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + ltPlayer.getName() + " " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_BAN_ALREADY));
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_NEVERPLAYEDERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("unban")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_UNBAN)) {
				if(args.length == 2) {
					final LTPlayer ltPlayer = LTPlayer.fromName(args[1]);
					if(ltPlayer != null) {
						if(!ltPlayer.isRegistered()) DatabaseModule.User.register(ltPlayer);
						if(ltPlayer.isBanned()) {
							DatabaseModule.User.unban(ltPlayer.getUniqueId());
							sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ltPlayer.getName() + " " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_UNBAN_UNBANNED));
						} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ltPlayer.getName() + " " + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_UNBAN_ALREADY));
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_NEVERPLAYEDERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("banlist")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BANLIST)) {
				if(args.length == 1) {
					final LinkedList<String> banlist = DatabaseModule.User.getBansList();
					if(banlist.size() > 0) {
						banlist.sort(Comparator.naturalOrder());
						String banString = (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_BANLIST_LIST) + " ";
						for(final String username : banlist) {
							String end = ", ";
							if(banlist.getLast().equals(username)) end = ".";
							banString = banString + username + end;
						}
						sender.sendMessage(banString);
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_BANLIST_EMPTY));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("blocks")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BLOCKS)) {
				if(args.length > 1) {
					final LTPlayer ltPlayer = LTPlayer.fromName(args[1]);
					if(ltPlayer != null) {
						final List<MailboxBlock> mailboxes = new ArrayList<>();
						for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks()) if(block.getOwner().getUniqueId().equals(ltPlayer.getUniqueId())) mailboxes.add(block);
						if(mailboxes.size() > 0) {
							sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.BLOCK_ADMIN_LIST) + " " + ChatColor.GREEN + ltPlayer.getName() + ":");
							Integer number = 1;
							for(final MailboxBlock block : mailboxes) {
								String server = "";
								if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_ENABLE)) server = "Server=" + ChatColor.GREEN + block.getServer() + ChatColor.YELLOW + ", ";
								final Location loc = block.getLocation();
								sender.sendMessage(ChatColor.YELLOW + "    - #" + ChatColor.GREEN + String.valueOf(number) + ChatColor.YELLOW + " : " + server + LanguageModule.get(LanguageModule.Type.BLOCK_LIST_WORLD) + "=" + ChatColor.GREEN + loc.getWorld().getName() + ChatColor.YELLOW + ", X=" + ChatColor.GREEN + String.valueOf(loc.getBlockX()) + ChatColor.YELLOW + ", Y=" + ChatColor.GREEN + String.valueOf(loc.getBlockY()) + ChatColor.YELLOW + ", Z=" + ChatColor.GREEN + String.valueOf(loc.getBlockZ()));
								number++;
							}
						} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_NOTFOUND));
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_NEVERPLAYEDERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("dump")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_DUMP)) {
				if(args.length == 1) {
					sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.GREEN + Bukkit.getServer().getName() + " " + ChatColor.RESET + "server version " + ChatColor.GREEN + Bukkit.getServer().getVersion());
					sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.RESET + LTItemMail.getInstance().getDescription().getName() + " version " + ChatColor.GREEN + (String) ConfigurationModule.get(ConfigurationModule.Type.VERSION_NUMBER) + ChatColor.RESET + " build " + ChatColor.GREEN + (Integer) ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER));
					for(final ExtensionModule.EXT plugin : ExtensionModule.getInstance().REG.keySet()) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.LIGHT_PURPLE + "Extension: " + ChatColor.RESET + plugin.plugin().getDescription().getName() + " version " + ChatColor.GREEN + plugin.plugin().getDescription().getVersion());
					for(final JavaPlugin plugin : LTItemMail.getInstance().apiHandlers) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.LIGHT_PURPLE + "Using internal API: " + ChatColor.RESET + plugin.getDescription().getName() + " version " + ChatColor.GREEN + plugin.getDescription().getVersion());
					Bukkit.dispatchCommand(sender, "ltitemmail:itemmailadmin update");
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("changelog")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_CHANGELOG)) {
				if(args.length == 1) {
					new BukkitRunnable() {
						@Override
						public final void run() {
							FetchUtil.Build.changelog(sender);
						}
					}.runTaskAsynchronously(LTItemMail.getInstance());
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("give")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_GIVE)) {
				if(args.length == 2) {
					final LTPlayer ltPlayer = LTPlayer.fromName(args[1]);
					if(ltPlayer != null) {
						final Player player = ltPlayer.getBukkitPlayer().getPlayer();
						if(player != null) {
							BukkitUtil.PlayerInventory.addItem(player, new MailboxItem().getItem(null));
							if(BukkitUtil.PlayerInventory.hasSpace(player.getInventory())) {
								sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_ADDED));
							} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_DROPPED));
						} else if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_ENABLE)) {
							if(MultiServerModule.getHandle().getOnlinePlayers().contains(ltPlayer.getName())) {
								MultiServerModule.getHandle().send("LTIM_GMB", sender.getName(), ltPlayer.getName());
							} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_OFFLINE));
						} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_OFFLINE));
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_NEVERPLAYEDERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_MAIN)) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ((String) LanguageModule.get(LanguageModule.Type.COMMAND_INVALID)).replaceAll("%command%", ChatColor.GREEN + "/itemmailadmin" + ChatColor.YELLOW));
		if(!hasPermission) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_PERMISSIONERROR));
		return true;
	}
	@Override
	public final List<String> onTabComplete(final CommandSender sender, Command cmd, final String commandLabel, final String[] args){
		if(args.length == 1) {
			final List<String> commands = new ArrayList<>();
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_MAIN)) commands.add("help");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_UPDATE)) commands.add("update");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_LIST)) commands.add("list");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_RECOVER)) commands.add("recover");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_RELOAD)) commands.add("reload");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_INFO)) commands.add("info");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BAN)) commands.add("ban");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_UNBAN)) commands.add("unban");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BANLIST)) commands.add("banlist");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BLOCKS)) commands.add("blocks");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_DUMP)) commands.add("dump");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_CHANGELOG)) commands.add("changelog");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_GIVE)) commands.add("give");
			return TabUtil.partial(args[0], commands);
		}
		if(args.length == 2) if((PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_LIST) && args[0].equals("list")) || (PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BAN) && args[0].equals("ban")) || (PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_UNBAN) && args[0].equals("unban")) || (PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_INFO) && args[0].equals("info")) || (PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BLOCKS) && args[0].equals("blocks")) || (PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_GIVE) && args[0].equals("give"))) {
			final LinkedList<String> response = new LinkedList<>();
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_ENABLE)) {
				for(final String bungeePlayer : MultiServerModule.getHandle().getOnlinePlayers()) response.add(bungeePlayer);
				for(final Player p: Bukkit.getOnlinePlayers()) if(!response.contains(p.getName())) response.add(p.getName());
			} else for(final Player onlinePlayer : Bukkit.getOnlinePlayers()) response.add(onlinePlayer.getName());
			return TabUtil.partial(args[1], response);
		}
		return Collections.emptyList();
	}
}
