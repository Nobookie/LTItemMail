/*
 * Copyright (C) 2026  Murilo Amaral Nappi
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;

public final class LTUltimateAdvancementAPI implements LTExtension {
	private final List<Material> shulkers = new ArrayList<>();
	private UltimateAdvancementAPI api = null;
	public LTUltimateAdvancementAPI() {
		for(final Material shulker : Material.values()) {
			final String name = shulker.toString();
			if(!name.startsWith("LEGACY_") && name.endsWith("_SHULKER_BOX")) shulkers.add(shulker);
		}
		Collections.shuffle(shulkers);
		api = UltimateAdvancementAPI.getInstance(LTItemMail.getInstance());
		final AdvancementTab tab = api.createAdvancementTab(LTItemMail.getInstance().getDescription().getName());
		final AdvancementDisplay display = new AdvancementDisplay(Material.WHITE_SHULKER_BOX, BukkitUtil.Text.parse((String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME)), AdvancementFrameType.TASK, false, false, 0, 0, "");
		final RootAdvancement root = new RootAdvancement(tab, "root", display, "textures/block/oxidized_copper.png");
		Advancements.setup(tab, root);
		tab.getEventManager().register(tab, PlayerLoadingCompletedEvent.class, EventPriority.HIGHEST, event -> {
			final Player player = event.getPlayer();
			tab.showTab(player);
			tab.grantRootAdvancement(player);
		});
	}
	public final void sendMessage(final LTPlayer ltPlayer, final String message) {
		if(ltPlayer.toBukkit().getPlayer() != null) api.displayCustomToast(ltPlayer.toBukkit().getPlayer(), new AdvancementDisplay(shulkers.get(new Random().nextInt(shulkers.size() - 1)), BukkitUtil.Text.parse((String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME)), AdvancementFrameType.TASK, true, false, 0, 0, message));
	}
	@Override
	public final void unload() {}
	public static final class Advancements {
		public static final class CraftFirstMailboxAdvancement extends BaseAdvancement {
			public CraftFirstMailboxAdvancement(final String key, final AdvancementDisplay display, final Advancement parent, final int maxProgression) {
				super(key, display, parent, maxProgression);
				this.registerEvent(CraftItemEvent.class, EventPriority.HIGHEST, event -> {
					final HumanEntity who = event.getWhoClicked();
					if(who instanceof Player) {
						final Player player = (Player) who;
						if(isVisible(player) && BukkitUtil.DataContainer.Mailbox.isMailbox(event.getCurrentItem())) incrementProgression(player);
					}
				});
			}
			@Override
			public final void giveReward(final Player player) {
				player.giveExp(35);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 1f);
			}
			public static final AdvancementDisplay display() {
				return new AdvancementDisplay(Material.SHULKER_BOX, "Craft your first mailbox.", AdvancementFrameType.GOAL, true, true, 0, 0, "");
			}
		}
		public static final class MailSentCountAdvancement extends BaseAdvancement {
			private final int exp;
			private final List<ItemStack> rewards;
			public MailSentCountAdvancement(final String key, final AdvancementDisplay display, final Advancement parent, final int maxProgression, final int exp, final List<ItemStack> rewards) {
				super(key, display, parent, maxProgression);
				this.exp = exp;
				this.rewards = rewards;
				this.registerEvent(CraftItemEvent.class, EventPriority.HIGHEST, event -> {
					final HumanEntity who = event.getWhoClicked();
					if(who instanceof Player) {
						final Player player = (Player) who;
						if(isVisible(player) && BukkitUtil.DataContainer.Mailbox.isMailbox(event.getCurrentItem())) incrementProgression(player);
					}
				});
			}
			@Override
			public final void giveReward(final Player player) {
				player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 1f);
				if(exp > 0) {
					player.giveExp(exp);
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 1f);
				}
				if(rewards.size() > 0) {
					for(final ItemStack reward : rewards) if(player.getInventory().firstEmpty() == -1) {
						player.getLocation().getWorld().dropItem(player.getLocation(), reward);
						player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1f, 1f);
					} else player.getInventory().addItem(reward);
				}
			}
			public static final AdvancementDisplay display(final int maxProgression) {
				return new AdvancementDisplay(Material.SHULKER_BOX, "Send " + maxProgression + " boxes!", AdvancementFrameType.CHALLENGE, true, true, 0, 0, "");
			}
		}
		public static final void setup(final AdvancementTab tab, final RootAdvancement root) {
			final CraftFirstMailboxAdvancement craftFirstMailbox = new CraftFirstMailboxAdvancement("craft_first_mailbox", CraftFirstMailboxAdvancement.display(), root, 1);
			final MailSentCountAdvancement send10Boxes = new MailSentCountAdvancement("send_10_boxes", MailSentCountAdvancement.display(10), root, 10, 10, Collections.emptyList());
			final MailSentCountAdvancement send20Boxes = new MailSentCountAdvancement("send_20_boxes", MailSentCountAdvancement.display(20), send10Boxes, 20, 15, Collections.emptyList());
			final MailSentCountAdvancement send40Boxes = new MailSentCountAdvancement("send_40_boxes", MailSentCountAdvancement.display(40), send20Boxes, 40, 35, Collections.emptyList());
			final MailSentCountAdvancement send80Boxes = new MailSentCountAdvancement("send_80_boxes", MailSentCountAdvancement.display(80), send40Boxes, 80, 50, Collections.emptyList());
			final MailSentCountAdvancement send160Boxes = new MailSentCountAdvancement("send_160_boxes", MailSentCountAdvancement.display(160), send80Boxes, 160, 80, Arrays.asList(new ItemStack(Material.NETHERITE_BLOCK, 3)));
			tab.registerAdvancements(root,
					craftFirstMailbox,
					send10Boxes,
					send20Boxes,
					send40Boxes,
					send80Boxes,
					send160Boxes);
		}
	}
}