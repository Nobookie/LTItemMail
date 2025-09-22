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