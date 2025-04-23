package br.net.gmj.nobookie.LTItemMail.module;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.event.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule.Action;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTUltimateAdvancementAPI;

public final class BungeeModule implements PluginMessageListener {
	private static final List<String> players = new ArrayList<>();
	public BungeeModule() {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_MODE)) new BukkitRunnable() {
			@Override
			public final void run() {
				final ByteArrayDataOutput bungee = ByteStreams.newDataOutput();
				bungee.writeUTF("PlayerList");
				bungee.writeUTF("ALL");
				Bukkit.getServer().sendPluginMessage(LTItemMail.getInstance(), "BungeeCord", bungee.toByteArray());
			}
		}.runTaskTimer(LTItemMail.getInstance(), 10, 10);
	}
	private final LTUltimateAdvancementAPI ultimateAdvancementAPI = (LTUltimateAdvancementAPI) ExtensionModule.getInstance().get(ExtensionModule.EXT.ULTIMATEADVANCEMENTAPI);
	@Override
	public final void onPluginMessageReceived(final String channel, final Player player, final byte[] message) {
		if(!channel.equals("BungeeCord")) return;
		final ByteArrayDataInput in = ByteStreams.newDataInput(message);
		final String subchannel = in.readUTF();
		try {
			switch(subchannel) {
				case "LTItemMail_MailboxReceived":
					byte[] func = new byte[in.readShort()];
					in.readFully(func);
					final DataInputStream funcMsg = new DataInputStream(new ByteArrayInputStream(func));
					final String[] function = funcMsg.readUTF().split(";");
					final LTPlayer sender = LTPlayer.fromName(function[0]);
					final LTPlayer receiver = LTPlayer.fromName(function[1]);
					final Integer mailboxID = Integer.parseInt(function[2]);
					if(receiver != null && receiver.getBukkitPlayer().getPlayer() != null) {
						final Player bukkitReceiver = receiver.getBukkitPlayer().getPlayer();
						MailboxModule.log(receiver, null, Action.RECEIVED, mailboxID, null, null, null);
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
								bukkitReceiver.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + ChatColor.GREEN + "" + sender.getName() + " (#" + mailboxID + ")");
								break;
							case TITLE:
								bukkitReceiver.sendTitle(ChatColor.AQUA + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) +  " " + ChatColor.GREEN, sender.getName() + " (#" + mailboxID + ")", 20 * 1, 20 * 5, 20 * 1);
								break;
							case TOAST:
								if(ultimateAdvancementAPI != null) ultimateAdvancementAPI.show(receiver, LanguageModule.get(LanguageModule.Type.MAILBOX_FROM) + " " + sender.getName() + " (#" + mailboxID + ")");
								break;
						}
					}
					break;
				case "PlayerList":
					final String server = in.readUTF();
					if(server.equals("ALL")) {
						players.clear();
						final String[] playerList = in.readUTF().split(", ");
						for(final String bungeePlayer : playerList) players.add(bungeePlayer);
					}
					break;
			}
		} catch(final IOException e) {
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
	}
	public static final List<String> getOnlinePlayers(){
		return players;
	}
}