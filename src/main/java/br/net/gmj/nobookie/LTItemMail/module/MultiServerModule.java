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
package br.net.gmj.nobookie.LTItemMail.module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule.Action;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;

public class MultiServerModule {
	private static MultiServerModule instance = null;
	private MultiServerInstance handle = null;
	public MultiServerModule() {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_ENABLE)) {
			if(((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_MODE)).equalsIgnoreCase("bungee")) handle = new Bungee();
			if(((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_MODE)).equalsIgnoreCase("redis")) handle = new Redis();
		}
	}
	public static final void load() {
		if(instance == null) instance = new MultiServerModule();
	}
	public static final void unload() {
		if(instance != null && instance.handle != null) instance.handle.unload();
	}
	public static final MultiServerInstance getHandle() {
		if(instance != null && instance.handle != null) return instance.handle;
		return null;
	}
	private static class Bungee implements MultiServerInstance, PluginMessageListener {
		private final List<String> players = new ArrayList<>();
		private BukkitTask task = null;
		public Bungee() {
			task = new BukkitRunnable() {
				@Override
				public final void run() {
					final Player first = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
					if(first != null) {
						final ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("PlayerList");
						out.writeUTF("ALL");
						first.sendPluginMessage(LTItemMail.getInstance(), "BungeeCord", out.toByteArray());
					}
				}
			}.runTaskTimer(LTItemMail.getInstance(), 20, 20);
		}
		@Override
		public final void onPluginMessageReceived(final String channel, final Player bukkitPlayer, final byte[] byteMessage) {
			if(!channel.equals("BungeeCord")) return;
			final ByteArrayDataInput in = ByteStreams.newDataInput(byteMessage);
			final String subchannel = in.readUTF();
			try {
				if(subchannel.equals("LTIM_MBR")) {
					final short len = in.readShort();
					final byte[] msgbytesin = new byte[len];
					in.readFully(msgbytesin);
					final DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytesin));
					final LTPlayer sender = LTPlayer.fromName(msgin.readUTF());
					final LTPlayer receiver = LTPlayer.fromName(msgin.readUTF());
					final Integer mailbox = Integer.parseInt(msgin.readUTF());
					if(receiver != null && receiver.getBukkitPlayer().getPlayer() != null) {
						final Player bukkitReceiver = receiver.getBukkitPlayer().getPlayer();
						MailboxModule.log(receiver, null, Action.RECEIVED, mailbox.intValue(), null, null, null);
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
								bukkitReceiver.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + ChatColor.GREEN + "" + sender.getName() + " (#" + mailbox + ")");
								break;
							case TITLE:
								bukkitReceiver.sendTitle(ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) +  " " + ChatColor.GREEN, sender.getName() + " (#" + mailbox + ")", 20 * 1, 20 * 5, 20 * 1);
								break;
							case TOAST:
								if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI)) ((LTUltimateAdvancementAPI) ExtensionModule.getInstance().get(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI)).show(receiver, LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + sender.getName() + " (#" + mailbox + ")");
								break;
						}
					}
				}
				if(subchannel.equals("LTIM_GMB")) {
					final short len = in.readShort();
					final byte[] msgbytesin = new byte[len];
					in.readFully(msgbytesin);
					final DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytesin));
					final String sender = msgin.readUTF();
					final Player receiver = LTPlayer.fromName(msgin.readUTF()).getBukkitPlayer().getPlayer();
					String data;
					if(receiver != null) {
						BukkitUtil.PlayerInventory.addItem(receiver, new MailboxItem().getItem(null));
						if(BukkitUtil.PlayerInventory.hasSpace(receiver.getInventory())) {
							data = (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_ADDED);
						} else data = (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_DROPPED);
					} else data = (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.COMMAND_ADMIN_GIVE_OFFLINE);
					send("LTIM_SM", sender, data);
				}
				if(subchannel.equals("LTIM_SM")) {
					final short len = in.readShort();
					final byte[] msgbytesin = new byte[len];
					in.readFully(msgbytesin);
					final DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytesin));
					final String receiver = msgin.readUTF();
					final String message = msgin.readUTF();
					if(receiver.equals("CONSOLE")) {
						Bukkit.getConsoleSender().sendMessage(message);
					} else if(Bukkit.getPlayer(receiver) != null) Bukkit.getPlayer(receiver).sendMessage(message);
				}
				if(subchannel.equals("PlayerList")) {
					final String server = in.readUTF();
					if(server.equals("ALL")) {
						players.clear();
						final String[] playerList = in.readUTF().split(", ");
						for(final String fromBungee : playerList) players.add(fromBungee);
					}
				}
			} catch(final IOException e) {
				ConsoleModule.debug(getClass(), "Unable to send/receive message from BungeeCord channel.");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		@Override
		public final List<String> getOnlinePlayers(){
			return players;
		}
		@Override
		public final void load() {
			Bukkit.getMessenger().registerOutgoingPluginChannel(LTItemMail.getInstance(), "BungeeCord");
			Bukkit.getMessenger().registerIncomingPluginChannel(LTItemMail.getInstance(), "BungeeCord", this);
		}
		@Override
		public final void unload() {
			if(task != null) task.cancel();
			Bukkit.getMessenger().unregisterOutgoingPluginChannel(LTItemMail.getInstance(), "BungeeCord");
			Bukkit.getMessenger().unregisterIncomingPluginChannel(LTItemMail.getInstance(), "BungeeCord");
		}
		@Override
		public final void send(final String channel, final String...messages) {
			try {
				final Player first = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
				if(first != null) {
					final ByteArrayDataOutput out = ByteStreams.newDataOutput();
					out.writeUTF("Forward");
					out.writeUTF("ONLINE");
					out.writeUTF(channel);
					final ByteArrayOutputStream msgbytesout = new ByteArrayOutputStream();
					final DataOutputStream msgout = new DataOutputStream(msgbytesout);
					for(final String message : messages) msgout.writeUTF(message);
					out.writeShort(msgbytesout.toByteArray().length);
					out.write(msgbytesout.toByteArray());
					first.sendPluginMessage(LTItemMail.getInstance(), "BungeeCord", out.toByteArray());
				} else ConsoleModule.warning("É necessário ter um jogador online no servidor para poder enviar mensagens no canal BungeeCord.");
			} catch(final IOException e) {
				ConsoleModule.debug(MultiServerModule.Bungee.class, "Unable to send message to BungeeCord channel.");
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
	}
	private static class Redis implements MultiServerInstance {
		@Override
		public final List<String> getOnlinePlayers(){
			return null;
		}
		@Override
		public final void load() {
			
		}
		@Override
		public final void unload() {
			
		}
		@Override
		public final void send(final String channel, final String...messages) {
			
		}
	}
	public interface MultiServerInstance {
		public abstract List<String> getOnlinePlayers();
		public abstract void load();
		public abstract void unload();
		public abstract void send(String channel, String...messages);
	}
}