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
package br.net.gmj.nobookie.LTItemMail.module;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.util.FetchUtil;

public class ModelsModule {
	private ModelsModule() {}
	private static File file;
	public static final void check() {
		file = FetchUtil.FileManager.get("item-models.yml");
		if(file == null) {
			ConsoleModule.warning("item-models.yml not found!");
			ConsoleModule.warning("Generating a new one and all default models will be added.");
			FetchUtil.FileManager.create("item-models.yml");
		}
	}
	private static boolean update = false;
	public static final FileConfiguration load() {
		file = FetchUtil.FileManager.get("item-models.yml");
		if(file != null) {
			final FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
				ConsoleModule.info("Item models loaded.");
				try {
					if(configuration.getInt("model-version") < DataModule.Version.ITEM_MODELS_YML.value()) {
						update = true;
						ConsoleModule.warning("Item models outdated!");
						ConsoleModule.warning("New models will be added.");
						configuration.set("model-version", DataModule.Version.ITEM_MODELS_YML.value());
						configuration.save(file);
					}
				} catch(final IllegalArgumentException e) {
					configuration.set("model-version", 0);
					configuration.save(file);
				}
				return configuration;
			} catch (final IOException | InvalidConfigurationException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		return null;
	}
	public static final Integer get(final Type type) {
		Integer result = type.result();
		final String path = type.path();
		if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_TEXTURES)) if(LTItemMail.getInstance().models.isSet(path)) {
			result = LTItemMail.getInstance().models.getInt(path);
		} else {
			ConsoleModule.info("Item models fallback: [" + path + ":" + result + "]");
			LTItemMail.getInstance().models.set(path, result);
			try {
				LTItemMail.getInstance().models.save(file);
			} catch (final IOException e) {
				if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
			}
		}
		if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.HEADDATABASE) && type.skull()) result = -1;
		if(ExtensionModule.getInstance().isRegistered(ExtensionModule.EXT.SKULLS) && type.skull()) result = -1;
		return result;
	}
	public static final void addMissing() {
		if(update) {
			for(final Type type : Type.values()) get(type);
			update = false;
		}
	}
	public enum Type {
		MAILBOX_LIMITER("mailbox.limiter", 999999001, false),
		MAILBOX_BUTTON_COST("mailbox.button.cost", 999999002, true),
		MAILBOX_BUTTON_LABEL("mailbox.button.label", 999999006, true),
		MAILBOX_BUTTON_DENY("mailbox.button.deny", 999999007, true),
		MAILBOX_BUTTON_ACCEPT("mailbox.button.accept", 999999008, true),
		MAILBOX_GUI_NORMAL("mailbox.gui.normal", 999999003, false),
		MAILBOX_GUI_PENDING("mailbox.gui.pending", 999999004, false),
		MAILBOX_GUI_ADMIN("mailbox.gui.admin", 999999005, false);
		private final String path;
		private final Integer result;
		private final Boolean skull;
		Type(final String path, final Integer result, final Boolean skull){
			this.path = path;
			this.result = result;
			this.skull = skull;
		}
		public final String path() {
			return path;
		}
		public final Integer result() {
			return result;
		}
		public final Boolean skull() {
			return skull;
		}
	}
}