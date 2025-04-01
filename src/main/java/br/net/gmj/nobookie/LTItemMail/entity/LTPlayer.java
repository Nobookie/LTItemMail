package br.net.gmj.nobookie.LTItemMail.entity;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * 
 * {@link Bukkit#getOfflinePlayer(String)} requires a case sensitive name and cannot be trusted. This class uses the database first and the username cache after to ensure consistency.
 * 
 * @author Nobookie
 * 
 */
public final class LTPlayer {
	private final OfflinePlayer player;
	private final String name;
	private final UUID uuid;
	private LTPlayer(final OfflinePlayer player, final String name, final UUID uuid) {
		this.player = player;
		this.name = name;
		this.uuid = uuid;
	}
	/**
	 * 
	 * Creates the LTPlayer object from a player's name (case NOT sensitive).
	 * 
	 * @param name The player name.
	 * 
	 */
	public static final LTPlayer fromName(@NotNull final String name) {
		try {
			final UUID uuid = FetchUtil.Player.fromName(name);
			if(uuid != null) return new LTPlayer(Bukkit.getOfflinePlayer(uuid), FetchUtil.Player.fromUUID(uuid), uuid);
		} catch(final IllegalArgumentException e) {
			ConsoleModule.debug(LTPlayer.class, "Argument cannot be null.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * Creates the LTPlayer object from a player's unique id.
	 * 
	 * @param uuid The player UUID.
	 * 
	 */
	public static final LTPlayer fromUUID(@NotNull final UUID uuid) {
		try {
			final String name = FetchUtil.Player.fromUUID(uuid);
			if(name != null) return new LTPlayer(Bukkit.getOfflinePlayer(FetchUtil.Player.fromName(name)), name, FetchUtil.Player.fromName(name));
		} catch(final IllegalArgumentException e) {
			ConsoleModule.debug(LTPlayer.class, "Argument cannot be null.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * Converts from Bukkit player to LTPlayer (same as {@link LTPlayer#fromName(String)}.
	 * 
	 */
	public static final LTPlayer fromBukkitPlayer(@NotNull final OfflinePlayer player) {
		try {
			return LTPlayer.fromName(player.getName());
		} catch(final IllegalArgumentException e) {
			ConsoleModule.debug(LTPlayer.class, "Argument cannot be null.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * Converts LTPlayer into Bukkit player.
	 * 
	 */
	@NotNull
	public final OfflinePlayer getBukkitPlayer() {
		return player;
	}
	/**
	 * 
	 * Forces the LTPlayer to send a mailbox (no charge). It must be online for this to work.
	 * 
	 * @param playerTo The player who will receive (See {@link LTPlayer}).
	 * @param items A list of items that the player will receive.
	 * @param label The label you want to put on the mailbox.
	 * 
	 */
	@NotNull
	public final Boolean forceSend(@NotNull final LTPlayer playerTo, @NotNull final LinkedList<ItemStack> items, @NotNull final String label) {
		try {
			MailboxModule.send(player.getPlayer(), playerTo, items, label);
			return true;
		} catch(final IllegalArgumentException e) {
			ConsoleModule.debug(getClass(), "Argument cannot be null.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return false;
	}
	/**
	 * 
	 * Gives to the LTPlayer a mailbox block.
	 * 
	 * @return true if the player is online and received the mailbox block. Otherwise, it will return false.
	 * 
	 */
	@NotNull
	public final Boolean giveMailboxBlock() {
		final Player player = this.player.getPlayer();
		if(player != null && player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(new MailboxItem().getItem(null));
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.AMBIENT, 1, 1);
			return true;
		}
		return false;
	}
	/**
	 * 
	 * Gets the LTPlayer user name.
	 * 
	 */
	@NotNull
	public final String getName() {
		return name;
	}
	/**
	 * 
	 * Gets the LTPlayer unique id.
	 * 
	 */
	@NotNull
	public final UUID getUniqueId() {
		return uuid;
	}
	/**
	 * 
	 * Gets the ban reason if the LTPlayer is banned.
	 * 
	 */
	public final String getBanReason() {
		if(isRegistered()) return DatabaseModule.User.getBanReason(uuid);
		return null;
	}
	/**
	 * 
	 * Checks if the LTPlayer is banned from the post office (cannot send items, receive only).
	 * 
	 * @return true if the player is banned. Otherwise, it will return false.
	 * 
	 */
	@NotNull
	public final Boolean isBanned() {
		if(isRegistered()) return DatabaseModule.User.isBanned(uuid);
		return false;
	}
	/**
	 * 
	 * Gets the count of mails sent from the LTPlayer.
	 * 
	 */
	@NotNull
	public final Integer getMailSentCount() {
		if(isRegistered()) return DatabaseModule.User.getSentCount(uuid);
		return 0;
	}
	/**
	 * 
	 * Gets the count of mails sent to the LTPlayer.
	 * 
	 */
	@NotNull
	public final Integer getMailReceivedCount() {
		if(isRegistered()) return DatabaseModule.User.getReceivedCount(uuid);
		return 0;
	}
	/**
	 * 
	 * Checks if the LTPlayer has a registration on the post office.
	 * 
	 * @return true if the player is registered. Otherwise, it will return false.
	 * 
	 */
	@NotNull
	public final Boolean isRegistered() {
		return DatabaseModule.User.isRegistered(uuid);
	}
	/**
	 * 
	 * Gets the registry date of the LTPlayer (dd/MM/yyyy).
	 * 
	 */
	public final String getRegistryDate() {
		return DatabaseModule.User.getRegistryDate(uuid);
	}
	/**
	 * 
	 * Sends a toast message to the LTPlayer.
	 * 
	 * @param message The message that will be shown.
	 * 
	 */
	@NotNull
	public final void sendToastMessage(@NotNull final String message) {
		try {
			if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI)) {
				final LTUltimateAdvancementAPI ultimateAdvancementAPI = (LTUltimateAdvancementAPI) ExtensionModule.getInstance().get(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI);
				ultimateAdvancementAPI.show(this, message);
			}
		} catch(final IllegalArgumentException e) {
			ConsoleModule.debug(getClass(), "Argument cannot be null.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
	}
	/**
	 * 
	 * Checks if the player is online.
	 * 
	 */
	public final Boolean isOnline() {
		return getBukkitPlayer().getPlayer() != null;
	}
	/**
	 * 
	 * Sends a chat message to the LTPlayer.
	 * 
	 * @param message The message that will be shown.
	 * 
	 */
	public final Boolean sendMessage(@NotNull final String message) {
		if(getBukkitPlayer().getPlayer() != null) {
			getBukkitPlayer().getPlayer().sendMessage(message);
			return true;
		}
		return false;
	}
	public final Spigot spigot() {
		try {
			Class.forName("org.spigotmc.CustomTimingsHandler.class");
			return new Spigot(this);
		} catch (final ClassNotFoundException e) {
			ConsoleModule.debug(getClass(), "Server is not a Spigot implementation.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return null;
	}
	public static class Spigot {
		private final LTPlayer player;
		private Spigot(final LTPlayer player) {
			this.player = player;
		}
		/**
		 * 
		 * Sends a message to the LTPlayer.
		 * 
		 * @param type The screen position.
		 * @param component The component to send.
		 * 
		 */
		public final Boolean sendMessage(@NotNull final ChatMessageType type, @NotNull final BaseComponent[] component) {
			if(player.getBukkitPlayer().getPlayer() != null) {
				player.getBukkitPlayer().getPlayer().spigot().sendMessage(type, component);
				return true;
			}
			return false;
		}
	}
}