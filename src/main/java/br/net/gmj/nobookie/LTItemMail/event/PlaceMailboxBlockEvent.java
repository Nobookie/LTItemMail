package br.net.gmj.nobookie.LTItemMail.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.gmj.nobookie.LTItemMail.block.Block;
/**
 * 
 * Event called when a mailbox block is placed.
 * 
 * @author Nobookie
 * 
 */
public class PlaceMailboxBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Block block;
	private Reason reason;
	public PlaceMailboxBlockEvent(final Block block, final Reason reason) {
		this.block = block;
		this.reason = reason;
	}
	/**
	 * 
	 * Gets the block affected by this event.
	 * 
	 */
	public final Block getBlock() {
		return block;
	}
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	public static final HandlerList getHandlerList() {
		return handlers;
	}
	/**
	 * 
	 * Gets what triggered the event.
	 * 
	 */
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
