package br.net.gmj.nobookie.LTItemMail.block.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.event.BreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.event.PlayerBreakMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.event.PlayerPlaceMailboxBlockEvent;
import br.net.gmj.nobookie.LTItemMail.inventory.MailboxInventory;
import br.net.gmj.nobookie.LTItemMail.item.Item;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule.EXT;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTBlueMap;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTCitizens;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTDecentHolograms;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTDynmap;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTGriefPrevention;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTRedProtect;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTTownyAdvanced;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTWorldGuard;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;

public final class MailboxBlockListener implements Listener {
	private final Item mailbox = new MailboxItem();
	private final LTDynmap dynmap = (LTDynmap) ExtensionModule.getInstance().get(ExtensionModule.EXT.DYNMAP);
	private final LTBlueMap blueMap = (LTBlueMap) ExtensionModule.getInstance().get(ExtensionModule.EXT.BLUEMAP);
	private final LTDecentHolograms decentHolograms = (LTDecentHolograms) ExtensionModule.getInstance().get(ExtensionModule.EXT.DECENTHOLOGRAMS);
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onClick(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if(event.hasBlock() && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getHand().equals(EquipmentSlot.HAND)) {
			final Block block = event.getClickedBlock();
			if(block.getType().toString().endsWith("_SHULKER_BOX")) if(DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
				event.setCancelled(true);
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				if(player.isSneaking()) return;
				if(canInteract(player, block.getLocation())) {
					if(PermissionModule.hasPermission(player, PermissionModule.Type.BLOCK_PLAYER_USE)) {
						final LTPlayer owner = LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation()));
						if(owner.getUniqueId().equals(player.getUniqueId()) && !PermissionModule.hasPermission(player, PermissionModule.Type.CMD_ADMIN_BYPASS)) {
							if(DatabaseModule.Virtual.getMailboxesList(player.getUniqueId(), DatabaseModule.Virtual.Status.PENDING).size() > 0) {
								player.performCommand("ltitemmail:itemmail");
							} else player.performCommand("ltitemmail:itemmail list");
						} else {
							if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.CITIZENS)) ((LTCitizens) ExtensionModule.getInstance().get(EXT.CITIZENS)).call(player);
							player.openInventory(MailboxInventory.getInventory(MailboxInventory.Type.OUT, null, owner, null, player.getUniqueId(), "", false));
						}
					} else player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_USEERROR));
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPlace(final BlockPlaceEvent event) {
		final ItemStack item = event.getItemInHand();
		if(item != null && item.getItemMeta() != null && item.getType().toString().endsWith("_SHULKER_BOX") && BukkitUtil.DataContainer.Mailbox.isMailbox(item)) {
			final Player player = event.getPlayer();
			final Block block = event.getBlockPlaced();
			if(canBuildBreak(player, block.getLocation()) && canBuild(player, block.getLocation())) {
				if(PermissionModule.hasPermission(player, PermissionModule.Type.BLOCK_PLAYER_PLACE)) {
					final Block blockBelow = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() - 1), block.getLocation().getBlockZ()).getBlock();
					if(blockBelow.getType().toString().endsWith("_FENCE") || blockBelow.getType().toString().endsWith("_WALL")) {
						if(!DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
							final PlayerPlaceMailboxBlockEvent placeEvent = new PlayerPlaceMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(player.getUniqueId()), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), PlayerPlaceMailboxBlockEvent.Reason.BY_PLAYER, LTPlayer.fromUUID(player.getUniqueId()));
							Bukkit.getPluginManager().callEvent(placeEvent);
							if(!placeEvent.isCancelled()) {
								if(DatabaseModule.Block.placeMailbox(player.getUniqueId(), block.getLocation())) {
									if(dynmap != null) dynmap.createMarker(player, block.getLocation());
									if(blueMap != null) blueMap.createMarker(player, block.getLocation());
									if(decentHolograms != null) decentHolograms.createHolo(player, block.getLocation());
									MailboxModule.log(LTPlayer.fromUUID(player.getUniqueId()), null, MailboxModule.Action.PLACED, null, null, null, block.getLocation());
								} else event.setCancelled(true);
							} else event.setCancelled(true);
						} else event.setCancelled(true);
					} else {
						event.setCancelled(true);
						player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_BELOWERROR));
					}
				} else {
					event.setCancelled(true);
					player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_PLACEERROR));
				}
			} else event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		if(block != null && block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
			final ItemStack newMailbox = mailbox.getItem(null);
			newMailbox.setType(block.getType());
			final Player player = event.getPlayer();
			if(canBuildBreak(player, block.getLocation()) && canBreak(player, block.getLocation())) {
				if(PermissionModule.hasPermission(player, PermissionModule.Type.BLOCK_PLAYER_BREAK)) {
					if(DatabaseModule.Block.isMailboxOwner(player.getUniqueId(), block.getLocation())) {
						final PlayerBreakMailboxBlockEvent breakEvent = new PlayerBreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), LTPlayer.fromUUID(player.getUniqueId()), (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.BY_PLAYER_OWNER, LTPlayer.fromUUID(player.getUniqueId()));
						Bukkit.getPluginManager().callEvent(breakEvent);
						if(!breakEvent.isCancelled()) {
							if(DatabaseModule.Block.breakMailbox(block.getLocation())) {
								event.setDropItems(false);
								block.getWorld().dropItem(block.getLocation(), newMailbox);
								if(dynmap != null) dynmap.deleteMarker(player, block.getLocation());
								if(blueMap != null) blueMap.deleteMarker(player, block.getLocation(), false);
								if(decentHolograms != null) decentHolograms.deleteHolo(player, block.getLocation());
								MailboxModule.log(LTPlayer.fromUUID(player.getUniqueId()), null, MailboxModule.Action.BROKE, null, null, null, block.getLocation());
							} else event.setCancelled(true);
						} else event.setCancelled(true);
					} else if(PermissionModule.hasPermission(player, PermissionModule.Type.BLOCK_ADMIN_BREAK)){
						final LTPlayer owner = LTPlayer.fromUUID(DatabaseModule.Block.getMailboxOwner(block.getLocation()));
						final PlayerBreakMailboxBlockEvent breakEvent = new PlayerBreakMailboxBlockEvent(new MailboxBlock(DatabaseModule.Block.getMailboxID(block.getLocation()), owner, (String) ConfigurationModule.get(ConfigurationModule.Type.BUNGEE_SERVER_ID), block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ()), BreakMailboxBlockEvent.Reason.BY_PLAYER_ADMIN, LTPlayer.fromUUID(player.getUniqueId()));
						Bukkit.getPluginManager().callEvent(breakEvent);
						if(!breakEvent.isCancelled()) {
							if(DatabaseModule.Block.breakMailbox(block.getLocation())) {
								event.setDropItems(false);
								block.getWorld().dropItem(block.getLocation(), newMailbox);
								if(dynmap != null) dynmap.deleteMarker(owner.getBukkitPlayer(), block.getLocation());
								if(blueMap != null) blueMap.deleteMarker(owner.getBukkitPlayer(), block.getLocation(), false);
								if(decentHolograms != null) decentHolograms.deleteHolo(owner.getBukkitPlayer(), block.getLocation());
								MailboxModule.log(LTPlayer.fromUUID(player.getUniqueId()), owner, MailboxModule.Action.ADMIN_BROKE, null, null, null, block.getLocation());
								player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_ADMINBROKE) + " " + owner.getName());
							} else event.setCancelled(true);
						} else event.setCancelled(true);
					} else {
						event.setCancelled(true);
						player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_OWNERERROR));
					}
				} else {
					event.setCancelled(true);
					player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + LanguageModule.get(LanguageModule.Type.BLOCK_BREAKERROR));
				}
			} else event.setCancelled(true);
		} else if(block != null && (block.getType().toString().endsWith("_FENCE") || block.getType().toString().endsWith("_WALL"))) {
			final Block blockAbove = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() + 1), block.getLocation().getBlockZ()).getBlock();
			if(blockAbove.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(blockAbove.getLocation())) event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onInventoryMove(final InventoryMoveItemEvent event) {
		final Location block = event.getDestination().getLocation();
		if(block != null && block.getBlock().getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block)) if(event.getSource().getType().equals(InventoryType.HOPPER)) event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPistonExtend(final BlockPistonExtendEvent event) {
		Boolean cancel = false;
		final List<Block> blocks = event.getBlocks();
		for(final Block block : blocks) {
			if(block != null) {
				if(block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) cancel = true;
				if(block.getType().toString().endsWith("_FENCE") || block.getType().toString().endsWith("_WALL")) {
					final Block blockAbove = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() + 1), block.getLocation().getBlockZ()).getBlock();
					if(blockAbove.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(blockAbove.getLocation())) cancel = true;
				}
			}
		}
		event.setCancelled(cancel);
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onPistonRetract(final BlockPistonRetractEvent event) {
		Boolean cancel = false;
		final List<Block> blocks = event.getBlocks();
		for(final Block block : blocks) {
			if(block != null) {
				if(block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) cancel = true;
				if(block.getType().toString().endsWith("_FENCE") || block.getType().toString().endsWith("_WALL")) {
					final Block blockAbove = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() + 1), block.getLocation().getBlockZ()).getBlock();
					if(blockAbove.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(blockAbove.getLocation())) cancel = true;
				}
			}
		}
		event.setCancelled(cancel);
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onBlockExplode(final BlockExplodeEvent event) {
		Boolean cancel = false;
		final List<Block> blocks = event.blockList();
		for(final Block block : blocks) {
			if(block != null) {
				if(block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
					blocks.remove(block);
					cancel = true;
				}
				if(block.getType().toString().endsWith("_FENCE") || block.getType().toString().endsWith("_WALL")) {
					final Block blockAbove = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() + 1), block.getLocation().getBlockZ()).getBlock();
					if(blockAbove.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(blockAbove.getLocation())) {
						blocks.remove(block);
						blocks.remove(blockAbove);
						cancel = true;
					}
				}
			}
		}
		if(cancel) {
			event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
			for(final Block block : blocks) {
				block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, block.getLocation(), 1);
				block.breakNaturally();
			}
		}
		event.setCancelled(cancel);
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onEntityExplode(final EntityExplodeEvent event) {
		Boolean cancel = false;
		final List<Block> blocks = event.blockList();
		for(final Block block : blocks) {
			if(block != null) {
				if(block.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(block.getLocation())) {
					blocks.remove(block);
					cancel = true;
				}
				if(block.getType().toString().endsWith("_FENCE") || block.getType().toString().endsWith("_WALL")) {
					final Block blockAbove = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), (block.getLocation().getBlockY() + 1), block.getLocation().getBlockZ()).getBlock();
					if(blockAbove.getType().toString().endsWith("_SHULKER_BOX") && DatabaseModule.Block.isMailboxBlock(blockAbove.getLocation())) {
						blocks.remove(block);
						blocks.remove(blockAbove);
						cancel = true;
					}
				}
			}
		}
		if(cancel) {
			event.getLocation().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
			for(final Block block : blocks) {
				block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE, block.getLocation(), 1);
				block.breakNaturally();
			}
		}
		event.setCancelled(cancel);
	}
	private final LTGriefPrevention griefPrevention = (LTGriefPrevention) ExtensionModule.getInstance().get(ExtensionModule.EXT.GRIEFPREVENTION);
	private final LTRedProtect redProtect = (LTRedProtect) ExtensionModule.getInstance().get(ExtensionModule.EXT.REDPROTECT);
	private final LTTownyAdvanced townyAdvanced = (LTTownyAdvanced) ExtensionModule.getInstance().get(ExtensionModule.EXT.TOWNYADVANCED);
	private final LTWorldGuard worldGuard = (LTWorldGuard) ExtensionModule.getInstance().get(ExtensionModule.EXT.WORLDGUARD);
	private final boolean canBuildBreak(final Player player, final Location location) {
		Boolean canBuildBreak = true;
		if(canBuildBreak && griefPrevention != null) canBuildBreak = griefPrevention.canBuildBreak(player, location);
		if(canBuildBreak && redProtect != null) canBuildBreak = redProtect.canBuildBreak(player, location);
		return canBuildBreak;
	}
	private final boolean canBuild(final Player player, final Location location) {
		Boolean canBuild = true;
		if(canBuild && worldGuard != null) canBuild = worldGuard.canBuild(player, location);
		if(canBuild && townyAdvanced != null) canBuild = townyAdvanced.canBuild(player, location);
		return canBuild;
	}
	private final boolean canBreak(final Player player, final Location location) {
		Boolean canBreak = true;
		if(canBreak && worldGuard != null) canBreak = worldGuard.canBreak(player, location);
		if(canBreak && townyAdvanced != null) canBreak = townyAdvanced.canBreak(player, location);
		return canBreak;
	}
	private final boolean canInteract(final Player player, final Location location) {
		Boolean canInteract = true;
		if(canInteract && worldGuard != null) canInteract = worldGuard.canInteract(player, location);
		if(canInteract && townyAdvanced != null) canInteract = townyAdvanced.canInteract(player, location);
		if(canInteract && griefPrevention != null) canInteract = griefPrevention.canInteract(player, location);
		if(canInteract && redProtect != null) canInteract = redProtect.canInteract(player, location);
		return canInteract;
	}
}