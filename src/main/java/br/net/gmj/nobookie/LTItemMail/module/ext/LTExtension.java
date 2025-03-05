package br.net.gmj.nobookie.LTItemMail.module.ext;

import org.bukkit.plugin.Plugin;

public interface LTExtension {
	public Plugin getBasePlugin();
	public void unload();
}