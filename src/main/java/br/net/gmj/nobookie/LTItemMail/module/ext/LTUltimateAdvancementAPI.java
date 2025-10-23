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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;

public final class LTUltimateAdvancementAPI implements LTExtension {
	private final UltimateAdvancementAPI api;
	private final List<Material> shulkers = new ArrayList<>();
	private AdvancementTab tab = null;
	private AdvancementDisplay display = null;
	private RootAdvancement root = null;
	public LTUltimateAdvancementAPI() {
		api = UltimateAdvancementAPI.getInstance(LTItemMail.getInstance());
		for(final Material shulker : Material.values()) {
			final String name = shulker.toString();
			if(!name.startsWith("LEGACY_") && name.endsWith("_SHULKER_BOX")) shulkers.add(shulker);
		}
		Collections.shuffle(shulkers);
	}
	@Override
	public final void unload() {}
	public final void show(final LTPlayer player, final String message) {
		tab = api.createAdvancementTab(LTItemMail.getInstance().getName().toLowerCase());
		display = new AdvancementDisplay(shulkers.get(new Random().nextInt(shulkers.size() - 1)), message, AdvancementFrameType.TASK, true, false, 0, 0);
		root = new RootAdvancement(tab, "root", display, "textures/block/cobblestone.png");
		tab.registerAdvancements(root);
		final Player bukkitPlayer = player.getBukkitPlayer().getPlayer();
		if(bukkitPlayer != null) {
			tab.showTab(bukkitPlayer);
			root.displayToastToPlayer(bukkitPlayer);
			new BukkitRunnable() {
				@Override
				public final void run() {
					tab.hideTab(bukkitPlayer);
					root = null;
					display = null;
					tab = null;
					api.unregisterAdvancementTab(LTItemMail.getInstance().getName().toLowerCase());
				}
			}.runTaskLater(LTItemMail.getInstance(), 1);
		}
	}
}