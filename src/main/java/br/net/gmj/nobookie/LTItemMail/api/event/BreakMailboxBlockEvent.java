package br.net.gmj.nobookie.LTItemMail.api.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
/**
 * 
 * Event called when a mailbox block breaks.
 * 
 * @author Nobookie
 * 
 */
public class BreakMailboxBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final MailboxBlock block;
	private final Reason reason;
	private final Boolean virtual;
	private final ClaimProvider provider;
	private final Event providerEvent;
	/**
	 * 
	 * The constructor of the event.
	 * 
	 * @param block The {@link MailboxBlock} object
	 * @param reason The reason of the event
	 * @param virtual Indicates whether the event was purely virtual or not
	 * @param provider The claim provider if there is any
	 * @param providerEvent The original event from Bukkit
	 * 
	 */
	public BreakMailboxBlockEvent(final MailboxBlock block, final Reason reason, final Boolean virtual, final ClaimProvider provider, final Event providerEvent) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.block = block;
		this.reason = reason;
		this.virtual = virtual;
		this.provider = provider;
		this.providerEvent = providerEvent;
	}
	/**
	 * 
	 * Gets the block affected by this event.
	 * 
	 * @return {@link MailboxBlock}
	 * 
	 */
	@Nonnull
	public final MailboxBlock getBlock() {
		return block;
	}
	/**
	 * 
	 * Indicates whether the event was purely virtual or not
	 * 
	 * @return {@link Boolean#TRUE} if the block was removed from the database but remains placed on the current world as a vanilla Shulker Box.
	 * 
	 */
	@Nonnull
	public final Boolean isVirtual() {
		return virtual;
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
	/**
	 * 
	 * Gets what triggered the event.
	 * 
	 * @return {@link Reason}
	 * 
	 */
	@Nonnull
	public final Reason getReason() {
		return reason;
	}
	/**
	 * 
	 * Gets what plugin was involved on the break event if the reason is {@link Reason#ON_UNCLAIM}. Otherwise it will return null.
	 * 
	 * @return {@link ClaimProvider} or null
	 * 
	 */
	@Nullable
	public final ClaimProvider getClaimProvider() {
		return provider;
	}
	/**
	 * 
	 * Gets the original event from Bukkit.
	 * 
	 * @return {@link Event} or null
	 * 
	 */
	@Nullable
	public final Event getClaimProviderEvent() {
		return providerEvent;
	}
	/**
	 * 
	 * What triggered the event.
	 * 
	 * @author Nobookie
	 * 
	 */
	public enum Reason {
		/**
		 * 
		 * Called on unclaim.
		 * 
		 */
		ON_UNCLAIM,
		/**
		 * 
		 * Called by owner.
		 * 
		 */
		BY_PLAYER_OWNER,
		/**
		 * 
		 * Called by admin.
		 * 
		 */
		BY_PLAYER_ADMIN,
		/**
		 * 
		 * Called by server.
		 * 
		 */
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
		/**
		 * 
		 * Called by GriefPrevention.
		 * 
		 */
		GRIEFPREVENTION,
		/**
		 * 
		 * Called by RedProtect.
		 * 
		 */
		REDPROTECT,
		/**
		 * 
		 * Called by TownyAdvanced.
		 * 
		 */
		TOWNYADVANCED,
		/**
		 * 
		 * Called by Lands.
		 * 
		 */
		LANDS
	}
}
