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
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
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
		Boolean result = true;
		try {
			final TownBlock block = api.getTownBlock(location);
			if(block != null && !block.getTown().getMayor().getName().equals(player.getName())) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.BUILD);
			ConsoleModule.debug(getClass(), "#canBuild: " + player.getName() + " " + result);
		} catch (final NotRegisteredException e) {
			ConsoleModule.debug(getClass(), "Unable to check for plot permissions.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return result;
	}
	public final boolean canBreak(final Player player, final Location location) {
		Boolean result = true;
		try {
			final TownBlock block = api.getTownBlock(location);
			if(block != null && !block.getTown().getMayor().getName().equals(player.getName())) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.DESTROY);
			ConsoleModule.debug(getClass(), "#canBreak: " + player.getName() + " " + result);
		} catch (final NotRegisteredException e) {
			ConsoleModule.debug(getClass(), "Unable to check for plot permissions.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return result;
	}
	public final boolean canInteract(final Player player, final Location location) {
		Boolean result = true;
		try {
			final TownBlock block = api.getTownBlock(location);
			if(block != null && !block.getTown().getMayor().getName().equals(player.getName())) result = PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.ITEM_USE);
			ConsoleModule.debug(getClass(), "#canInteract: " + player.getName() + " " + result);
		} catch (final NotRegisteredException e) {
			ConsoleModule.debug(getClass(), "Unable to check for plot permissions.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return result;
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlotUnclaim(final TownUnclaimEvent event) {
		final BoundingBox chunk = event.getWorldCoord().getBoundingBox();
		for(double x = chunk.getMinX(); x < chunk.getMaxX(); x++) for(double z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) for(double y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
			final Block block = new Location(event.getWorldCoord().getBukkitWorld(), x, y, z).getBlock();
			if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
				Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.TOWNYADVANCED, event));
				block.setType(Material.AIR);
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onTownRuined(final TownRuinedEvent event) {
		for(final TownBlock townBlock : event.getTown().getTownBlocks()) {
			final BoundingBox chunk = townBlock.getWorldCoord().getBoundingBox();
			for(double x = chunk.getMinX(); x < chunk.getMaxX(); x++) for(double z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) for(double y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
				final Block block = new Location(townBlock.getWorldCoord().getBukkitWorld(), x, y, z).getBlock();
				if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
					Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.TOWNYADVANCED, event));
					block.setType(Material.AIR);
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onTownDeleted(final PreDeleteTownEvent event) {
		for(final TownBlock townBlock : event.getTown().getTownBlocks()) {
			final BoundingBox chunk = townBlock.getWorldCoord().getBoundingBox();
			for(double x = chunk.getMinX(); x < chunk.getMaxX(); x++) for(double z = chunk.getMinZ(); z < chunk.getMaxZ(); z++) for(double y = chunk.getMinY(); y < chunk.getMaxY(); y++) {
				final Block block = new Location(townBlock.getWorldCoord().getBukkitWorld(), x, y, z).getBlock();
				if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
					Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.TOWNYADVANCED, event));
					block.setType(Material.AIR);
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onNewDay(final NewDayEvent event) {
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ECONOMY_ENABLE) && (Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_ENABLE)) {
			for(final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
				Integer tax = 0;
				for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks(player.getUniqueId())) {
					if(!block.getServer().equals((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID))) continue;
					try {
						boolean remove = false;
						final TownBlock townBlock = api.getTownBlock(block.getLocation());
						if(townBlock != null && !townBlock.getTown().getMayor().getName().equals(player.getName()) && EconomyModule.getInstance().has(player, (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST))) {
							tax = tax + (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST);
							api.getTown(block.getLocation()).depositToBank(api.getResident(player.getUniqueId()), (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST));
						} else remove = true;
						if(remove) {
							block.remove(false);
							if(player.getPlayer() != null) {
								final String world = block.getLocation().getWorld().getName();
								final Integer x = block.getLocation().getBlockX();
								final Integer y = block.getLocation().getBlockY();
								final Integer z = block.getLocation().getBlockZ();
								player.getPlayer().sendMessage("Uma caixa de corrêio foi removida por falta de pagamento de taxa adicional! Mundo: " + world + ", X: " + x + ", Y: " + y + ", Z: " + z);
							}
						}
					} catch (final NullPointerException | TownyException e) {
						ConsoleModule.debug(getClass(), "Unable to tax residents.");
						if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
					}
				}
				if(tax > 0 && EconomyModule.getInstance().withdraw(player, (Integer) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_TOWNYADVANCED_TAXES_COST))) if(player.getPlayer() != null) player.getPlayer().sendMessage("Você pagou $ " + tax + " de taxa adicional para ter caixas de corrêio em seus terrenos!");
			}
		}
	}
}