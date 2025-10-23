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
package br.net.gmj.nobookie.LTItemMail.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;

public class UpdateTask {
	private final BukkitTask task;
	public UpdateTask() {
		task = new BukkitRunnable() {
			@Override
			public final void run() {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ltitemmail:itemmailadmin update -s");
				if(!(Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_UPDATE_PERIODIC_NOTIFICATION)) task.cancel();
			}
		}.runTaskTimer(LTItemMail.getInstance(), 1, 20 * 60 * 60 * 24);
	}
}