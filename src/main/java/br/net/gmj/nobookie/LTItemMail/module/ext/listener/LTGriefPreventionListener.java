package br.net.gmj.nobookie.LTItemMail.module.ext.listener;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.event.BreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;

public class LTGriefPreventionListener implements Listener {
	public LTGriefPreventionListener() {
		Bukkit.getServer().getPluginManager().registerEvents(this, LTItemMail.getInstance());
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onClaimDelete(final ClaimDeletedEvent event) {
		for(final Chunk chunk : event.getClaim().getChunks()) for(int x = 0; x < 15; x++) for(int z = 0; z < 15; z++) for(int y = 0; y < 255; y++) {
			final Block block = chunk.getBlock(x, y, z);
			if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
				Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.GRIEFPREVENTION));
				final ItemStack mailbox = new MailboxItem().getItem(null);
				mailbox.setType(block.getType());
				block.setType(Material.AIR);
				block.getWorld().dropItem(block.getLocation(), mailbox);
			}
		}
	}
}