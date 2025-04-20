package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.DeleteRegionEvent;
import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.event.BreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;

public final class LTRedProtect implements LTExtension, Listener {
	public LTRedProtect() {
		Bukkit.getPluginManager().registerEvents(this, LTItemMail.getInstance());
	}
	@Override
	public final void unload() {
		HandlerList.unregisterAll(this);
	}
	public final boolean canBuildBreak(final Player player, final Location location) {
		final Region region = RedProtect.get().getAPI().getRegion(location);
		Boolean result = true;
		if(region != null) result = (region.canBuild(player) && region.canBreak(location.getBlock().getType()));
		ConsoleModule.debug(getClass(), "#canBuildBreak: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canInteract(final Player player, final Location location) {
		final Region region = RedProtect.get().getAPI().getRegion(location);
		Boolean result = true;
		if(region != null) result = region.canChest(player);
		ConsoleModule.debug(getClass(), "#canInteract: " + player.getName() + " " + result);
		return result;
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onRegionDelete(final DeleteRegionEvent event) {
		final Location min = event.getRegion().getMinLocation();
		final Location max = event.getRegion().getMaxLocation();
		for(int x = min.getBlockX(); x < max.getBlockX(); x++) for(int z = min.getBlockZ(); z < max.getBlockZ(); z++) for(int y = min.getBlockY(); y < max.getBlockY(); y++) {
			final Block block = new Location(min.getWorld(), x, y, z).getBlock();
			if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
				Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.REDPROTECT));
				final ItemStack mailbox = new MailboxItem().getItem(null);
				mailbox.setType(block.getType());
				block.setType(Material.AIR);
				block.getWorld().dropItem(block.getLocation(), mailbox);
			}
		}
	}
}