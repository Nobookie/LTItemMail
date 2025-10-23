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

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.ConsoleModule;
import br.net.gmj.nobookie.LTItemMail.module.DataModule;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;
import me.arcaniax.hdb.api.HeadDatabaseAPI;

public final class LTHeadDatabase implements LTExtension {
	private final HeadDatabaseAPI api;
	public LTHeadDatabase() {
		api = new HeadDatabaseAPI();
		check();
		load();
	}
	@Override
	public final void unload() {}
	private File file;
	private final void check() {
		file = FetchUtil.FileManager.get("headdatabase.yml");
		if(file == null) {
			ConsoleModule.warning("headdatabase.yml not found!");
			ConsoleModule.warning("Generating a new one and all default head IDs will be added.");
			FetchUtil.FileManager.create("headdatabase.yml");
		}
	}
	private FileConfiguration heads = null;
	private final void load() {
		file = FetchUtil.FileManager.get("headdatabase.yml");
		if(file != null) {
			final FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				heads = configuration;
				ConsoleModule.info("HeadDatabase IDs loaded.");
				try {
					if(configuration.getInt("head-version") < DataModule.Version.HEADDATABASE_YML.value()) {
						ConsoleModule.warning("HeadDatabase IDs outdated!");
						ConsoleModule.warning("New IDs will be added.");
						configuration.set("head-version", DataModule.Version.HEADDATABASE_YML.value());
						configuration.save(file);
						addMissing();
					}
				} catch(final IllegalArgumentException e) {
					configuration.set("head-version", 0);
					configuration.save(file);
				}
			} catch (final IOException | InvalidConfigurationException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
	}
	public final ItemStack getHead(final Type type) {
		Integer id = type.id();
		final String path = type.path();
		if(heads.isSet(path)) {
			id = heads.getInt(path);
		} else {
			ConsoleModule.info("HeadDatabase IDs fallback: [" + path + ":" + id + "]");
			heads.set(path, id);
			try {
				heads.save(file);
			} catch (final IOException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		return get(id);
	}
	private final void addMissing() {
		for(final Type type : Type.values()) getHead(type);
	}
	private final ItemStack get(final Integer id) {
		try {
			return api.getItemHead(String.valueOf(id));
		} catch(final NullPointerException e) {
			ConsoleModule.debug(getClass(), "Unable to retrieve head from HeadDatabase server [" + id + "].");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return null;
	}
	public final Integer getId(final ItemStack head) {
		return Integer.parseInt(api.getItemID(head));
	}
	public enum Type {
		MAILBOX_BUTTON_COST("mailbox.button.cost", 60568),
		MAILBOX_BUTTON_LABEL("mailbox.button.label", 66505),
		MAILBOX_BUTTON_DENY("mailbox.button.deny", 106285),
		MAILBOX_BUTTON_ACCEPT("mailbox.button.accept", 106284);
		private final String path;
		private final Integer id;
		Type(final String path, final Integer id){
			this.path = path;
			this.id = id;
		}
		public final String path() {
			return path;
		}
		public final Integer id() {
			return id;
		}
	}
}