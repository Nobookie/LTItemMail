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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DataModule;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

public final class VersionControlTask {
	private BukkitTask task;
	public VersionControlTask() {
		new BukkitRunnable() {
			@Override
			public final void run() {
				FetchUtil.FileManager.download(DataModule.getManifestURL(LTItemMail.getInstance().getDescription().getVersion()), "manifest.yml", true);
				task = new BukkitRunnable() {
					@Override
					public final void run() {
						try {
							final YamlConfiguration info = new YamlConfiguration();
							info.load(FetchUtil.FileManager.get("manifest.yml"));
							if(!info.getBoolean("main.enabled")) {
								final List<String> warnings = info.getStringList("main.warnings");
								if(warnings.size() > 0) ConsoleModule.server(warnings);
								Bukkit.getPluginManager().disablePlugin(LTItemMail.getInstance());
							} else {
								final List<Integer> boards = new ArrayList<>();
								int rawID = 0;
								while(true) if(info.get("boards." + rawID) != null) {
									boards.add(rawID);
									rawID++;
								} else break;
								final Map<Integer, Map<String, Map<String, List<String>>>> messages = new HashMap<>();
								for(int id : boards) if(!ConfigurationModule.getBoardsRead().contains(id)) {
									LTItemMail.getInstance().boardsForPlayers.add(id);
									final Map<String, Map<String, List<String>>> contents1 = new HashMap<>();
									final Map<String, List<String>> contents2 = new HashMap<>();
									contents2.put(info.getString("boards." + id + ".datetime"), info.getStringList("boards." + id + ".contents"));
									contents1.put(info.getString("boards." + id + ".title"), contents2);
									messages.put(id, contents1);
									ConfigurationModule.setBoardRead(id);
								}
								if(messages.size() > 0) ConsoleModule.board(Bukkit.getConsoleSender(), messages);
							}
							task.cancel();
						} catch (final InvalidConfigurationException | IOException e) {
							if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
						} catch(final IllegalArgumentException e) {}
					}
				}.runTaskTimerAsynchronously(LTItemMail.getInstance(), 20, 20);
			}
		}.runTaskAsynchronously(LTItemMail.getInstance());
	}
}