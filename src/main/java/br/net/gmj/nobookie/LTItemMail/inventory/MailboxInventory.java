package br.net.gmj.nobookie.LTItemMail.inventory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.ModelsModule;
import net.md_5.bungee.api.ChatColor;

public final class MailboxInventory {
	private MailboxInventory() {}
	public static final String getName(final Type type, final Integer mailboxID, final LTPlayer player) {
		switch(type) {
			case IN:
				if(mailboxID != null) {
					return (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + "#" + String.valueOf(mailboxID);
				} else return (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + "#";
			case IN_PENDING:
				if(mailboxID != null) {
					return (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + "!" + String.valueOf(mailboxID);
				} else return (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + "!";
			case OUT:
				if(player != null) {
					return (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + "@" + player.getName();
				} else return (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME) + "@";
		}
		return null;
	}
	public static final Inventory getInventory(final Type type, final Integer mailboxID, LTPlayer player, final LinkedList<ItemStack> contents, final UUID from, final String label, final Boolean adminRecover) {
		Inventory inventory = null;
		switch(type) {
			case IN:
				inventory = Bukkit.createInventory(null, 36, getName(type, mailboxID, null));
				for(int i = 0; i < 27; i++) inventory.setItem(i, contents.get(i));
				buildGUI(inventory, from, label, adminRecover, false);
				break;
			case IN_PENDING:
				inventory = Bukkit.createInventory(null, 36, getName(type, mailboxID, null));
				for(int i = 0; i < 27; i++) inventory.setItem(i, contents.get(i));
				buildGUI(inventory, from, label, false, true);
				break;
			case OUT:
				inventory = Bukkit.createInventory(null, 36, getName(type, mailboxID, player));
				buildGUI(inventory, from, label, false, false);
				break;
		}
		return inventory;
	}
	private static final void buildGUI(final Inventory inventory, final UUID from, final String message, final Boolean adminRecover, final Boolean acceptAndDeny) {
		final ItemStack gui = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
		final ItemStack limiter = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
		final ItemStack costButton = new ItemStack(Material.EMERALD, 1);
		final ItemStack labelButton = new ItemStack(Material.BOOK, 1);
		final ItemStack denyButton = new ItemStack(Material.BARRIER, 1);
		final ItemStack acceptButton = new ItemStack(Material.ENDER_EYE, 1);
		gui.setItemMeta(prepareItem(gui.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_GUI_NORMAL), null, null));
		limiter.setItemMeta(prepareItem(limiter.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_LIMITER), null, null));
		costButton.setItemMeta(prepareItem(costButton.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_BUTTON_COST), ChatColor.RESET + LanguageModule.get(LanguageModule.Type.MAILBOX_COST), Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "$ 0.0", ChatColor.RESET + "" + ChatColor.WHITE + LanguageModule.get(LanguageModule.Type.MAILBOX_COSTUPDATE))));
		String bookFrom = "@CONSOLE";
		if(from != null) bookFrom = "@" + LTPlayer.fromUUID(from).getName();
		String bookLore = "";
		if(!message.isEmpty()) bookLore = ChatColor.RESET + "" + ChatColor.YELLOW + message;
		labelButton.setItemMeta(prepareItem(labelButton.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_BUTTON_LABEL), ChatColor.RESET + LanguageModule.get(LanguageModule.Type.MAILBOX_LABEL), Arrays.asList(bookFrom, bookLore)));
		denyButton.setItemMeta(prepareItem(denyButton.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_BUTTON_DENY), ChatColor.RESET + "" + ChatColor.DARK_RED + "" + ChatColor.BOLD + LanguageModule.get(LanguageModule.Type.MAILBOX_DENY), null));
		acceptButton.setItemMeta(prepareItem(acceptButton.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_BUTTON_ACCEPT), ChatColor.RESET + "" + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + LanguageModule.get(LanguageModule.Type.MAILBOX_ACCEPT), null));
		for(int i = 28; i < 29; i++) inventory.setItem(i, limiter);
		inventory.setItem(31, limiter);
		if(!adminRecover) {
			inventory.setItem(29, costButton);
			inventory.setItem(30, labelButton);
			if(acceptAndDeny) {
				gui.setItemMeta(prepareItem(gui.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_GUI_PENDING), null, null));
				inventory.setItem(32, acceptButton);
				inventory.setItem(33, denyButton);
			} else {
				inventory.setItem(32, limiter);
				inventory.setItem(33, limiter);
			}
		} else {
			gui.setItemMeta(prepareItem(gui.getItemMeta(), ModelsModule.get(ModelsModule.Type.MAILBOX_GUI_ADMIN), null, null));
			inventory.setItem(29, limiter);
			inventory.setItem(30, limiter);
			inventory.setItem(32, limiter);
			inventory.setItem(33, limiter);
		}
		inventory.setItem(27, gui);
		for(int i = 34; i < 36; i++) inventory.setItem(i, limiter);
	}
	private static final ItemMeta prepareItem(final ItemMeta meta, final int model, final String name, final List<String> lore) {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_TEXTURES)) {
			meta.setCustomModelData(model);
		} else meta.setCustomModelData(null);
		if(name != null) {
			meta.setDisplayName(name);
		} else meta.setDisplayName(" ");
		if(lore != null) meta.setLore(lore);
		return meta;
	}
	public enum Type {
		IN,
		IN_PENDING,
		OUT
	}
}