package br.net.gmj.nobookie.LTItemMail.module.ext.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.event.BreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;

public final class LTTownyAdvancedListener implements Listener {
	public LTTownyAdvancedListener() {
		Bukkit.getServer().getPluginManager().registerEvents(this, LTItemMail.getInstance());
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlotUnclaim(final TownUnclaimEvent event) {
		final BoundingBox chunk = event.getWorldCoord().getBoundingBox();
		for(double x = chunk.getMinX(); x < chunk.getMaxX(); x++) for(double z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) for(double y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
			final Block block = new Location(event.getWorldCoord().getBukkitWorld(), x, y, z).getBlock();
			if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
				Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.TOWNYADVANCED));
				final ItemStack mailbox = new MailboxItem().getItem(null);
				mailbox.setType(block.getType());
				block.setType(Material.AIR);
				block.getWorld().dropItem(block.getLocation(), mailbox);
			}
		}
	}
}