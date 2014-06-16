package cz.agents.gtlibrary.domain.aceofspades;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class FirstPlayerAoSAction extends ActionImpl {

	private static final long serialVersionUID = -1465461941414355860L;

	private boolean wantsToContinue;

	public FirstPlayerAoSAction(InformationSet informationSet, boolean wantsToContinue) {
		super(informationSet);
		this.wantsToContinue = wantsToContinue;
	}

	@Override
	public void perform(GameState gameState) {
		((AoSGameState) gameState).performFirstPlayerAction(this);
	}

	public boolean wantsToContinue() {
		return wantsToContinue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (wantsToContinue ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FirstPlayerAoSAction other = (FirstPlayerAoSAction) obj;
		if (wantsToContinue != other.wantsToContinue)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return wantsToContinue ? "I want to continue." : "I don't want to continue";
	}

}
