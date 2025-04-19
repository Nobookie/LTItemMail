package br.net.gmj.nobookie.LTItemMail.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.event.PlayerSendMailEvent;
import br.net.gmj.nobookie.LTItemMail.inventory.MailboxInventory;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DatabaseModule;
import br.net.gmj.nobookie.LTItemMail.module.EconomyModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule;
import br.net.gmj.nobookie.LTItemMail.module.ExtensionModule.EXT;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.module.MailboxModule;
import br.net.gmj.nobookie.LTItemMail.module.PermissionModule;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTCitizens;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTHeadDatabase;
import br.net.gmj.nobookie.LTItemMail.module.ext.LTSkulls;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;

public final class MailboxVirtualListener implements Listener {
	public MailboxVirtualListener() {
		Bukkit.getServer().getPluginManager().registerEvents(this, LTItemMail.getInstance());
	}
	private final LTCitizens citizens = (LTCitizens) ExtensionModule.getInstance().get(EXT.CITIZENS);
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onInventoryClose(final InventoryCloseEvent event) {
		final Player player = (Player) event.getPlayer();
		final Inventory inventory = event.getInventory();
		final InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN, null, null)) && inventoryView.getTitle().split("#").length == 2) {
			final Integer mailboxID = Integer.valueOf(inventoryView.getTitle().split("#")[1]);
			final ItemStack[] contents = inventory.getContents();
			final Boolean isEmpty = BukkitUtil.Inventory.isEmpty(contents);
			if(!isEmpty) {
				final LinkedList<ItemStack> items = BukkitUtil.Inventory.getContents(contents);
				DatabaseModule.Virtual.updateMailbox(mailboxID, items);
			} else DatabaseModule.Virtual.updateMailbox(mailboxID, new LinkedList<ItemStack>());
			player.sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_CLOSED));
			inventory.clear();
			if(citizens != null) citizens.dismiss(player.getName());
			BukkitUtil.AutoRun.execute(getClass(), ConfigurationModule.Type.AUTORUN_MAIL_CLAIMED_ONCLOSE, player);
		}
		if(inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.OUT, null, null)) && inventoryView.getTitle().split("@").length == 2) {
			final LTPlayer sender = LTPlayer.fromUUID(player.getUniqueId());
			final LTPlayer receiver = LTPlayer.fromName(inventoryView.getTitle().split("@")[1]);
			final ItemStack[] contents = inventory.getContents();
			final boolean isEmpty = BukkitUtil.Inventory.isEmpty(contents);
			if(!isEmpty) {
				final LinkedList<ItemStack> items = BukkitUtil.Inventory.getContents(contents);
				final int count = BukkitUtil.Inventory.getCount(items);
				inventory.clear();
				final String label = contents[30].getItemMeta().getLore().get(1).replace(ChatColor.COLOR_CHAR + "e", "");
				double newcost = 0.0;
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_TYPE_COST)) {
					newcost = (Double) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_COST) * count;
				} else newcost = (Double) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_COST);
				if(EconomyModule.getInstance() != null) {
					if(EconomyModule.getInstance().has(sender.getBukkitPlayer().getPlayer(), newcost)) {
						if(EconomyModule.getInstance().withdraw(player, newcost)) {
							MailboxModule.log(sender, null, MailboxModule.Action.PAID, null, newcost, null, null);
							final PlayerSendMailEvent sendEvent = new PlayerSendMailEvent(sender, receiver, items, true, newcost, label);
							Bukkit.getPluginManager().callEvent(sendEvent);
							if(!sendEvent.isCancelled()) {
								sender.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ((String) LanguageModule.get(LanguageModule.Type.TRANSACTION_PAID)).replaceAll("%money%", "" + ChatColor.GREEN + newcost + ChatColor.YELLOW));
								MailboxModule.send(sender.getBukkitPlayer().getPlayer(), receiver, items, label);
							} else {
								EconomyModule.getInstance().deposit(sender.getBukkitPlayer().getPlayer(), newcost);
								MailboxModule.log(sender, null, MailboxModule.Action.REFUNDED, null, newcost, null, null);
								sender.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_BLOCKED));
								MailboxModule.log(sender, null, MailboxModule.Action.CANCELED, null, null, null, null);
								MailboxModule.send(Bukkit.getConsoleSender(), LTPlayer.fromUUID(sender.getUniqueId()), items, sendEvent.getCancelReason());
							}
						} else {
							sender.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.TRANSACTION_ERROR));
							MailboxModule.log(sender, null, MailboxModule.Action.CANCELED, null, null, null, null);
							MailboxModule.send(Bukkit.getConsoleSender(), LTPlayer.fromUUID(sender.getUniqueId()), items, label);
						}
					} else {
						sender.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + ((String) LanguageModule.get(LanguageModule.Type.TRANSACTION_NOMONEY)).replaceAll("%money%", "" + ChatColor.GREEN + newcost + ChatColor.YELLOW));
						MailboxModule.log(sender, null, MailboxModule.Action.CANCELED, null, null, null, null);
						MailboxModule.send(Bukkit.getConsoleSender(), LTPlayer.fromUUID(sender.getUniqueId()), items, label);
					}
				} else {
					final PlayerSendMailEvent sendEvent = new PlayerSendMailEvent(sender, receiver, items, false, 0.0, label);
					Bukkit.getPluginManager().callEvent(sendEvent);
					if(!sendEvent.isCancelled()) {
						MailboxModule.send(sender.getBukkitPlayer().getPlayer(), receiver, items, label);
					} else {
						sender.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_BLOCKED));
						MailboxModule.log(sender, null, MailboxModule.Action.CANCELED, null, null, null, null);
						MailboxModule.send(Bukkit.getConsoleSender(), LTPlayer.fromUUID(sender.getUniqueId()), items, sendEvent.getCancelReason());
					}
				}
			} else sender.getBukkitPlayer().getPlayer().sendMessage((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_TAG) + " " + ChatColor.YELLOW + "" + LanguageModule.get(LanguageModule.Type.MAILBOX_ABORTED));
			if(citizens != null) citizens.dismiss(player.getName());
			BukkitUtil.AutoRun.execute(getClass(), ConfigurationModule.Type.AUTORUN_MAIL_NEW_ONCLOSE, player);
		}
		if(inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN_PENDING, null, null)) && inventoryView.getTitle().split("!").length == 2) {
			inventory.clear();
			if(citizens != null) citizens.dismiss(player.getName());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onItemDrop(final PlayerDropItemEvent event) {
		final LTPlayer player = LTPlayer.fromUUID(event.getPlayer().getUniqueId());
		final ItemStack ee = event.getItemDrop().getItemStack();
		if(PermissionModule.hasPermission(player.getBukkitPlayer().getPlayer(), PermissionModule.Type.CMD_ADMIN_MAIN) && ee.getType().equals(Material.PAPER) && ee.getItemMeta() != null && ee.getItemMeta().getDisplayName().equalsIgnoreCase("give me a mailbox")) {
			int i = 0;
			for(i = 0; i < ee.getAmount(); i++) if(!player.giveMailboxBlock()) break;
			ee.setAmount(ee.getAmount() - i);
			ConsoleModule.info(ChatColor.RESET + "" + ChatColor.ITALIC + "Gave " + i + " x " + new MailboxItem().getName() + ChatColor.RESET + ChatColor.ITALIC + " to " + player.getName());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onInventoryDrag(final InventoryDragEvent event) {
		final Player player = (Player) event.getWhoClicked();
		final InventoryView inventoryView = player.getOpenInventory();
		if((inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN, null, null)) && inventoryView.getTitle().split("#").length == 2) || (inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN_PENDING, null, null)) && inventoryView.getTitle().split("!").length == 2)) {
			final Inventory top = inventoryView.getTopInventory();
			final Inventory dragged = event.getInventory();
			if(top == dragged) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public final void onInventoryClick(final InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		final InventoryView inventoryView = player.getOpenInventory();
		if(inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN, null, null)) && inventoryView.getTitle().split("#").length == 2) {
			final Inventory top = inventoryView.getTopInventory();
			final Inventory bottom = inventoryView.getBottomInventory();
			final Inventory clicked = event.getClickedInventory();
			final InventoryAction action = event.getAction();
			if(clicked == top && (action == InventoryAction.PLACE_SOME || action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE || action == InventoryAction.SWAP_WITH_CURSOR || action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD)) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
			} else if(clicked == bottom && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
			}
		}
		if(inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN_PENDING, null, null)) && inventoryView.getTitle().split("!").length == 2) {
			final InventoryAction action = event.getAction();
			if(action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_HALF || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME || action == InventoryAction.PLACE_SOME || action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE || action == InventoryAction.SWAP_WITH_CURSOR || action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
				event.setResult(Result.DENY);
				event.setCancelled(true);
			}
		}
		if((inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN_PENDING, null, null)) && inventoryView.getTitle().split("!").length == 2) || (inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.IN, null, null)) && inventoryView.getTitle().split("#").length == 2) || (inventoryView.getTitle().contains(MailboxInventory.getName(MailboxInventory.Type.OUT, null, null)) && inventoryView.getTitle().split("@").length == 2)) if(event.getRawSlot() > 26 && event.getRawSlot() < 36 && event.getCurrentItem() != null) {
			final ItemStack selected = event.getCurrentItem();
			event.setResult(Result.DENY);
			event.setCancelled(true);
			final Inventory inventory = event.getClickedInventory();
			final ItemStack[] contents = inventory.getContents();
			LTHeadDatabase headDB = null;
			LTSkulls skulls = null;
			if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.HEADDATABASE)) headDB = (LTHeadDatabase) ExtensionModule.getInstance().get(ExtensionModule.EXT.HEADDATABASE);
			if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.SKULLS)) skulls = (LTSkulls) ExtensionModule.getInstance().get(ExtensionModule.EXT.SKULLS);
			Boolean primeCost = false;
			if(headDB != null) {
				if(selected.getItemMeta() instanceof SkullMeta) if(headDB.getId(selected).equals(LTHeadDatabase.Type.MAILBOX_BUTTON_COST.id())) primeCost = true;
			} if(skulls != null) {
				if(selected.getItemMeta() instanceof SkullMeta) if(skulls.getId(selected).equals(LTSkulls.Type.MAILBOX_BUTTON_COST.id())) primeCost = true;
			} else if(selected.getType().equals(Material.EMERALD)) primeCost = true;
			if(primeCost) {
				final boolean isEmpty = BukkitUtil.Inventory.isEmpty(contents);
				double newcost = 0.0;
				if(!isEmpty) {
					final int count = BukkitUtil.Inventory.getCount(BukkitUtil.Inventory.getContents(contents));
					if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_TYPE_COST)) {
						newcost = (Double) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_COST) * count;
					} else newcost = (Double) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_COST);
				}
				final ItemMeta selectedMeta = selected.getItemMeta();
				final List<String> selectedLore = selectedMeta.getLore();
				if(EconomyModule.getInstance() != null) {
					selectedLore.set(0, ChatColor.RESET + "" + ChatColor.GREEN + "$ " + newcost);
				} else selectedLore.set(0, ChatColor.RESET + "" + ChatColor.DARK_RED + LanguageModule.get(LanguageModule.Type.MAILBOX_COSTERROR));
				selectedMeta.setLore(selectedLore);
				selected.setItemMeta(selectedMeta);
				inventory.setItem(event.getRawSlot(), selected);
			}
			final String[] elements = inventoryView.getTitle().split("!");
			if(elements.length == 2) {
				final Integer mailboxID = Integer.parseInt(elements[1]);
				Boolean primeDeny = false;
				Boolean primeAccept = false;
				if(headDB != null) {
					if(selected.getItemMeta() instanceof SkullMeta) {
						if(headDB.getId(selected).equals(LTHeadDatabase.Type.MAILBOX_BUTTON_DENY.id())) primeDeny = true;
						if(headDB.getId(selected).equals(LTHeadDatabase.Type.MAILBOX_BUTTON_ACCEPT.id())) primeAccept = true;
					}
				} else if(skulls != null) {
					if(selected.getItemMeta() instanceof SkullMeta) {
						if(skulls.getId(selected).equals(LTSkulls.Type.MAILBOX_BUTTON_DENY.id())) primeDeny = true;
						if(skulls.getId(selected).equals(LTSkulls.Type.MAILBOX_BUTTON_ACCEPT.id())) primeAccept = true;
					}
				} else {
					if(selected.getType().equals(Material.BARRIER)) primeDeny = true;
					if(selected.getType().equals(Material.ENDER_EYE)) primeAccept = true;
				}
				if(primeDeny) {
					BukkitUtil.AutoRun.execute(getClass(), ConfigurationModule.Type.AUTORUN_MAIL_PENDING_WHENDENIED, player);
					DatabaseModule.Virtual.setStatus(mailboxID, DatabaseModule.Virtual.Status.DENIED);
					final UUID from = DatabaseModule.Virtual.getMailboxFrom(mailboxID);
					if(from != null) {
						MailboxModule.log(LTPlayer.fromUUID(player.getUniqueId()), LTPlayer.fromUUID(from), MailboxModule.Action.GAVE_BACK, mailboxID, null, null, null);
						final Integer backMailboxID = MailboxModule.send(Bukkit.getConsoleSender(), LTPlayer.fromUUID(from), BukkitUtil.Inventory.getContents(contents), player.getName() + " " + LanguageModule.get(LanguageModule.Type.MAILBOX_RETURNED));
						DatabaseModule.Virtual.setStatus(backMailboxID, DatabaseModule.Virtual.Status.ACCEPTED);
					}
					DatabaseModule.Virtual.setMailboxDeleted(mailboxID);
					inventoryView.close();
				}
				if(primeAccept) {
					BukkitUtil.AutoRun.execute(getClass(), ConfigurationModule.Type.AUTORUN_MAIL_PENDING_WHENACCEPTED, player);
					DatabaseModule.Virtual.setStatus(mailboxID, DatabaseModule.Virtual.Status.ACCEPTED);
					event.getInventory().clear();
					inventoryView.close();
					player.openInventory(MailboxInventory.getInventory(MailboxInventory.Type.IN, mailboxID, null, DatabaseModule.Virtual.getMailbox(mailboxID), DatabaseModule.Virtual.getMailboxFrom(mailboxID), DatabaseModule.Virtual.getMailboxLabel(mailboxID), false));
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public final void onSendMail(final PlayerSendMailEvent event) {
		final LTPlayer playerFrom = event.getFrom();
		final LTPlayer playerTo = event.getTo();
		if(!playerFrom.isRegistered()) DatabaseModule.User.register(playerFrom);
		if(!playerTo.isRegistered()) DatabaseModule.User.register(playerTo);
		if(playerFrom.isBanned()) {
			event.setCancelReason(LanguageModule.get(LanguageModule.Type.PLAYER_BANNED) + " (@" + playerFrom.getName() + " => @" + playerTo.getName() + ")");
			event.setCancelled(true);
		} else {
			Integer sent_count = playerFrom.getMailSentCount();
			DatabaseModule.User.setSentCount(playerFrom.getUniqueId(), sent_count++);
			Integer received_count = playerTo.getMailReceivedCount();
			DatabaseModule.User.setReceivedCount(playerTo.getUniqueId(), received_count++);
		}
	}
}