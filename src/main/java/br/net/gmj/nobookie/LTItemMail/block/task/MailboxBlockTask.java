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
			if(!block.getServer().equals((String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID))) continue;
			if(!block.getBukkitBlock().getType().toString().endsWith("_SHULKER_BOX")) {
				block.remove(true);
				if(blueMap != null) blueMap.deleteMarker(block.getOwner().getBukkitPlayer(), block.getLocation(), false);
				if(decentHolograms != null) decentHolograms.deleteHolo(block.getOwner().getBukkitPlayer(), block.getLocation());
				if(dynmap != null) dynmap.deleteMarker(block.getOwner().getBukkitPlayer(), block.getLocation());
			}
		}
	}
}