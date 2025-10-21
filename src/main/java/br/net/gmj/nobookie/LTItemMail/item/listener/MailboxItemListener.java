package br.net.gmj.nobookie.LTItemMail.item.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;

public final class MailboxItemListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockDispense(final BlockDispenseEvent e) {
		final ItemStack mailbox = e.getItem();
		if(mailbox != null && BukkitUtil.DataContainer.Mailbox.isMailbox(mailbox)) e.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onShulkerWash(final CauldronLevelChangeEvent e) {
		Player player = null;
		if(e.getEntity() != null && e.getEntity() instanceof Player) player = (Player) e.getEntity();
		if(player != null && e.getReason().equals(CauldronLevelChangeEvent.ChangeReason.SHULKER_WASH)) {
			ItemStack hand = null;
			if(!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
				hand = player.getInventory().getItemInMainHand();
			} else if(!player.getInventory().getItemInOffHand().getType().equals(Material.AIR)) hand = player.getInventory().getItemInOffHand();
			if(hand != null && BukkitUtil.DataContainer.Mailbox.isMailbox(hand)) e.setCancelled(true);
		}
	}
}