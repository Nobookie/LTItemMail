package br.net.gmj.nobookie.LTItemMail.api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Iterables;
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
	private static LTItemMailAPI instance = null;
	public static final LTItemMailAPI get(@Nonnull final JavaPlugin plugin) {
		Objects.requireNonNull(plugin);
		if(instance == null) instance = new LTItemMailAPI();
		if(plugin != LTItemMail.getInstance() && !LTItemMail.getInstance().apiHandlers.contains(plugin)) {
			LTItemMail.getInstance().apiHandlers.add(plugin);
			ConsoleModule.info("Hooked into " + plugin.getDescription().getName());
		}
		return instance;
	}
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
	@Nonnull
	public final void sendSpecialMail(@Nonnull final LTPlayer player, @Nonnull final LinkedList<ItemStack> items, @Nonnull final String label) throws NullPointerException {
		Objects.requireNonNull(player);
		Objects.requireNonNull(items);
		Objects.requireNonNull(label);
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
			try {
				final Player first = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
				if(first != null) {
					final ByteArrayDataOutput out = ByteStreams.newDataOutput();
					out.writeUTF("Forward");
					out.writeUTF("ONLINE");
					out.writeUTF("LTIM_SM");
					final ByteArrayOutputStream msgbytesout = new ByteArrayOutputStream();
					final DataOutputStream msgout = new DataOutputStream(msgbytesout);
					msgout.writeUTF(player.getName());
					msgout.writeUTF((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_SPECIAL));
					out.writeShort(msgbytesout.toByteArray().length);
					out.write(msgbytesout.toByteArray());
					first.sendPluginMessage(LTItemMail.getInstance(), "BungeeCord", out.toByteArray());
				} else ConsoleModule.warning("É necessário ter um jogador online no servidor para poder enviar mensagens no canal BungeeCord.");
			} catch(final IOException e) {
				ConsoleModule.debug(LTItemMailAPI.class, "Unable to send message to BungeeCord channel.");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
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
	@Nonnull
	public final List<MailboxBlock> getMailboxBlockList(){
		final List<MailboxBlock> blocks = new ArrayList<>();
		for(final MailboxBlock mailbox : DatabaseModule.Block.getMailboxBlocks()) if(mailbox.getServer().equals(ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID))) blocks.add(mailbox);
		return blocks;
	}
	/**
	 * 
	 * Gets all existing mailbox
	 * blocks standing in
	 * one or more worlds
	 * from a specific owner.
	 * 
	 * @param owner The specific owner as a {@link LTPlayer} object.
	 * 
	 * @return List of {@link MailboxBlock} from a owner.
	 * 
	 */
	@Nonnull
	public final List<MailboxBlock> getMailboxBlockList(@Nonnull final LTPlayer owner) throws NullPointerException {
		Objects.requireNonNull(owner);
		final List<MailboxBlock> blocks = new ArrayList<>();
		for(final MailboxBlock mailbox : DatabaseModule.Block.getMailboxBlocks()) if(mailbox.getServer().equals(ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID)) && mailbox.getOwner().getUniqueId().equals(owner.getUniqueId())) blocks.add(mailbox);
		return blocks;
	}
	/**
	 * 
	 * Gets all existing mailbox
	 * blocks standing in
	 * one or more worlds of
	 * a specific server.
	 * 
	 * @param server The server ID.
	 * 
	 * @return List of {@link MailboxBlock}.
	 * 
	 */
	@Nonnull
	public final List<MailboxBlock> getMailboxBlockList(@Nonnull final String server) throws NullPointerException {
		Objects.requireNonNull(server);
		final List<MailboxBlock> blocks = new ArrayList<>();
		for(final MailboxBlock mailbox : DatabaseModule.Block.getMailboxBlocks()) if(mailbox.getServer().equals(server)) blocks.add(mailbox);
		return blocks;
	}
	/**
	 * 
	 * Gets all existing mailbox
	 * blocks standing in
	 * one or more worlds of
	 * a specific server
	 * from a specific owner.
	 * 
	 * @param server The server ID.
	 * @param owner The specific owner as a {@link LTPlayer} object.
	 * 
	 * @return List of {@link MailboxBlock} from a owner.
	 * 
	 */
	@Nonnull
	public final List<MailboxBlock> getMailboxBlockList(@Nonnull final String server, @Nonnull final LTPlayer owner) throws NullPointerException {
		Objects.requireNonNull(server);
		Objects.requireNonNull(owner);
		final List<MailboxBlock> blocks = new ArrayList<>();
		for(final MailboxBlock mailbox : DatabaseModule.Block.getMailboxBlocks()) if(mailbox.getServer().equals(server) && mailbox.getOwner().getUniqueId().equals(owner.getUniqueId())) blocks.add(mailbox);
		return blocks;
	}
	/**
	 * 
	 * Gets a LT Item Mail block
	 * on a provided location.
	 * 
	 * @param location The location to look (See {@link Location}).
	 * 
	 * @return The {@link MailboxBlock} object or null if not found.
	 * 
	 */
	@Nullable
	public final MailboxBlock getMailboxBlock(@Nonnull final Location location) throws NullPointerException {
		Objects.requireNonNull(location);
		if(DatabaseModule.Block.isMailboxBlock(location)) for(final MailboxBlock mailbox : getMailboxBlockList()) if(mailbox.getServer().equals(ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID)) && mailbox.getLocation().equals(location)) return mailbox;
		return null;
	}
	/**
	 * 
	 * Gets a LT Item Mail block
	 * on a provided location
	 * on a specific server.
	 * 
	 * @param server The server ID.
	 * @param location The location to look (See {@link Location}).
	 * 
	 * @return The {@link MailboxBlock} object or null if not found.
	 * 
	 */
	@Nullable
	public final MailboxBlock getMailboxBlock(@Nonnull final String server, @Nonnull final Location location) throws NullPointerException {
		Objects.requireNonNull(server);
		Objects.requireNonNull(location);
		if(DatabaseModule.Block.isMailboxBlock(location)) for(final MailboxBlock mailbox : getMailboxBlockList()) if(mailbox.getServer().equals(server) && mailbox.getLocation().equals(location)) return mailbox;
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
	@Nullable
	public final List<String> getBungeeOnlinePlayers(){
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
	@Nullable
	public final List<LTPlayer> getBungeeOnlineLTPlayers(){
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) {
			final List<LTPlayer> players = new ArrayList<>();
			for(final String name : getBungeeOnlinePlayers()) players.add(LTPlayer.fromName(name));
			return players;
		}
		return null;
	}
}