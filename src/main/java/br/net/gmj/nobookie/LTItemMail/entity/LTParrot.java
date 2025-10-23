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
package br.net.gmj.nobookie.LTItemMail.entity;

import net.citizensnpcs.api.npc.NPC;

public final class LTParrot {
	private final NPC parrot;
	private boolean dismissed = false;
	public LTParrot(final NPC parrot) {
		this.parrot = parrot;
	}
	public final NPC getParrot() {
		return parrot;
	}
	public final boolean isDismissed() {
		return dismissed;
	}
	public final void setDismissed(final boolean dismiss) {
		this.dismissed = dismiss;
	}
}