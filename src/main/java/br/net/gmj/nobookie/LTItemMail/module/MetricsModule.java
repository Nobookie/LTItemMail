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
package br.net.gmj.nobookie.LTItemMail.module;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.util.BStats;

public final class MetricsModule implements Runnable {
	private static BStats metrics = null;
	public MetricsModule() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(LTItemMail.getInstance(), this, 1, 20 * 60 * 15);
	}
	private final void setup() {
		shutdown();
		metrics = new BStats(LTItemMail.getInstance(), DataModule.BSTATS);
		metrics.addCustomChart(new BStats.SimplePie("builds", () -> {
	        return String.valueOf((Integer) ConfigurationModule.get(ConfigurationModule.Type.BUILD_NUMBER));
	    }));
		metrics.addCustomChart(new BStats.SimplePie("language", () -> {
	        return ((String) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_LANGUAGE)).toLowerCase();
	    }));
		for(final ExtensionModule.EXT plugin : ExtensionModule.getInstance().REG.keySet()) metrics.addCustomChart(new BStats.SimplePie("extensions", () -> {
	        return plugin.plugin().getDescription().getName();
	    }));
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_HOOK_ECONOMY_ENABLE) && EconomyModule.getInstance() != null && EconomyModule.getInstance().getEconomy() != null) metrics.addCustomChart(new BStats.SimplePie("economy", () -> {
	        return EconomyModule.getInstance().getEconomy().plugin().getName();
	    }));
		for(final JavaPlugin plugin : LTItemMail.getInstance().apiHandlers) metrics.addCustomChart(new BStats.SimplePie("api", () -> {
	        return plugin.getDescription().getName();
	    }));
	}
	@Override
	public final void run() {
		setup();
	}
	public static final void shutdown() {
		if(metrics != null) metrics.shutdown();
	}
}