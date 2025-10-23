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
package br.net.gmj.nobookie.LTItemMail.api.entity;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;
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
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nullable
	public static final LTPlayer fromName(@Nonnull final String name) throws NullPointerException {
		Objects.requireNonNull(name);
		final UUID uuid = FetchUtil.Player.fromName(name);
		if(uuid != null) return new LTPlayer(Bukkit.getOfflinePlayer(uuid), FetchUtil.Player.fromUUID(uuid), uuid);
		return null;
	}
	/**
	 * 
	 * Creates the LTPlayer object from a player's unique id.
	 * 
	 * @param uuid The player UUID.
	 * 
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nullable
	public static final LTPlayer fromUUID(@Nonnull final UUID uuid) throws NullPointerException {
		Objects.requireNonNull(uuid);
		final String name = FetchUtil.Player.fromUUID(uuid);
		if(name != null) return new LTPlayer(Bukkit.getOfflinePlayer(FetchUtil.Player.fromName(name)), name, FetchUtil.Player.fromName(name));
		return null;
	}
	/**
	 * 
	 * Converts from Bukkit player to LTPlayer (same as {@link LTPlayer#fromName(String)}.
	 * 
	 * @param player The {@link OfflinePlayer} object from Bukkit.
	 * 
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nullable
	public static final LTPlayer fromBukkitPlayer(@Nonnull final OfflinePlayer player) throws NullPointerException {
		Objects.requireNonNull(player);
		return LTPlayer.fromName(player.getName());
	}
	/**
	 * 
	 * Converts LTPlayer into Bukkit player.
	 * 
	 * @return {@link OfflinePlayer} representing the vanilla player from Bukkit.
	 * 
	 */
	@Nonnull
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
	@Nonnull
	public final void forceSend(@Nonnull final LTPlayer playerTo, @Nonnull final LinkedList<ItemStack> items, @Nonnull final String label) throws NullPointerException {
		Objects.requireNonNull(playerTo);
		Objects.requireNonNull(items);
		Objects.requireNonNull(label);
		MailboxModule.send(player.getPlayer(), playerTo, items, label);
	}
	/**
	 * 
	 * Gives to the LTPlayer a mailbox block.
	 * 
	 * @return {@link Boolean#TRUE} if the player is online and received the mailbox block.
	 * 
	 */
	@Nonnull
	public final Boolean giveMailboxBlock() {
		final Player player = this.player.getPlayer();
		if(player != null) {
			BukkitUtil.PlayerInventory.addItem(player, new MailboxItem().getItem(null));
			return true;
		}
		return false;
	}
	/**
	 * 
	 * Gets the LTPlayer user name.
	 * 
	 * @return {@link String} representing the player name.
	 * 
	 */
	@Nonnull
	public final String getName() {
		return name;
	}
	/**
	 * 
	 * Gets the LTPlayer unique id.
	 * 
	 * @return {@link UUID} representing the player unique id.
	 * 
	 */
	@Nonnull
	public final UUID getUniqueId() {
		return uuid;
	}
	/**
	 * 
	 * Gets the ban reason if the LTPlayer is banned.
	 * 
	 * @return {@link String} representing the ban reason or null.
	 * 
	 */
	@Nullable
	public final String getBanReason() {
		if(isRegistered()) return DatabaseModule.User.getBanReason(uuid);
		return null;
	}
	/**
	 * 
	 * Checks if the LTPlayer is banned from the post office (cannot send items, receive only).
	 * 
	 * @return {@link Boolean#TRUE} if the player is banned. Otherwise, it will return false.
	 * 
	 */
	@Nonnull
	public final Boolean isBanned() {
		if(isRegistered()) return DatabaseModule.User.isBanned(uuid);
		return false;
	}
	/**
	 * 
	 * Gets the count of mails sent from the LTPlayer.
	 * 
	 * @return {@link Integer} representing the amount of mails sent by this player.
	 * 
	 */
	@Nonnull
	public final Integer getMailSentCount() {
		if(isRegistered()) return DatabaseModule.User.getSentCount(uuid);
		return 0;
	}
	/**
	 * 
	 * Gets the count of mails sent to the LTPlayer.
	 * 
	 * @return {@link Integer} representing the amount of mails received by this player.
	 * 
	 */
	@Nonnull
	public final Integer getMailReceivedCount() {
		if(isRegistered()) return DatabaseModule.User.getReceivedCount(uuid);
		return 0;
	}
	/**
	 * 
	 * Checks if the LTPlayer has a registration on the post office.
	 * 
	 * @return {@link Boolean#TRUE} if the player is registered. Otherwise, it will return false.
	 * 
	 */
	@Nonnull
	public final Boolean isRegistered() {
		return DatabaseModule.User.isRegistered(uuid);
	}
	/**
	 * 
	 * Gets the registry date of the LTPlayer (dd/MM/yyyy).
	 * 
	 * @return {@link String} or null
	 * 
	 */
	@Nullable
	public final String getRegistryDate() {
		return DatabaseModule.User.getRegistryDate(uuid);
	}
	/**
	 * 
	 * Sends a toast message to the LTPlayer.
	 * 
	 * @param message The message that will be shown.
	 * 
	 * @return {@link Boolean#TRUE} if the toast message was successfully sent.
	 * 
	 */
	@Nonnull
	public final Boolean sendToastMessage(@Nonnull final String message) throws NullPointerException {
		Objects.requireNonNull(message);
		if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI)) {
			final LTUltimateAdvancementAPI ultimateAdvancementAPI = (LTUltimateAdvancementAPI) ExtensionModule.getInstance().get(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI);
			ultimateAdvancementAPI.show(this, message);
			return true;
		}
		return false;
	}
	/**
	 * 
	 * Checks if the player is online.
	 * 
	 * @return {@link Boolean#TRUE} if the player is currently online.
	 * 
	 */
	@Nonnull
	public final Boolean isOnline() {
		return getBukkitPlayer().getPlayer() != null;
	}
	/**
	 * 
	 * Sends a chat message to the LTPlayer.
	 * 
	 * @param message The message that will be shown.
	 * 
	 * @return {@link Boolean#TRUE} if the message was successfully sent.
	 * 
	 */
	@Nonnull
	public final Boolean sendMessage(@Nonnull final String message) throws NullPointerException {
		Objects.requireNonNull(message);
		if(getBukkitPlayer().getPlayer() != null) {
			getBukkitPlayer().getPlayer().sendMessage(message);
			return true;
		}
		return false;
	}
	/**
	 * 
	 * Works with Spigot and forks only.
	 * 
	 * @return {@link Spigot} object or null.
	 * 
	 */
	@Nullable
	public final Spigot spigot() {
		try {
			Class.forName("org.spigotmc.CustomTimingsHandler.class");
			return new Spigot(this);
		} catch (final ClassNotFoundException e) {
			ConsoleModule.debug(getClass(), "Server is not a Spigot implementation. Aborted.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return null;
	}
	/**
	 * 
	 * Works with Spigot and forks only.
	 * 
	 * @author Nobookie
	 * 
	 */
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
		 * @return {@link Boolean#TRUE} if the message was successfully sent.
		 * 
		 */
		@Nonnull
		public final Boolean sendMessage(@Nonnull final ChatMessageType type, @Nonnull final BaseComponent[] component) throws NullPointerException {
			Objects.requireNonNull(type);
			Objects.requireNonNull(component);
			if(player.getBukkitPlayer().getPlayer() != null) {
				player.getBukkitPlayer().getPlayer().spigot().sendMessage(type, component);
				return true;
			}
			return false;
		}
	}
}