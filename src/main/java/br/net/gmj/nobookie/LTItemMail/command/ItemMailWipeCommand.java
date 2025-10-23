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

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;

@LTCommandInfo(
	name = "itemmailwipe",
	description = "Wipe every data from the database.",
	aliases = "",
	permission = "ltitemmail.admin.wipe",
	usage = "/<command> [confirm]"
)
public final class ItemMailWipeCommand extends LTCommandExecutor {
	@Override
	public final boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
		if(PermissionModule.hasPermission(sender, PermissionModule.Type.CMD_ADMIN_WIPE)) {
			if(args.length == 0) {
				sender.sendMessage(ChatColor.RED + LanguageModule.get(LanguageModule.Type.COMMAND_WIPE_WARNING) + " " + ChatColor.YELLOW + "/itemmailwipe confirm" + ChatColor.RED + ".");
			} else if(args[0].equals("confirm")) {
				sender.sendMessage(ChatColor.DARK_GREEN + LanguageModule.get(LanguageModule.Type.COMMAND_WIPE_WIPING) + " " + ChatColor.RED + LanguageModule.get(LanguageModule.Type.COMMAND_WIPE_TURNOFF));
				if(DatabaseModule.purge()) {
					sender.sendMessage(ChatColor.DARK_GREEN + LanguageModule.get(LanguageModule.Type.COMMAND_WIPE_WIPED) + " " + ChatColor.WHITE + LanguageModule.get(LanguageModule.Type.COMMAND_WIPE_RESTART));
				} else sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + LanguageModule.get(LanguageModule.Type.COMMAND_WIPE_UNABLE));
			} else Bukkit.dispatchCommand(sender, "ltitemmail:itemmailwipe");
		} else sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.PLAYER_PERMISSIONERROR));
		return true;
	}
	@Override
	public final List<String> onTabComplete(final CommandSender sender, Command cmd, final String commandLabel, final String[] args){
		return Collections.emptyList();
	}
}
