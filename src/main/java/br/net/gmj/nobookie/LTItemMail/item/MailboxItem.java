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
package br.net.gmj.nobookie.LTItemMail.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import br.net.gmj.nobookie.LTItemMail.LTItemMail;
import br.net.gmj.nobookie.LTItemMail.item.listener.MailboxItemListener;
import br.net.gmj.nobookie.LTItemMail.module.ConfigurationModule;
import br.net.gmj.nobookie.LTItemMail.module.LanguageModule;
import br.net.gmj.nobookie.LTItemMail.util.BukkitUtil;

public final class MailboxItem implements Item {
	@Override
	public final String getName() {
		return (String) ConfigurationModule.get(ConfigurationModule.Type.MAILBOX_NAME);
	}
	@Override
	public final List<String> getDescription(final String data) {
		return Arrays.asList(LanguageModule.get(LanguageModule.Type.BLOCK_BELOWERROR));
	}
	@Override
	public final Item.Type getType() {
		return Item.Type.MAILBOX_ITEM;
	}
	@Override
	public final Material getMaterial() {
		return Material.WHITE_SHULKER_BOX;
	}
	@Override
	public final String[] getData() {
		return null;
	}
	@Override
	public final List<Listener> getListeners() {
		return Arrays.asList(new MailboxItemListener());
	}
	@Override
	public final void runTasks() {}
	@Override
	public final List<BukkitTask> getTasks() {
		return Collections.emptyList();
	}
	@Override
	public final ItemStack getItem(final String data) {
		final ItemStack item = new ItemStack(getMaterial(), 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(getName());
		meta.setLore(getDescription(data));
		meta = BukkitUtil.DataContainer.Mailbox.setMailbox(meta);
		item.setItemMeta(meta);
		return item;
	}
	@Override
	public final Recipe getRecipe() {
		final NamespacedKey key = new NamespacedKey(LTItemMail.getInstance(), getType().toString().toLowerCase());
		final ShapedRecipe recipe = new ShapedRecipe(key, getItem(null));
		recipe.shape("ppp", "pbp", "pgp");
		recipe.setIngredient('p', Material.STONE);
		recipe.setIngredient('b', Material.CHEST);
		recipe.setIngredient('g', Material.IRON_INGOT);
		return recipe;
	}
}