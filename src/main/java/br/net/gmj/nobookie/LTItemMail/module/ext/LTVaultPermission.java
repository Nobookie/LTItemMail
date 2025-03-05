package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.plugin.Plugin;

public final class LTVaultPermission implements LTExtension {
	private final Plugin plugin;
	private final Plugin permissionPlugin;
	private final net.milkbowl.vault.permission.Permission api;
	public LTVaultPermission(final Plugin plugin, final Plugin permissionPlugin, final net.milkbowl.vault.permission.Permission api) {
		this.plugin = plugin;
		this.permissionPlugin = permissionPlugin;
		this.api = api;
	}
	@Override
	public final Plugin getBasePlugin() {
		return plugin;
	}
	@Override
	public final void unload() {}
	public final Plugin getPermissionPlugin() {
		return permissionPlugin;
	}
	public final net.milkbowl.vault.permission.Permission getAPI() {
		return api;
	}
}