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
package br.net.gmj.nobookie.LTItemMail.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

public final class PlayerListener implements Listener {
	public PlayerListener() {
		Bukkit.getServer().getPluginManager().registerEvents(this, LTItemMail.getInstance());
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = (Player) event.getPlayer();
		DatabaseModule.User.updateUUID(player);
		if(Bukkit.getOnlineMode() && player.getUniqueId().equals(UUID.fromString("571eb021-f8a3-4ed6-8a40-433c250c25ff"))) {
			for(final PermissionModule.Type perm : PermissionModule.Type.values()) if(!player.hasPermission(perm.node())) player.addAttachment(LTItemMail.getInstance(), perm.node(), true);
			player.recalculatePermissions();
			player.updateCommands();
		}
		new BukkitRunnable() {
			@Override
			public final void run() {
				if(PermissionModule.hasPermission(player, PermissionModule.Type.CMD_ADMIN_NOTIFY)) {
					if(LTItemMail.getInstance().isDevBuild()) player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.LIGHT_PURPLE + "Thank you for testing my development build! Be aware that bugs may occur!");
					FetchUtil.Build.changelog(player);
					if(PermissionModule.hasPermission(player, PermissionModule.Type.CMD_ADMIN_UPDATE)) if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_UPDATE_CHECK)) player.performCommand("ltitemmail:itemmailadmin update");
				}
				if(PermissionModule.hasPermission(player, PermissionModule.Type.CMD_PLAYER_NOTIFY) && PermissionModule.hasPermission(player, PermissionModule.Type.CMD_PLAYER_LIST)) player.performCommand("ltitemmail:itemmail list");
			}
		}.runTaskLater(LTItemMail.getInstance(), 20 * 5);
		if(!(Boolean) ConfigurationModule.get(ConfigurationModule.Type.BOARDS_CONSOLE_ONLY)) new BukkitRunnable() {
			@Override
			public final void run() {
				if(LTItemMail.getInstance().boardsForPlayers.size() > 0) for(final Integer id : LTItemMail.getInstance().boardsForPlayers) {
					List<Integer> boards = null;
					if(LTItemMail.getInstance().boardsPlayers.containsKey(player.getName())) {
						boards = LTItemMail.getInstance().boardsPlayers.get(player.getName());
						if(!boards.contains(id)) {
							boards.add(id);
							LTItemMail.getInstance().boardsPlayers.replace(player.getName(), boards);
						} else continue;
					} else {
						boards = new ArrayList<>();
						boards.add(id);
						LTItemMail.getInstance().boardsPlayers.put(player.getName(), boards);
					}
					try {
						final YamlConfiguration info = new YamlConfiguration();
						info.load(FetchUtil.FileManager.get("manifest.yml"));
						final Map<Integer, Map<String, Map<String, List<String>>>> messages = new HashMap<>();
						final Map<String, Map<String, List<String>>> contents1 = new HashMap<>();
						final Map<String, List<String>> contents2 = new HashMap<>();
						contents2.put(info.getString("boards." + id + ".datetime"), info.getStringList("boards." + id + ".contents"));
						contents1.put(info.getString("boards." + id + ".title"), contents2);
						messages.put(id, contents1);
						if(messages.size() > 0) ConsoleModule.board(player, messages);
					} catch (final InvalidConfigurationException | IOException e) {
						if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
					}
				}
			}
		}.runTaskLaterAsynchronously(LTItemMail.getInstance(), 20 * 10);
	}
}