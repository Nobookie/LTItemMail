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

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;

public final class LTDynmap extends DynmapCommonAPIListener implements LTExtension {
	private MarkerAPI api = null;
	public LTDynmap(){
		DynmapCommonAPIListener.register(this);
	}
	@Override
	public final void apiEnabled(final DynmapCommonAPI api) {
		if(api != null) {
			this.api = api.getMarkerAPI();
			for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks()) createMarker(block.getOwner().getBukkitPlayer(), block.getLocation());
		}
	}
	@Override
	public final void unload() {
		for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks()) deleteMarker(block.getOwner().getBukkitPlayer(), block.getLocation());
		DynmapCommonAPIListener.unregister(this);
	}
	public final void createMarker(final OfflinePlayer player, final Location location) {
		if(api != null) {
			final MarkerSet set = getSet();
			final String world = location.getWorld().getName();
			final Integer x = location.getBlockX();
			final Integer y = location.getBlockY();
			final Integer z = location.getBlockZ();
			final String id = player.getName() + "_" + world + "_" + x + "_" + y + "_" + z;
			deleteMarker(player, location);
			set.createMarker(id, LanguageModule.get(LanguageModule.Type.BLOCK_NAME) + " | " + LanguageModule.get(LanguageModule.Type.BLOCK_OWNER) + " " + player.getName() + " (" + x + ", " + y + ", " + z + ")", false, world, x, y, z, getIcon(), false);
			ConsoleModule.debug(getClass(), "#createMarker: " + id);
		}
	}
	public final void deleteMarker(final OfflinePlayer player, final Location location) {
		if(api != null) {
			final MarkerSet set = getSet();
			final Marker marker = set.findMarker(player.getName() + "_" + location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ());
			if(marker != null) {
				final String id = marker.getMarkerID();
				marker.deleteMarker();
				ConsoleModule.debug(getClass(), "#deleteMarker: " + id);
			}
		}
	}
	private final MarkerSet getSet() {
		MarkerSet set = api.getMarkerSet("ltitemmail_markers");
		if(set == null) set = api.createMarkerSet("ltitemmail_markers", LanguageModule.get(LanguageModule.Type.BLOCK_NAME), null, false);
		set.setDefaultMarkerIcon(getIcon());
		return set;
	}
	private final MarkerIcon getIcon() {
		MarkerIcon icon = api.getMarkerIcon("ltitemmail_mailbox");
		if(icon == null) icon = api.createMarkerIcon("ltitemmail_mailbox", LanguageModule.get(LanguageModule.Type.BLOCK_NAME), LTItemMail.getInstance().getResource("mailbox_icon.png"));
		return icon;
	}
}