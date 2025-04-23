package br.net.gmj.nobookie.LTItemMail.module;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;

public final class MailboxModule {
	private MailboxModule() {}
	private static final void write(final String content) {
		try {
			Files.createDirectories(Paths.get(LTItemMail.getInstance().getDataFolder() + File.separator + "logs"));
			final BufferedWriter writer = new BufferedWriter(new FileWriter(LTItemMail.getInstance().getDataFolder() + File.separator + "logs" + File.separator + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()) + ".log", true));
			writer.write("[" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()) + "] " + content);
			writer.newLine();
			writer.close();
		} catch (final IOException e) {
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) ConsoleModule.debug(MailboxModule.class, content);
	}
	public static final void ready() {
		write("Mailbox Log Worker ready!");
	}
	public static final boolean log(final LTPlayer from, final LTPlayer to, final Action action, final Integer mailboxID, final Double mailboxCost, final LinkedList<ItemStack> contents, final Location mailboxBlock) {
		if(from == null || action == null) return false;
		String log = from.getName() + " ";
		String contentString = "";
		switch(action) {
			case CANCELED:
				log = log + "prevented from sending a mailbox";
				break;
			case GAVE_BACK:
				log = log + "gave back Mailbox#" + mailboxID + " to " + to.getName();
				break;
			case PAID:
				log = log + "paid $" + mailboxCost;
				break;
			case REFUNDED:
				log = log + "was refunded $" + mailboxCost;
				break;
			case RECOVERED:
				log = log + "recovered Mailbox#" + mailboxID;
				break;
			case DELETED:
				log = log + "deleted Mailbox#" + mailboxID;
				break;
			case OPENED:
				log = log + "opened Mailbox#" + mailboxID;
				break;
			case RECEIVED:
				if(contents != null) {
					for(final ItemStack content : contents) if(content != null && !content.getType().equals(Material.AIR)) {
						String itemName = content.getType().toString();
						if(content.hasItemMeta()) itemName = content.getItemMeta().getDisplayName();
						contentString = contentString + itemName + "[" + content.getType().toString() + "](" + content.getAmount() + ") ";
					}
				} else contentString = "#BungeeLimitation";
				log = log + "received Mailbox#" + mailboxID + " / Contents: " + contentString;
				break;
			case SENT:
				if(contents != null) {
					for(final ItemStack content : contents) if(content != null && !content.getType().equals(Material.AIR)) {
						String itemName = content.getType().toString();
						if(content.hasItemMeta()) itemName = content.getItemMeta().getDisplayName();
						contentString = contentString + itemName + "[" + content.getType().toString() + "](" + content.getAmount() + ") ";
					}
				} else contentString = "#BungeeLimitation";
				log = log + "sent to " + to.getName() + ": Mailbox#" + mailboxID + " / Contents: " + contentString;
				break;
			case PLACED:
				log = log + "placed a mailbox at World: " + mailboxBlock.getWorld().getName() + ", X: " + mailboxBlock.getBlockX() + ", Y: " + mailboxBlock.getBlockY() + ", Z: " + mailboxBlock.getBlockZ();
				break;
			case BROKE:
				log = log + "broke a mailbox at World: " + mailboxBlock.getWorld().getName() + ", X: " + mailboxBlock.getBlockX() + ", Y: " + mailboxBlock.getBlockY() + ", Z: " + mailboxBlock.getBlockZ();
				break;
			case ADMIN_BROKE:
				log = log + "broke the mailbox of " + to.getName() + " at World: " + mailboxBlock.getWorld().getName() + ", X: " + mailboxBlock.getBlockX() + ", Y: " + mailboxBlock.getBlockY() + ", Z: " + mailboxBlock.getBlockZ();
				break;
		}
		write(log);
		return true;
	}
	public static final int send(final CommandSender sender, final LTPlayer receiver, final LinkedList<ItemStack> contentsarray, final String label) {
		LTPlayer pSender = null;
		UUID uSender = null;
		if(sender instanceof Player) {
			pSender = LTPlayer.fromUUID(((Player) sender).getUniqueId());
			uSender = pSender.getUniqueId();
		}
		final Integer mailboxID = DatabaseModule.Virtual.saveMailbox(uSender, receiver.getUniqueId(), contentsarray, label);
		log(pSender, receiver, Action.SENT, mailboxID, null, contentsarray, null);
		if(!(sender instanceof ConsoleCommandSender)) sender.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + ((String) LanguageModule.get(LanguageModule.Type.MAILBOX_SENT)).replaceAll("%player%", ChatColor.AQUA + receiver.getName() + ChatColor.YELLOW));
		if(receiver.getBukkitPlayer().getPlayer() != null) {
			final Player bukkitReceiver = receiver.getBukkitPlayer().getPlayer();
			log(receiver, null, Action.RECEIVED, mailboxID, null, contentsarray, null);
			MailboxModule.Display display;
			try {
				display = MailboxModule.Display.valueOf(((String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_DISPLAY)).toUpperCase());
			} catch(final IllegalArgumentException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) {
					ConsoleModule.severe("New mail notification must be CHAT, TITLE or TOAST");
					e.printStackTrace();
				}
				display = MailboxModule.Display.CHAT;
			}
			switch(display) {
				case CHAT:
					bukkitReceiver.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + ChatColor.GREEN + "" + sender.getName() + ChatColor.AQUA + " (#" + mailboxID + "): " + ChatColor.GOLD + label);
					break;
				case TITLE:
					bukkitReceiver.sendTitle(ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + ChatColor.GREEN, sender.getName() + ChatColor.AQUA + " (#" + mailboxID + ")", 20 * 1, 20 * 5, 20 * 1);
					break;
				case TOAST:
					if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI)) {
						final LTUltimateAdvancementAPI ultimateAdvancementAPI = (LTUltimateAdvancementAPI) ExtensionModule.getInstance().get(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI);
						ultimateAdvancementAPI.show(receiver, LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + sender.getName() + ChatColor.AQUA + " (#" + mailboxID + ")");
						if(!label.isEmpty()) Bukkit.getScheduler().runTaskLater(LTItemMail.getInstance(), new Runnable() {
							@Override
							public final void run() {
								ultimateAdvancementAPI.show(receiver, label);
							}
						}, 20 * 3);
					}
					break;
			}
		} else if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) {
			final ByteArrayDataOutput bungee = ByteStreams.newDataOutput();
			if(pSender != null) {
				bungee.writeUTF("Forward");
				bungee.writeUTF("ALL");
				bungee.writeUTF("LTItemMail_MailboxReceived");
				final ByteArrayDataOutput function = ByteStreams.newDataOutput();
				final String data = pSender.getName() + ";" + receiver.getName() + ";" + mailboxID;
				function.writeUTF(data);
				bungee.writeShort(function.toByteArray().length);
				bungee.write(function.toByteArray());
			} else {
				bungee.writeUTF("Message");
				bungee.writeUTF(receiver.getName());
				bungee.writeUTF((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + ChatColor.GREEN + "" + sender.getName());
			}
			Bukkit.getServer().sendPluginMessage(LTItemMail.getInstance(), "BungeeCord", bungee.toByteArray());
		}
		return mailboxID;
	}
	public enum Action {
		PAID,
		REFUNDED,
		RECOVERED,
		DELETED,
		OPENED,
		RECEIVED,
		SENT,
		PLACED,
		BROKE,
		ADMIN_BROKE,
		CANCELED,
		GAVE_BACK
	}
	public enum Display {
		CHAT,
		TITLE,
		TOAST
	}
}