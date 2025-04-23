package br.net.gmj.nobookie.LTItemMail.api.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import br.net.gmj.nobookie.LTItemMail.api.block.Block;
/**
 * 
 * Event called when a mailbox block breaks.
 * 
 * @author Nobookie
 * 
 */
public class BreakMailboxBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Block block;
	private Reason reason;
	private final Boolean virtual;
	private final ClaimProvider provider;
	public BreakMailboxBlockEvent(final Block block, final Reason reason, final Boolean virtual, final ClaimProvider provider) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.block = block;
		this.reason = reason;
		this.virtual = virtual;
		this.provider = provider;
	}
	/**
	 * 
	 * Gets the block affected by this event.
	 * 
	 */
	@NotNull
	public final Block getBlock() {
		return block;
	}
	/**
	 * 
	 * "true" means that the block was removed from the database and it remains on the game as a vanilla shulker box.
	 * 
	 */
	@NotNull
	public final Boolean isVirtual() {
		return virtual;
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
	 * Gets what plugin was involved on the break event if the reason is {@link Reason#ON_UNCLAIM}. Otherwise it will return null.
	 * 
	 */
	public final ClaimProvider getClaimProvider() {
		return provider;
	}
	/**
	 * 
	 * What triggered the event.
	 * 
	 * @author Nobookie
	 * 
	 */
	public enum Reason {
		ON_UNCLAIM,
		BY_PLAYER_OWNER,
		BY_PLAYER_ADMIN,
		BY_SERVER
	}
	/**
	 * 
	 * Claim providers.
	 * 
	 * @author Nobookie
	 * 
	 */
	public enum ClaimProvider {
		GRIEFPREVENTION,
		REDPROTECT,
		TOWNYADVANCED
	}
}
