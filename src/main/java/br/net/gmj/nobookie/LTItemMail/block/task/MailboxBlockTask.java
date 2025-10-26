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
package br.net.gmj.nobookie.LTItemMail.block.task;

import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTBlueMap;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTDecentHolograms;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTDynmap;

public final class MailboxBlockTask implements Runnable {
	private final LTBlueMap blueMap = (LTBlueMap) ExtensionModule.getInstance().get(ExtensionModule.EXT.BLUEMAP);
	private final LTDecentHolograms decentHolograms = (LTDecentHolograms) ExtensionModule.getInstance().get(ExtensionModule.EXT.DECENTHOLOGRAMS);
	private final LTDynmap dynmap = (LTDynmap) ExtensionModule.getInstance().get(ExtensionModule.EXT.DYNMAP);
	public final void run() {
		for(final MailboxBlock block : DatabaseModule.Block.getMailboxBlocks()) {
			if(!block.getServer().equals((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_MULTI_SERVER_SUPPORT_SERVER_ID))) continue;
			if(!block.getBukkitBlock().getType().toString().endsWith("_SHULKER_BOX")) {
				block.remove(true);
				if(blueMap != null) blueMap.deleteMarker(block.getOwner().getBukkitPlayer(), block.getLocation(), false);
				if(decentHolograms != null) decentHolograms.deleteHolo(block.getOwner().getBukkitPlayer(), block.getLocation());
				if(dynmap != null) dynmap.deleteMarker(block.getOwner().getBukkitPlayer(), block.getLocation());
			}
		}
	}
}