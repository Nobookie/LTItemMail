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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ServerLinks.ServerLink;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;

public final class LinkModule {
	public static final List<ServerLink> links = new ArrayList<>();
	public static final void register() {
		if(LTItemMail.getInstance().getServerVer() >= 21 && LTItemMail.getInstance().getServerRev() >= 7) links.add(Bukkit.getServerLinks().addLink("LT Item Mail Wiki", URI.create("https://nobookie.gitbook.io/lt-item-mail/")));
	}
	public static final void unload() {
		if(LTItemMail.getInstance().getServerVer() >= 21 && LTItemMail.getInstance().getServerRev() >= 7) for(final ServerLink link : links) Bukkit.getServerLinks().removeLink(link);
	}
	/*public static final void load() {
		if(slClass() != null) try {
			final Method method = slClass().getDeclaredMethod("add", String.class, URI.class);
			method.setAccessible(true);
			method.invoke("LT Item Mail Wiki", URI.create("https://nobookie.gitbook.io/lt-item-mail/"));
		} catch(final Exception e) {}
	}
	private static final Class<?> slClass(){
		Class<?> clazz = null;
		try {
			clazz = Class.forName("org.bukkit.ServerLinks");
		} catch (final ClassNotFoundException | SecurityException e) {
			ConsoleModule.debug(LinkModule.class, "You are running an older version of the game. Method \"getServerLinks\" not found.");
			if((Boolean) ConfigurationModule.get(ConfigurationModule.Type.PLUGIN_DEBUG)) e.printStackTrace();
		}
		return clazz;
	}*/
}