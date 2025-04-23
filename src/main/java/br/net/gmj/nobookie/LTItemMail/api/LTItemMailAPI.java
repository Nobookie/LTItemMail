package br.net.gmj.nobookie.LTItemMail.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.api.event.ServerSendMailEvent;
import br.net.gmj.nobookie.LTItemMail.module.BungeeModule;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;

/**
 * 
 * The API class.
 * 
 * @author Nobookie
 * 
 */
public final class LTItemMailAPI {
	private LTItemMailAPI() {}
	/**
	 * 
	 * Method used to send items anonymously
	 * to any player on the server. The mail
	 * will be assigned as a "Special Mail".
	 * The sender will be assigned as CONSOLE.
	 * 
	 * @param player The player who will receive (See {@link LTPlayer}).
	 * @param items A list of items that the player will receive.
	 * @param label The label you want to put on the mailbox.
	 * 
	 */
	public static final Boolean sendSpecialMail(@NotNull final LTPlayer player, @NotNull final LinkedList<ItemStack> items, @NotNull final String label) {
		try {
			final Player bukkitPlayer = player.getBukkitPlayer().getPlayer();
			DatabaseModule.Virtual.saveMailbox(null, player.getBukkitPlayer().getUniqueId(), items, label);
			Bukkit.getPluginManager().callEvent(new ServerSendMailEvent(player, items, label));
			if(bukkitPlayer != null) {
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
						bukkitPlayer.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_SPECIAL));
						break;
					case TITLE:
						bukkitPlayer.sendTitle(ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_SPECIAL), "", 20 * 1, 20 * 5, 20 * 1);
						break;
					case TOAST:
						if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI)) {
							final LTUltimateAdvancementAPI ultimateAdvancementAPI = (LTUltimateAdvancementAPI) ExtensionModule.getInstance().get(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI);
							ultimateAdvancementAPI.show(player, LanguageModule.get(LanguageModule.Type.MAILBOX_SPECIAL));
							if(!label.isEmpty()) {
								final String l = label;
								new BukkitRunnable() {
									@Override
									public final void run() {
										ultimateAdvancementAPI.show(player, l);
									}
								}.runTaskLater(LTItemMail.getInstance(), 20 * 3);
							}
						}
						break;
				}
			} else if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) {
				final ByteArrayDataOutput bungee = ByteStreams.newDataOutput();
				bungee.writeUTF("Message");
				bungee.writeUTF(player.getName());
				bungee.writeUTF((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_SPECIAL));
				Bukkit.getServer().sendPluginMessage(LTItemMail.getInstance(), "BungeeCord", bungee.toByteArray());
			}
			return true;
		} catch(final IllegalArgumentException e) {
			ConsoleModule.debug(LTItemMailAPI.class, "Argument cannot be null.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return false;
	}
	/**
	 * 
	 * Gets all existing mailbox
	 * blocks standing in
	 * one or more worlds.
	 * 
	 * @return List of {@link MailboxBlock}.
	 * 
	 */
	@NotNull
	public static final List<MailboxBlock> getMailboxBlockList(){
		final List<MailboxBlock> blockList = new ArrayList<>();
		for(final MailboxBlock mailboxBlock : DatabaseModule.Block.getMailboxBlocks()) blockList.add(mailboxBlock);
		return blockList;
	}
	/**
	 * 
	 * Gets a LT Item Mail block
	 * on the provided location.
	 * 
	 * @param location The location to look (See {@link Location}).
	 * 
	 * @return The {@link MailboxBlock} object or null if not found.
	 * 
	 */
	public static final MailboxBlock getMailboxBlock(final Location location) {
		if(DatabaseModule.Block.isMailboxBlock(location)) for(final MailboxBlock mailbox : getMailboxBlockList()) if(mailbox.getServer().equals(ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID)) && mailbox.getLocation().equals(location)) return mailbox;
		return null;
	}
	/**
	 * 
	 * Gets a list of online
	 * players on the Bungee
	 * network.
	 * 
	 * @return A list of player names (can be sensitive case) or null if bungee mode is disabled.
	 * 
	 */
	public static final List<String> getBungeeOnlinePlayers(){
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) return BungeeModule.getOnlinePlayers();
		return null;
	}
	/**
	 * 
	 * Gets a list of online
	 * players on the Bungee
	 * network.
	 * 
	 * @return A list of LTPlayer objects representing the players that are online or null if bungee mode is disabled. If you are not sure that all players ever joined (at least once) a server where LTItemMail is installed, it's recommend to use {@link LTItemMailAPI#getBungeeOnlinePlayers()} instead.
	 * 
	 */
	public static final List<LTPlayer> getBungeeOnlineLTPlayers(){
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) {
			final List<LTPlayer> players = new ArrayList<>();
			for(final String name : getBungeeOnlinePlayers()) players.add(LTPlayer.fromName(name));
			return players;
		}
		return null;
	}
}