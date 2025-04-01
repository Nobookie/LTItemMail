package br.net.gmj.nobookie.LTItemMail.module.ext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.OfflinePlayer;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public final class LTPlaceholderAPI extends PlaceholderExpansion implements LTExtension {
	public LTPlaceholderAPI() {
		register();
	}
	@Override
	public final void unload() {
		unregister();
	}
	@Override
	public final String getIdentifier() {
		return LTItemMail.getInstance().getDescription().getName().toLowerCase();
	}
	@Override
	public final String getAuthor() {
		return String.join(", ", LTItemMail.getInstance().getDescription().getAuthors());
	}
	@Override
	public final String getVersion() {
		return (String) ConfigurationModule.get(ConfigurationModule.Type.VERSION_NUMBER);
	}
	@Override
	public final boolean persist() {
		return true;
	}
	@Override
	public final String onRequest(final OfflinePlayer player, final String placeholder) {
		final String ph = placeholder.toLowerCase();
		LTPlayer ltPlayer = null;
		if(player != null) ltPlayer = LTPlayer.fromName(player.getName());
		if(ltPlayer != null && ltPlayer.isRegistered() && placeholder != null) switch(ph) {
			case "player_mailsent":
				return String.valueOf(ltPlayer.getMailSentCount());
			case "player_mailreceived":
				return String.valueOf(ltPlayer.getMailReceivedCount());
			case "player_banreason":
				return ltPlayer.getBanReason();
			case "player_registered":
				return ltPlayer.getRegistryDate();
			case "player_banned":
				if(ltPlayer.isBanned()) return LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_YES);
				return LanguageModule.get(LanguageModule.Type.COMMAND_PLAYER_INFO_BANNED_NO);
			default:
				if(ph.startsWith("player_")) {
					final String[] phSplit = ph.split("_");
					if(phSplit.length > 1) switch(phSplit[1]) {
						case "pending":
						case "accepted":
						case "denied":
							if(phSplit.length > 2) try {
								
								HashMap<Integer, String> mails = null;
								switch(phSplit[1]) {
									case "pending":
										mails = DatabaseModule.Virtual.getMailboxesList(ltPlayer.getUniqueId(), DatabaseModule.Virtual.Status.PENDING);
										break;
									case "accepted":
										mails = DatabaseModule.Virtual.getMailboxesList(ltPlayer.getUniqueId(), DatabaseModule.Virtual.Status.ACCEPTED);
										break;
									case "denied":
										mails = DatabaseModule.Virtual.getMailboxesList(ltPlayer.getUniqueId(), DatabaseModule.Virtual.Status.DENIED);
										break;
								}
								if(phSplit[2].equals("total")) {
									return String.valueOf(mails.size());
								} else {
									Integer pos = Integer.parseInt(phSplit[2]);
									if(mails.size() >= pos) {
										final List<Integer> ids = new ArrayList<>();
										for(final Integer id : mails.keySet()) ids.add(id);
										if(phSplit.length > 3) switch(phSplit[3]) {
											case "id":
												return String.valueOf(ids.get((pos - 1)));
											case "date":
												return mails.get(ids.get((pos - 1)));
											case "label":
												return DatabaseModule.Virtual.getMailboxLabel(ids.get((pos - 1)));
											case "from":
												return LTPlayer.fromUUID(DatabaseModule.Virtual.getMailboxFrom(ids.get((pos - 1)))).getName();
										}
										return "#" + ids.get((pos - 1)) + " (" + mails.get(ids.get((pos - 1))) + ")";
									}
								}
							} catch(final NumberFormatException e) {
								ConsoleModule.severe("Invalid placeholder %" + ph + "% (called by player " + ltPlayer.getName() + ")");
								ConsoleModule.severe("You must inform a valid position number. Ex: %ltitemmail_player_pending_1% (First), %ltitemmail_player_pending_2% (Second)...");
								if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
							}
							break;
					}
				}
		}
		return "";
	}
}