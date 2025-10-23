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
package br.net.gmj.nobookie.LTItemMail.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class MenuInventory {
	private MenuInventory() {}
	public static final String getName(final Type type) {
		switch(type) {
			case HOME:
				return ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Home";
			case ONLINE_PLAYERS:
				return ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Home > Online Players";
			case OFFLINE_PLAYER:
				return ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Home > Offline Player";
		}
		return null;
	}
	public static final Inventory getInventory(final Type type) {
		Inventory inventory = null;
		switch(type) {
			case HOME:
				inventory = Bukkit.createInventory(null, 9, getName(type));
				
			case ONLINE_PLAYERS:
				final Inventory online_players = Bukkit.createInventory(null, 54, getName(type));
				final List<ItemStack> heads = new ArrayList<>();
				for(final Player player : Bukkit.getOnlinePlayers()) {
					final ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
					final SkullMeta headMeta = (SkullMeta) head.getItemMeta();
					headMeta.setOwningPlayer(player);
					headMeta.setDisplayName("Click to send items to " + player.getName());
					head.setItemMeta(headMeta);
					heads.add(head);
				}
				for(int i = 0; i < 44; i++) if(heads.size() < (i + 1)) {
					online_players.setItem(i, heads.get(i));
				} else break;
			case OFFLINE_PLAYER:
				inventory = Bukkit.createInventory(null, InventoryType.ANVIL, getName(type));
				
		}
		return inventory;
	}
	@SuppressWarnings("unused")
	private static final void buildGUI(final Inventory inventory, final Type type, final String label) {
		switch(type) {
			case HOME:
				
				break;
			case OFFLINE_PLAYER:
				
				break;
			case ONLINE_PLAYERS:
				
				break;
		}
	}
	@SuppressWarnings("unused")
	private static final ItemMeta prepareItem(final ItemMeta meta, final int model, final String name, final List<String> lore) {
		meta.setCustomModelData(model);
		meta.setDisplayName(name);
		meta.setLore(lore);
		return meta;
	}
	public enum Type {
		HOME,
		ONLINE_PLAYERS,
		OFFLINE_PLAYER
	}
}