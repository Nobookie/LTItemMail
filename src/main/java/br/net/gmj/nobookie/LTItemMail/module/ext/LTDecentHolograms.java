package br.net.gmj.nobookie.LTItemMail.module.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public final class LTDecentHolograms implements LTExtension {
	private final Plugin plugin;
	private final List<Material> items;
	public LTDecentHolograms(final Plugin plugin) {
		this.plugin = plugin;
		items = new ArrayList<>();
		for(final Material material : Material.values()) {
			final String name = material.toString();
			if(!name.startsWith("LEGACY_") && (name.endsWith("_AXE") || name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE"))) items.add(material);
		}
		Collections.shuffle(items);
	}
	@Override
	public final Plugin getBasePlugin() {
		return plugin;
	}
	@Override
	public final void unload() {}
	public final void cleanup() {
		Bukkit.getScheduler().runTaskLater(LTItemMail.getInstance(), new Runnable() {
			@Override
			public final void run() {
				ConsoleModule.debug(getClass(), "#cleanup: performing");
				for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks()) deleteHolo(block.getOwner().getBukkitPlayer(), block.getLocation());
				for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks()) createHolo(block.getOwner().getBukkitPlayer(), block.getLocation());
				ConsoleModule.debug(getClass(), "#cleanup: done");
			}
		}, 1);
	}
	public final void createHolo(final OfflinePlayer player, final Location location) {
		final World world = location.getWorld();
		final Integer x = location.getBlockX();
		final Integer y = location.getBlockY() + 2;
		final Integer z = location.getBlockZ();
		final String id = player.getName() + "_" + world.getName() + "_" + x + "_" + y + "_" + z;
		final Location holoLocation = new Location(world, x + 0.5, y + 0.5, z + 0.5);
		if(DHAPI.getHologram(id) != null) deleteHolo(player, location);
		final Hologram holo = DHAPI.createHologram(id, holoLocation, true);
		DHAPI.addHologramLine(holo, items.get(new Random().nextInt(items.size() - 1)));
		DHAPI.addHologramLine(holo, "<#ANIM:wave:" + ChatColor.GREEN + "" + ChatColor.BOLD + "," + ChatColor.GOLD + "" + ChatColor.BOLD + ">" + LanguageModule.get(LanguageModule.Type.BLOCK_NAME) + "</#ANIM>");
		DHAPI.addHologramLine(holo, ChatColor.GOLD + LanguageModule.get(LanguageModule.Type.BLOCK_OWNER) + " " + ChatColor.AQUA + player.getName());
		ConsoleModule.debug(getClass(), "#createHolo: " + id);
	}
	public final void deleteHolo(final OfflinePlayer player, final Location location) {
		final String world = location.getWorld().getName();
		final Integer x = location.getBlockX();
		final Integer y = location.getBlockY() + 2;
		final Integer z = location.getBlockZ();
		final String id = player.getName() + "_" + world + "_" + x + "_" + y + "_" + z;
		if(DHAPI.getHologram(id) != null) {
			DHAPI.removeHologram(id);
			ConsoleModule.debug(getClass(), "#deleteHolo: " + id);
		}
	}
}