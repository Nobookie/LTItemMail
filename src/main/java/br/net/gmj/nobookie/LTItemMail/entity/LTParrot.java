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