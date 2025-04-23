package br.net.gmj.nobookie.LTItemMail.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.event.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.inventory.MailboxInventory;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule.Type;
import br.net.gmj.nobookie.LTItemMail.module.DataModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.EconomyModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;
import br.net.gmj.nobookie.LTItemMail.util.TabUtil;

@LTCommandInfo(
	name = "itemmail",
	description = "Lists player commands.",
	aliases = "ltitemmail:itemmail,ima,imail",
	permission = "ltitemmail.player",
	usage = "/<command> [help|version|list|open|delete|info|price|color|blocks]"
)
public final class ItemMailCommand extends LTCommandExecutor {
	//private final LTCitizens citizens = (LTCitizens) ExtensionModule.getInstance().get(EXT.CITIZENS);
	@SuppressWarnings("incomplete-switch")
	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
		Boolean hasPermission = false;
		if(args.length == 0) {
			hasPermission = true;
			HashMap<Integer, String> mailboxes;
			Player player = null;
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_OPEN) && sender instanceof Player && (mailboxes = DatabaseModule.Virtual.getMailboxesList((player = (Player) sender).getUniqueId(), DatabaseModule.Virtual.Status.PENDING)).size() > 0) {
				final List<Integer> ids = new ArrayList<>();
				for(final Integer id : mailboxes.keySet()) ids.add(id);
				player.performCommand("ltitemmail:itemmail open " + ids.get(0));
			} else Bukkit.dispatchCommand(sender, "ltitemmail:itemmail help");
		} else if(args[0].equalsIgnoreCase("help")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_MAIN)) {
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "[ " + LTItemMail.getInstance().getDescription().getName() + " ]");
				sender.sendMessage(ChatColor.GREEN + "/itemmail help " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_ITEMMAIL));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_VERSION)) sender.sendMessage(ChatColor.GREEN + "/itemmail version " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_VERSION));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_LIST)) sender.sendMessage(ChatColor.GREEN + "/itemmail list " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_LIST));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_OPEN)) sender.sendMessage(ChatColor.GREEN + "/itemmail open <id> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_OPEN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_DELETE)) sender.sendMessage(ChatColor.GREEN + "/itemmail delete <id> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_DELETE));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_PRICE)) sender.sendMessage(ChatColor.GREEN + "/itemmail price " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_COSTS));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_INFO)) sender.sendMessage(ChatColor.GREEN + "/itemmail info " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_MAIN));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_BLOCKS)) sender.sendMessage(ChatColor.GREEN + "/itemmail blocks " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_BLOCKS));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_COLOR)) sender.sendMessage(ChatColor.GREEN + "/itemmail color <color> " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_COLOR));
				if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_SEND)) sender.sendMessage(ChatColor.GREEN + "/mailitem <player> [label] " + ChatColor.AQUA + "- " + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_MAILITEM));
			}
		} else if(args[0].equalsIgnoreCase("version")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_VERSION)) {
				if(args.length == 1) {
					new BukkitRunnable() {
						@Override
						public final void run() {
							sender.sendMessage(ChatColor.YELLOW + "LT Item Mail");
							sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.DARK_GREEN + ConfigurationModule.get(ConfigurationModule.Type.VERSION_NUMBER));
							sender.sendMessage(ChatColor.YELLOW + "Build number: " + ChatColor.DARK_GREEN + ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER));
							final Map<String, Object> params = new HashMap<>();
							params.put("format", "dd/MM/yyyy HH:mm:ss z");
							sender.sendMessage(ChatColor.YELLOW + "Build date: " + ChatColor.DARK_GREEN + FetchUtil.URL.get(DataModule.getDateURL((Integer) ConfigurationModule.get(Type.BUILD_NUMBER)), params).replaceAll(System.lineSeparator(), ""));
							String authors = "";
							if(LTItemMail.getInstance().getDescription().getAuthors().size() > 1) {
								String separator = ", ";
								for(final String author : LTItemMail.getInstance().getDescription().getAuthors()) {
									if(LTItemMail.getInstance().getDescription().getAuthors().get(LTItemMail.getInstance().getDescription().getAuthors().size() - 1).equals(author)) separator = "";
									authors = authors + ChatColor.DARK_GREEN + author + ChatColor.YELLOW + separator;
								}
							} else authors = LTItemMail.getInstance().getDescription().getAuthors().get(0);
							sender.sendMessage(ChatColor.YELLOW + "Authors: " + authors);
							sender.sendMessage(ChatColor.YELLOW + "Website: " + ChatColor.DARK_GREEN + LTItemMail.getInstance().getDescription().getWebsite());
							if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_MAIN)) sender.sendMessage(ChatColor.YELLOW + "Manifest: " + ChatColor.DARK_GREEN + DataModule.getManifestURL(LTItemMail.getInstance().getDescription().getVersion()));
						}
					}.runTaskAsynchronously(LTItemMail.getInstance());
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("open")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_OPEN)) {
				if(sender instanceof Player) {
					final Player player = (Player) sender;
					if(args.length == 2) {
						final String idStr = args[1].toLowerCase();
						try {
							if(idStr.endsWith("p") || idStr.endsWith("a")) {
								DatabaseModule.Virtual.Status status;
								MailboxInventory.Type type;
								if(idStr.endsWith("p")) {
									status = DatabaseModule.Virtual.Status.PENDING;
									type = MailboxInventory.Type.IN_PENDING;
								} else {
									status = DatabaseModule.Virtual.Status.ACCEPTED;
									type = MailboxInventory.Type.IN;
								}
								final HashMap<Integer, String> mailboxes = DatabaseModule.Virtual.getMailboxesList(player.getUniqueId(), status);
								final Integer pos = Integer.valueOf(idStr.replace("#", "").replace("p", "").replace("a", ""));
								if(mailboxes.size() >= pos) {
									final List<Integer> ids = new ArrayList<>();
									for(final Integer id : mailboxes.keySet()) ids.add(id);
									//if(citizens != null) citizens.call(player);
									player.openInventory(MailboxInventory.getInventory(type, ids.get((pos - 1)), null, DatabaseModule.Virtual.getMailbox(ids.get((pos - 1))), DatabaseModule.Virtual.getMailboxFrom(ids.get((pos - 1))), DatabaseModule.Virtual.getMailboxLabel(ids.get((pos - 1))), false));
								}
							} else {
								final Integer mailboxID = Integer.valueOf(idStr.replace("#", ""));
								if(DatabaseModule.Virtual.isMaiboxOwner(player.getUniqueId(), mailboxID) && !DatabaseModule.Virtual.isMailboxDeleted(mailboxID)) {
									MailboxModule.log(LTPlayer.fromUUID(player.getUniqueId()), null, MailboxModule.Action.OPENED, mailboxID, null, null, null);
									MailboxInventory.Type type = null;
									switch(DatabaseModule.Virtual.getStatus(mailboxID)) {
										case ACCEPTED:
											type = MailboxInventory.Type.IN;
											break;
										case PENDING:
											type = MailboxInventory.Type.IN_PENDING;
											break;
										case UNDEFINED:
											player.sendMessage(ChatColor.DARK_RED + DatabaseModule.Virtual.Status.class.getName() + ": " + DatabaseModule.Virtual.Status.UNDEFINED.toString());
											break;
									}
									if(type != null) {
										//if(citizens != null) citizens.call(player);
										player.openInventory(MailboxInventory.getInventory(type, mailboxID, null, DatabaseModule.Virtual.getMailbox(mailboxID), DatabaseModule.Virtual.getMailboxFrom(mailboxID), DatabaseModule.Virtual.getMailboxLabel(mailboxID), false));
									}
								}
							}
						} catch (final NumberFormatException e) {
							player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_IDERROR));
						}
					} else player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
			}
		} else if(args[0].equalsIgnoreCase("list")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_LIST)) {
				if(sender instanceof Player) {
					final Player player = (Player) sender;
					if(args.length == 1) {
						final HashMap<Integer, String> mailboxes = DatabaseModule.Virtual.getMailboxesList(player.getUniqueId(), DatabaseModule.Virtual.Status.ALL);
						if(mailboxes.size() > 0) {
							player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_NEW));
							for(final Integer mailboxID : mailboxes.keySet()) {
								String from = "CONSOLE";
								final UUID uuidFrom = DatabaseModule.Virtual.getMailboxFrom(mailboxID);
								if(uuidFrom != null) from = LTPlayer.fromUUID(uuidFrom).getName();
								String label = "";
								if(!DatabaseModule.Virtual.getMailboxLabel(mailboxID).isEmpty()) label = ": " + DatabaseModule.Virtual.getMailboxLabel(mailboxID);
								player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + " #" + mailboxID + "" + ChatColor.RESET + " <= " + mailboxes.get(mailboxID) + " (@" + from + ")" + label);
							}
						} else player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_NONEW));
					} else player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
			}
		} else if(args[0].equalsIgnoreCase("delete")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_DELETE)) {
				if(sender instanceof Player) {
					final Player player = (Player) sender;
					if(args.length == 2) {
						try {
							final Integer mailboxID = Integer.valueOf(args[1].replace("#", ""));
							if(DatabaseModule.Virtual.isMaiboxOwner(player.getUniqueId(), mailboxID) && !DatabaseModule.Virtual.isMailboxDeleted(mailboxID)) {
								DatabaseModule.Virtual.setMailboxDeleted(mailboxID);
								MailboxModule.log(LTPlayer.fromUUID(player.getUniqueId()), null, MailboxModule.Action.DELETED, mailboxID, null, null, null);
								player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_DELETED) + " " + (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + " #" + mailboxID);
							}
						} catch (final NumberFormatException e) {
							player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_IDERROR));
						}
					} else player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
			}
		} else if(args[0].equalsIgnoreCase("price")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_PRICE)) {
				if(args.length == 1) {
					if(EconomyModule.getInstance() != null) {
						String costs = null;
						if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_TYPE_COST)) {
							costs = (Double) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_COST) + " x Item";
						} else costs = (Double) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_COST) + " x " + (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME);
						if(costs != null) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.TRANSACTION_COSTS) + " " + costs);
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.TRANSACTION_NOTINSTALLED));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
			}
		} else if(args[0].equalsIgnoreCase("info")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_INFO)) {
				if(sender instanceof Player) {
					final Player player = (Player) sender;
					final LTPlayer ltPlayer = LTPlayer.fromUUID(player.getUniqueId());
					if(args.length == 1) {
						if(!ltPlayer.isRegistered()) DatabaseModule.User.register(ltPlayer);
						player.sendMessage("");
						player.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_REGISTRY) + " " + ChatColor.DARK_GREEN + ltPlayer.getRegistryDate());
						String banned = LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_NO);
						String banreason = "";
						if(ltPlayer.isBanned()) {
							banned = LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_YES);
							banreason = ltPlayer.getBanReason();
						}
						player.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_MAIN) + " " + ChatColor.DARK_GREEN + banned);
						if(banreason != null && !banreason.isEmpty()) player.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_REASON) + " " + ChatColor.DARK_GREEN + banreason);
						player.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_SENT) + " " + ChatColor.DARK_GREEN + ltPlayer.getMailSentCount());
						player.sendMessage(ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_RECEIVED) + " " + ChatColor.DARK_GREEN + ltPlayer.getMailReceivedCount());
						player.sendMessage("");
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
			}
		} else if(args[0].equalsIgnoreCase("color")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_COLOR)) {
				if(sender instanceof Player) {
					final Player player = (Player) sender;
					final LinkedList<String> colors = new LinkedList<>();
					for(Material color : Material.values()) if(color.toString().endsWith("_SHULKER_BOX")) colors.add(color.toString().replace("_SHULKER_BOX", "").toLowerCase());
					colors.sort(Comparator.naturalOrder());
					if(args.length == 2) {
						final String color = args[1].toLowerCase();
						final ItemStack current = player.getInventory().getItemInMainHand();
						if(colors.contains(color) && current != null && current.getItemMeta() != null && current.getType().toString().endsWith("_SHULKER_BOX") && BukkitUtil.DataContainer.Mailbox.isMailbox(current)) {
							final ItemStack newMailbox = new ItemStack(Material.getMaterial(color.toUpperCase() + "_SHULKER_BOX"));
							newMailbox.setAmount(current.getAmount());
							newMailbox.setItemMeta(current.getItemMeta());
							current.setAmount(0);
							player.getInventory().setItemInMainHand(newMailbox);
						}
					} else if(args.length == 1) {
						String colorString = "";
						for(final String color : colors) if(colors.getLast().equals(color)) {
							colorString = colorString + ChatColor.WHITE + color + ChatColor.YELLOW + ".";
						} else colorString = colorString + ChatColor.WHITE + color + ChatColor.YELLOW + ", ";
						player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "Colors available: " + colorString);
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
			}
		} else if(args[0].equalsIgnoreCase("blocks")) {
			if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_BLOCKS)) {
				if(sender instanceof Player) {
					final Player player = (Player) sender;
					final List<MailboxBlock> mailboxes = new ArrayList<>();
					for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks()) if(block.getOwner().getUniqueId().equals(LTPlayer.fromUUID(player.getUniqueId()).getUniqueId())) mailboxes.add(block);
					if(mailboxes.size() > 0) {
						player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.BLOCK_LIST));
						Integer number = 1;
						for(final MailboxBlock block : mailboxes) {
							String server = "";
							if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) server = LanguageModule.get(LanguageModule.Type.BLOCK_LIST_SERVER) + "=" + ChatColor.GREEN + block.getServer() + ChatColor.YELLOW + ", ";
							final Location loc = block.getLocation();
							player.sendMessage(ChatColor.YELLOW + "    - #" + ChatColor.GREEN + String.valueOf(number) + ChatColor.YELLOW + " : " + server + LanguageModule.get(LanguageModule.Type.BLOCK_LIST_WORLD) + "=" + ChatColor.GREEN + loc.getWorld().getName() + ChatColor.YELLOW + ", X=" + ChatColor.GREEN + String.valueOf(loc.getBlockX()) + ChatColor.YELLOW + ", Y=" + ChatColor.GREEN + String.valueOf(loc.getBlockY()) + ChatColor.YELLOW + ", Z=" + ChatColor.GREEN + String.valueOf(loc.getBlockZ()));
							number++;
						}
					} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_NOTFOUND));
				} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
			}
		} else if(hasPermission = PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_MAIN)) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ((String) LanguageModule.get(LanguageModule.Type.COMMAND_INVALID)).replaceAll("%command%", ChatColor.GREEN + "/itemmail" + ChatColor.YELLOW));
		if(!hasPermission) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_PERMISSIONERROR));
		return true;
	}
	@Override
	public final List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args){
		if(args.length == 1) {
			final List<String> commands  = new ArrayList<>();
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_MAIN)) commands.add("help");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_VERSION)) commands.add("version");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_LIST)) commands.add("list");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_OPEN)) commands.add("open");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_DELETE)) commands.add("delete");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_INFO)) commands.add("info");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_PRICE)) commands.add("price");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_COLOR)) commands.add("color");
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_BLOCKS)) commands.add("blocks");
			return TabUtil.partial(args[0], commands);
		}
		if(args.length == 2) {
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_OPEN) || PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_DELETE)) if(args[0].equals("open") || args[0].equals("delete")) if(sender instanceof Player) {
				final Player player = (Player) sender;
				final HashMap<Integer, String> mailboxes = DatabaseModule.Virtual.getMailboxesList(player.getUniqueId(), DatabaseModule.Virtual.Status.ALL);
				final LinkedList<String> response = new LinkedList<>();
				for(final Integer i : mailboxes.keySet()) response.add(String.valueOf(i));
				response.sort(Comparator.naturalOrder());
				return TabUtil.partial(args[1], response);
			}
			if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_COLOR) && args[0].equals("color")) {
				final LinkedList<String> colors = new LinkedList<>();
				for(Material color : Material.values()) if(color.toString().endsWith("_SHULKER_BOX")) colors.add(color.toString().replace("_SHULKER_BOX", "").toLowerCase());
				colors.sort(Comparator.naturalOrder());
				return TabUtil.partial(args[1], colors);
			}
		}
		return Collections.emptyList();
	}
}
