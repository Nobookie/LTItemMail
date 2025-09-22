package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.PreDeleteTownEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.api.event.BreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.EconomyModule;

public final class LTTownyAdvanced implements LTExtension, Listener {
	private final TownyAPI api;
	public LTTownyAdvanced() {
		Bukkit.getPluginManager().registerEvents(this, LTItemMail.getInstance());
		api = TownyAPI.getInstance();
	}
	@Override
	public final void unload() {
		HandlerList.unregisterAll(this);
	}
	public final boolean canBuild(final Player player, final Location location) {
		final TownBlock block = api.getTownBlock(location);
		Boolean result = true;
		if(block != null) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.BUILD);
		ConsoleModule.debug(getClass(), "#canBuild: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canBreak(final Player player, final Location location) {
		final TownBlock block = api.getTownBlock(location);
		Boolean result = true;
		if(block != null) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.DESTROY);
		ConsoleModule.debug(getClass(), "#canBreak: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canInteract(final Player player, final Location location) {
		final TownBlock block = api.getTownBlock(location);
		Boolean result = true;
		if(block != null) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.ITEM_USE);
		ConsoleModule.debug(getClass(), "#canInteract: " + player.getName() + " " + result);
		return result;
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onPlotUnclaim(final TownUnclaimEvent event) {
		final BoundingBox chunk = event.getWorldCoord().getBoundingBox();
		for(double x = chunk.getMinX(); x < chunk.getMaxX(); x++) for(double z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) for(double y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
			final Block block = new Location(event.getWorldCoord().getBukkitWorld(), x, y, z).getBlock();
			if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
				Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.TOWNYADVANCED, event));
				block.setType(Material.AIR);
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onTownRuined(final TownRuinedEvent event) {
		for(final TownBlock townBlock : event.getTown().getTownBlocks()) {
			final BoundingBox chunk = townBlock.getWorldCoord().getBoundingBox();
			for(double x = chunk.getMinX(); x < chunk.getMaxX(); x++) for(double z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) for(double y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
				final Block block = new Location(townBlock.getWorldCoord().getBukkitWorld(), x, y, z).getBlock();
				if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
					Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.TOWNYADVANCED, event));
					block.setType(Material.AIR);
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onTownDeleted(final PreDeleteTownEvent event) {
		for(final TownBlock townBlock : event.getTown().getTownBlocks()) {
			final BoundingBox chunk = townBlock.getWorldCoord().getBoundingBox();
			for(double x = chunk.getMinX(); x < chunk.getMaxX(); x++) for(double z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) for(double y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
				final Block block = new Location(townBlock.getWorldCoord().getBukkitWorld(), x, y, z).getBlock();
				if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
					Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.TOWNYADVANCED, event));
					block.setType(Material.AIR);
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onNewDay(final NewDayEvent event) {
		Integer tax = 0;
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ECONOMY_ENABLE) && (Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_ENABLE)) for(final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks(player.getUniqueId())) {
				if(!block.getServer().equals((String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID))) continue;
				final TownBlock townBlock = api.getTownBlock(block.getLocation());
				if(townBlock != null) if(EconomyModule.getInstance().has(player, (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST))) {
					if(EconomyModule.getInstance().withdraw(player, (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST))) {
						tax = tax + (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST);
						try {
							townBlock.getTown().depositToBank(api.getResident(player.getUniqueId()), (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST));
						} catch (final TownyException e) {
							if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
						}
					} else block.remove(false);
				} else block.remove(false);
			}
			if(tax > 0 && player.getPlayer() != null) player.getPlayer().sendMessage("Você pagou " + tax + " de taxa adicional para ter caixas de corrêio em plots protegidos.");
		}
	}
}