package br.net.gmj.nobookie.LTItemMail.event;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.entity.LTPlayer;
/**
 * 
 * Event called when a mailbox is sent.
 * 
 * @author Nobookie
 * 
 */
public final class ServerSendMailEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final LTPlayer to;
	private final LinkedList<ItemStack> contents;
	private final String label;
	public ServerSendMailEvent(final LTPlayer to, final LinkedList<ItemStack> contents, final String label) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.to = to;
		this.contents = contents;
		this.label = label;
	}
	/**
	 * 
	 * Gets who received the mailbox.
	 * 
	 */
	@NotNull
	public final LTPlayer getTo() {
		return to;
	}
	/**
	 * 
	 * Gets the mailbox contents.
	 * 
	 */
	@NotNull
	public final LinkedList<ItemStack> getContents(){
		return contents;
	}
	/**
	 * 
	 * Gets the label of the mail.
	 * 
	 */
	@NotNull
	public final String getLabel() {
		return label;
	}
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	@NotNull
	public static final HandlerList getHandlerList() {
		return handlers;
	}
}