package br.net.gmj.nobookie.LTItemMail.api.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
/**
 * 
 * Event called when a mailbox block is placed.
 * 
 * @author Nobookie
 * 
 */
public class PlaceMailboxBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final MailboxBlock block;
	private Reason reason;
	public PlaceMailboxBlockEvent(final MailboxBlock block, final Reason reason) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.block = block;
		this.reason = reason;
	}
	/**
	 * 
	 * Gets the block affected by this event.
	 * 
	 */
	@NotNull
	public final MailboxBlock getBlock() {
		return block;
	}
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	@NotNull
	public static final HandlerList getHandlerList() {
		return handlers;
	}
	/**
	 * 
	 * Gets what triggered the event.
	 * 
	 */
	@NotNull
	public final Reason getReason() {
		return reason;
	}
	/**
	 * 
	 * What triggered the event.
	 * 
	 * @author Nobookie
	 * 
	 */
	public enum Reason {
		BY_PLAYER,
		BY_SERVER
	}
}
