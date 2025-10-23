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
package br.net.gmj.nobookie.LTItemMail.util;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.item.Item;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import me.clip.placeholderapi.PlaceholderAPI;

public final class BukkitUtil {
	private BukkitUtil() {}
	public static final class Text {
		public static final class Color {
			public static final String format(final String text) {
				return ChatColor.translateAlternateColorCodes('&', text);
			}
		}
	}
	public static final class Inventory {
		public static final boolean isEmpty(final ItemStack[] contents) {
			Boolean isEmpty = true;
			for(int i = 0; i < 27; i++) if(contents[i] != null) {
				isEmpty = false;
				break;
			}
			return isEmpty;
		}
		public static final LinkedList<ItemStack> getContents(final ItemStack[] contents){
			final LinkedList<ItemStack> items = new LinkedList<>();
			for(int i = 0; i < 27; i++) if(contents[i] != null) {
				items.add(contents[i]);
			} else items.add(new ItemStack(Material.AIR));
			return items;
		}
		public static final int getCount(final LinkedList<ItemStack> items) {
			int count = 0;
			for(final ItemStack item : items) if(item.getType() != Material.AIR) count = count + item.getAmount();
			return count;
		}
	}
	public static final class PlayerInventory {
		public static final boolean hasSpace(final org.bukkit.inventory.PlayerInventory inventory) {
			if(inventory.firstEmpty() == -1) return false;
			return true;
		}
		public static final void addItem(final Player player, final ItemStack item) {
			if(hasSpace(player.getInventory())) {
				player.getInventory().addItem(item);
			} else player.getLocation().getWorld().dropItem(player.getLocation(), item);
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1f, 1f);
		}
	}
	public static final class DataContainer {
		public static final class Mailbox {
			public static final ItemMeta setMailbox(final ItemMeta meta) {
				meta.getPersistentDataContainer().set(new NamespacedKey(LTItemMail.getInstance(), Item.Type.MAILBOX_ITEM.toString()), PersistentDataType.STRING, Item.Type.MAILBOX_ITEM.toString());
				return meta;
			}
			public static final ItemStack setMailbox(final ItemStack item) {
				item.setItemMeta(setMailbox(item.getItemMeta()));
				return item;
			}
			public static final boolean isMailbox(final ItemMeta meta) {
				return meta.getPersistentDataContainer().has(new NamespacedKey(LTItemMail.getInstance(), Item.Type.MAILBOX_ITEM.toString()), PersistentDataType.STRING);
			}
			public static final boolean isMailbox(final ItemStack item) {
				return isMailbox(item.getItemMeta());
			}
		}
		public static final class Skulls {
			public static final ItemMeta setID(final ItemMeta meta, final Integer id) {
				meta.getPersistentDataContainer().set(new NamespacedKey(LTItemMail.getInstance(), "SKULL_ID"), PersistentDataType.INTEGER, id);
				return meta;
			}
			public static final ItemStack setID(final ItemStack item, final Integer id) {
				item.setItemMeta(setID(item.getItemMeta(), id));
				return item;
			}
			public static final Integer getID(final ItemMeta meta) {
				if(meta.getPersistentDataContainer().has(new NamespacedKey(LTItemMail.getInstance(), "SKULL_ID"), PersistentDataType.INTEGER)) return meta.getPersistentDataContainer().get(new NamespacedKey(LTItemMail.getInstance(), "SKULL_ID"), PersistentDataType.INTEGER);
				return -1;
			}
			public static final Integer getID(final ItemStack item) {
				return getID(item.getItemMeta());
			}
		}
	}
	public static final class AutoRun {
		@SuppressWarnings("unchecked")
		public static final void execute(final Class<?> clazz, final ConfigurationModule.Type autorun, final Player player) {
			if(!autorun.toString().startsWith("AUTORUN_")) return;
			for(final String rawCmd : (List<String>) ConfigurationModule.get(autorun)) {
				final String executor = rawCmd.split("\\:")[0].toUpperCase();
				String cmd = rawCmd.split("\\:")[1].toLowerCase().replaceAll("/", "");
				if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.PLACEHOLDERAPI)) cmd = PlaceholderAPI.setPlaceholders(player, cmd);
				switch(executor) {
					case "CONSOLE":
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
						break;
					case "PLAYER":
						if(!player.performCommand(cmd)) ConsoleModule.debug(clazz, "[" + player.getName() + "] Could not perform command: '/" + cmd + "' from '" + autorun.path() + "'");
						break;
				}
			}
		}
	}
}