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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.api.event.BreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;

public final class LTGriefPrevention implements LTExtension, Listener {
	public LTGriefPrevention() {
		Bukkit.getPluginManager().registerEvents(this, LTItemMail.getInstance());
	}
	@Override
	public final void unload() {
		HandlerList.unregisterAll(this);
	}
	public final boolean canBuildBreak(final Player player, final Location location) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, false, null);
		Boolean result = true;
		if(claim != null) result = claim.hasExplicitPermission(player, ClaimPermission.Build);
		ConsoleModule.debug(getClass(), "#canBuildBreak: " + player.getName() + " " + result);
		return result;
	}
	public final boolean canInteract(final Player player, final Location location) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, false, null);
		Boolean result = true;
		if(claim != null) result = claim.hasExplicitPermission(player, ClaimPermission.Inventory);
		ConsoleModule.debug(getClass(), "#canInteract: " + player.getName() + " " + result);
		return result;
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onClaimDelete(final ClaimDeletedEvent event) {
		for(final Chunk chunk : event.getClaim().getChunks()) for(int x = 0; x < 15; x++) for(int z = 0; z < 15; z++) for(int y = 0; y < 255; y++) {
			final Block block = chunk.getBlock(x, y, z);
			if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
				Bukkit.getPluginManager().callEvent(new BreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation())), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.ON_UNCLAIM, false, BreakMailboxBlockEvent.ClaimProvider.GRIEFPREVENTION, event));
				final ItemStack mailbox = new MailboxItem().getItem(null);
				mailbox.setType(block.getType());
				block.setType(Material.AIR);
				block.getWorld().dropItem(block.getLocation(), mailbox);
			}
		}
	}
}