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

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.item.Item;
import br.net.gmj.nobookie.LTItemMail.item.MailboxItem;

public final class RegistrationModule {
	private RegistrationModule() {}
	public static final void setupItems() {
		for(final Item i : ITEMS) {
			for(final Listener l : i.getListeners()) Bukkit.getPluginManager().registerEvents(l, LTItemMail.getInstance());
			i.runTasks();
			try {
				if(Bukkit.addRecipe(i.getRecipe())) {
					ConsoleModule.debug(RegistrationModule.class, i.getType().toString() + " recipe registered.");
				} else ConsoleModule.debug(RegistrationModule.class, i.getType().toString() + " recipe was not registered due to an unknown reason.");
			} catch(final IllegalStateException e) {
				ConsoleModule.debug(RegistrationModule.class, i.getType().toString() + " recipe is registered already.");
			}
		}
	}
	@SuppressWarnings("deprecation")
	public static final void setupBlock() {
		final MailboxBlock mailbox = new MailboxBlock(null, null, null, null, null, null, null);
		for(final Listener listener : mailbox.getListeners()) Bukkit.getPluginManager().registerEvents(listener, LTItemMail.getInstance());
		mailbox.runTasks();
	}
	private static final List<Item> ITEMS = Arrays.asList(new MailboxItem());
}