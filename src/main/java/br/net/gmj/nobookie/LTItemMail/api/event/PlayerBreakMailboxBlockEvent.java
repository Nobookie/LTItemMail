/*
 * Copyright (C) 2025  Murilo Amaral Nappi
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package br.net.gmj.nobookie.LTItemMail.api.event;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.bukkit.event.Cancellable;

import br.net.gmj.nobookie.LTItemMail.api.block.MailboxBlock;
import br.net.gmj.nobookie.LTItemMail.api.entity.LTPlayer;
/**
 * 
 * Event called when a mailbox block is broken by a player.
 * 
 * @author Nobookie
 * 
 */
public class PlayerBreakMailboxBlockEvent extends BreakMailboxBlockEvent implements Cancellable {
	private final LTPlayer player;
	private Boolean cancelled = false;
	/**
	 * 
	 * The constructor of the event.
	 * 
	 * @param block The {@link MailboxBlock} object
	 * @param reason The reason of the event
	 * @param player The player involved on the event
	 * 
	 */
	public PlayerBreakMailboxBlockEvent(final MailboxBlock block, final BreakMailboxBlockEvent.Reason reason, final LTPlayer player) {
		super(block, reason, false, null, null);
		this.player = player;
	}
	/**
	 * 
	 * Gets who is involved on this event.
	 * 
	 * @return {@link LTPlayer} object representing a player.
	 * 
	 */
	@Nonnull
	public final LTPlayer getPlayer() {
		return player;
	}
	@Override
	public final boolean isCancelled() {
		return cancelled;
	}
	@Override
	public final void setCancelled(@Nonnull final boolean cancel) throws NullPointerException {
		Objects.requireNonNull(cancel);
		cancelled = cancel;
	}
}