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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.entity.LTParrot;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;

public final class LTCitizens implements LTExtension, Runnable {
	private final Random random;
	private final Map<String, LTParrot> parrots = new HashMap<>();
	private final List<Parrot.Variant> variants = Arrays.asList(Parrot.Variant.BLUE,
			Parrot.Variant.CYAN,
			Parrot.Variant.GRAY,
			Parrot.Variant.GREEN,
			Parrot.Variant.RED);
	public LTCitizens() {
		random = new Random();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(LTItemMail.getInstance(), this, 1, 1);
	}
	@Override
	public final void unload() {
		for(final String player : parrots.keySet()) {
			parrots.get(player).getParrot().destroy();
			parrots.remove(player);
			ConsoleModule.debug(getClass(), "Delivery Parrot of " + player + " destroyed!");
		}
	}
	public final boolean call(final Player player) {
		if(!parrots.containsKey(player.getName())) if(checkLocation(player.getLocation())) {
			final NPC parrot = CitizensAPI.getNPCRegistry().createNPC(EntityType.PARROT, LanguageModule.get(LanguageModule.Type.ENTITY_PARROT_NAME));
			parrot.setFlyable(false);
			parrot.setProtected(true);
			parrot.setUseMinecraftAI(false);
			int x = 0;
			int z = 0;
			if(player.getFacing().equals(BlockFace.EAST)) x = 1;
			if(player.getFacing().equals(BlockFace.WEST)) x = -1;
			if(player.getFacing().equals(BlockFace.SOUTH)) z = 1;
			if(player.getFacing().equals(BlockFace.NORTH)) z = -1;
			if(player.getFacing().equals(BlockFace.NORTH_EAST)) {
				x = 1;
				z = -1;
			}
			if(player.getFacing().equals(BlockFace.NORTH_WEST)) {
				x = -1;
				z = -1;
			}
			if(player.getFacing().equals(BlockFace.SOUTH_EAST)) {
				x = 1;
				z = 1;
			}
			if(player.getFacing().equals(BlockFace.SOUTH_WEST)) {
				x = -1;
				z = 1;
			}
			final Location spawn = player.getLocation().clone();
			final Location move  = player.getLocation().clone();
			spawn.add(x * 6, 3, z * 6);
			move.add(x, 0, z);
			parrot.spawn(spawn, SpawnReason.PLUGIN);
			((Parrot) parrot.getEntity()).setVariant(variants.get(new Random().nextInt(variants.size() - 1)));
			parrots.put(player.getName(), new LTParrot(parrot));
			new BukkitRunnable() {
				@Override
				public final void run() {
					if(parrot.isSpawned()) {
						parrot.setMoveDestination(move);
						final Block relative = parrot.getStoredLocation().getBlock().getRelative(BlockFace.DOWN);
						if(!relative.getType().equals(Material.AIR)) if(!relative.getType().toString().endsWith("_AIR")) this.cancel();
					}
				}
			}.runTaskTimer(LTItemMail.getInstance(), 1, 1);
			return true;
		}
		return false;
	}
	public final boolean dismiss(final String player) {
		if(parrots.containsKey(player)) {
			final LTParrot parrot = parrots.get(player);
			if(parrot.isDismissed()) return true;
			parrot.setDismissed(true);
			if(parrot.getParrot().isSpawned()) {
				if(checkLocation(parrot.getParrot().getStoredLocation())) {
					final double x = randomDouble();
					final double z = randomDouble();
					final BukkitTask task = new BukkitRunnable() {
						@Override
						public final void run() {
							if(parrot.getParrot().isSpawned()) {
								final Location move = parrot.getParrot().getStoredLocation().clone();
								move.add(x, 0.05, z);
								parrot.getParrot().setMoveDestination(move);
							}
						}
					}.runTaskTimer(LTItemMail.getInstance(), 1, 1);
					if(!task.isCancelled()) new BukkitRunnable() {
						@Override
						public final void run() {
							task.cancel();
							parrot.getParrot().destroy();
							parrots.remove(player);
						}
					}.runTaskLater(LTItemMail.getInstance(), 20 * 5);
				} else {
					parrot.getParrot().destroy();
					parrots.remove(player);
				}
			} else {
				parrot.getParrot().destroy();
				parrots.remove(player);
			}
			return true;
		}
		return false;
	}
	private final boolean checkLocation(final Location location) {
		for(int x = location.getBlockX() - 3; x < location.getBlockX() + 3; x++)
			for(int z = location.getBlockZ() - 3; z < location.getBlockZ() + 3; z++)
				for(int y = location.getBlockY() + 1; y < location.getBlockY() + 6; y++) {
					final Location block = location.clone();
					block.setX(x);
					block.setY(y);
					block.setZ(z);
					if(!block.getBlock().getType().equals(Material.AIR)) if(!block.getBlock().getType().toString().endsWith("_AIR")) return false;
				}
		return true;
	}
	private final double randomDouble() {
		return ((random.nextInt(100) + 1 + random.nextDouble()) - 10) / 100;
	}
	@Override
	public final void run() {
		for(final String name : parrots.keySet()) {
			final Player player = Bukkit.getPlayer(name);
			final LTParrot parrot = parrots.get(name);
			if(!parrot.isDismissed()) if(player == null) {
				dismiss(name);
			} else if(parrot.getParrot().getStoredLocation() != null && parrot.getParrot().getStoredLocation().distance(player.getLocation()) >= 10) dismiss(name);
		}
	}
}