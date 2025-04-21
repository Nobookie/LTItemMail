package br.net.gmj.nobookie.LTItemMail.item.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;

public final class MailboxItemListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public final void onBlockDispense(final BlockDispenseEvent e) {
		final ItemStack mailbox = e.getItem();
		if(mailbox != null && BukkitUtil.DataContainer.Mailbox.isMailbox(mailbox)) e.setCancelled(true);
	}
}