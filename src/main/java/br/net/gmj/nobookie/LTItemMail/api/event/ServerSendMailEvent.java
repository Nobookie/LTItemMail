package br.net.gmj.nobookie.LTItemMail.api.event;

import java.util.LinkedList;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
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
	/**
	 * 
	 * The constructor of the event.
	 * 
	 * @param to The player who received it
	 * @param contents The delivery contents
	 * @param label The delivery label if there is any
	 * 
	 */
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
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nonnull
	public final LTPlayer getTo() {
		return to;
	}
	/**
	 * 
	 * Gets the mailbox contents.
	 * 
	 * @return {@link LinkedList<ItemStack>}
	 * 
	 */
	@Nonnull
	public final LinkedList<ItemStack> getContents(){
		return contents;
	}
	/**
	 * 
	 * Gets the label of the mail.
	 * 
	 * @return {@link String}
	 * 
	 */
	@Nonnull
	public final String getLabel() {
		return label;
	}
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	/**
	 * 
	 * See {@link HandlerList}
	 * 
	 * @return {@link HandlerList}
	 * 
	 */
	@Nonnull
	public static final HandlerList getHandlerList() {
		return handlers;
	}
}