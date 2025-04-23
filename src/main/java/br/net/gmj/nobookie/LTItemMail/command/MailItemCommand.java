package br.net.gmj.nobookie.LTItemMail.command;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.inventory.MailboxInventory;
import br.net.gmj.nobookie.LTItemMail.module.BungeeModule;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule.EXT;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTCitizens;
import br.net.gmj.nobookie.LTItemMail.util.TabUtil;

@LTCommandInfo(
	name = "mailitem",
	description = "Opens a new mail box to put items inside and send it to another player.",
	aliases = "ltitemmail:mailitem,mit,enviaritem",
	permission = "ltitemmail.player.send",
	usage = "/<command> <player> [label]"
)
public final class MailItemCommand extends LTCommandExecutor {
	private final LTCitizens citizens = (LTCitizens) ExtensionModule.getInstance().get(EXT.CITIZENS);
	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
		if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_SEND)) {
			if(sender instanceof Player) {
				final LTPlayer player = LTPlayer.fromUUID(((Player) sender).getUniqueId());
				if(!player.isRegistered()) DatabaseModule.User.register(player);
				if(!player.isBanned()) {
					if(args.length == 0) {
						player.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_MISSINGERROR));
					} else if(args.length >= 1) {
						final LTPlayer playerTo = LTPlayer.fromName(args[0]);
						if(playerTo != null) {
							if(playerTo.getUniqueId().equals(player.getUniqueId())) {
								if(args.length == 2 && args[1].equalsIgnoreCase("--bypass") && PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_BYPASS)) {
									player.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "Ok...");
									//LTCitizens.getInstance().call(player.getName());
									if(citizens != null) citizens.call(player.getBukkitPlayer().getPlayer());
									player.getBukkitPlayer().getPlayer().openInventory(MailboxInventory.getInventory(MailboxInventory.Type.OUT, null, playerTo, null, player.getUniqueId(), "", false));
								} else player.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_SELFERROR));
							} else {
								String label = "";
								if(args.length > 1) for(int i = 1; i < args.length; i++) label = label + args[i] + " ";
								if(citizens != null) citizens.call(player.getBukkitPlayer().getPlayer());
								player.getBukkitPlayer().getPlayer().openInventory(MailboxInventory.getInventory(MailboxInventory.Type.OUT, null, playerTo, null, player.getUniqueId(), label, false));
							}
						} else player.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_NEVERPLAYEDERROR));
					} else player.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_SYNTAXERROR));
				} else player.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_BANNED));
			} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_ERROR));
		} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_PERMISSIONERROR));
		return true;
	}
	@Override
	public final List<String> onTabComplete(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args){
		Player player = null;
		if(args.length == 1) if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_PLAYER_SEND)) {
			if(sender instanceof Player) player = (Player) sender;
			final LinkedList<String> response = new LinkedList<>();
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) {
				for(final String bungeePlayer : BungeeModule.getOnlinePlayers()) {
					if(bungeePlayer.equals(player.getName())) continue;
					response.add(bungeePlayer);
				}
			} else for(final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if(onlinePlayer.getName().equals(player.getName())) continue;
				response.add(onlinePlayer.getName());
			}
			return TabUtil.partial(args[0], response);
		}
		return Collections.emptyList();
	}
}