package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.plugin.Plugin;

public final class LTVault implements LTExtension {
	private final Plugin permissionPlugin;
	private final net.milkbowl.vault.permission.Permission api;
	public LTVault(final Plugin permissionPlugin, final net.milkbowl.vault.permission.Permission api) {
		this.permissionPlugin = permissionPlugin;
		this.api = api;
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