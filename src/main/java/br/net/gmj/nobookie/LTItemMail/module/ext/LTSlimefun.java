package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AsyncAutoEnchanterProcessEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AutoDisenchantEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AutoEnchantEvent;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import io.github.thebusybiscuit.slimefun4.api.events.MultiBlockCraftEvent;

public final class LTSlimefun implements LTExtension, Listener {
	public LTSlimefun() {
		Bukkit.getPluginManager().registerEvents(this, LTItemMail.getInstance());
	}
	@Override
	public final void unload() {
		HandlerList.unregisterAll(this);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void blockPlacer(final BlockPlacerPlaceEvent e) {
		final ItemStack mailbox = e.getItemStack();
		if(mailbox != null && BukkitUtil.DataContainer.Mailbox.isMailbox(mailbox)) e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void autoDisenchanter(final AutoDisenchantEvent e) {
		final ItemStack mailbox = e.getItem();
		if(mailbox != null && BukkitUtil.DataContainer.Mailbox.isMailbox(mailbox)) e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void autoEnchanter(final AutoEnchantEvent e) {
		final ItemStack mailbox = e.getItem();
		if(mailbox != null && BukkitUtil.DataContainer.Mailbox.isMailbox(mailbox)) e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void autoEnchanter(final AsyncAutoEnchanterProcessEvent e) {
		final ItemStack mailbox = e.getItem();
		if(mailbox != null && BukkitUtil.DataContainer.Mailbox.isMailbox(mailbox)) e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void multiBlock(final MultiBlockCraftEvent e) {
		for(final ItemStack mailbox : e.getInput()) if(mailbox != null && BukkitUtil.DataContainer.Mailbox.isMailbox(mailbox)) {
			e.setCancelled(true);
			break;
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void android(final AndroidMineEvent e) {
		final Block mailbox = e.getBlock();
		if(mailbox != null && DatabaseModule.Block.isMailboxBlock(mailbox.getLocation())) e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void android(final AndroidFarmEvent e) {
		final Block mailbox = e.getBlock();
		if(mailbox != null && DatabaseModule.Block.isMailboxBlock(mailbox.getLocation())) e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void explosiveTool(final ExplosiveToolBreakBlocksEvent e) {
		final Block mailbox = e.getPrimaryBlock();
		if(mailbox != null && DatabaseModule.Block.isMailboxBlock(mailbox.getLocation())) {
			e.setCancelled(true);
		} else for(final Block mailboxes : e.getAdditionalBlocks()) if(mailboxes != null && DatabaseModule.Block.isMailboxBlock(mailboxes.getLocation())) {
			e.setCancelled(true);
			break;
		}
	}
}