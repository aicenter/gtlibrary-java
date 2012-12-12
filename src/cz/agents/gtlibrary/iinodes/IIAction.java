package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public abstract class IIAction implements Action {
	protected long isHash;

	public IIAction(long isHash) {
		this.isHash = isHash;
	}

	@Override
	public abstract void perform(GameState gameState);

	@Override
	public long getISHash() {
		return isHash;
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object object);
}
