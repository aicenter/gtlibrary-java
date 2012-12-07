package gametree.IINodes;

import gametree.interfaces.Action;
import gametree.interfaces.GameState;

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
