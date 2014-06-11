package cz.agents.gtlibrary.domain.aceofspades;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class NatureAoSAction extends ActionImpl {

	private static final long serialVersionUID = 7481078580610165635L;

	private boolean isAceOfSpades;

	public NatureAoSAction(InformationSet informationSet, boolean isAceOfSpades) {
		super(informationSet);
		this.isAceOfSpades = isAceOfSpades;
	}

	@Override
	public void perform(GameState gameState) {
		((AoSGameState) gameState).performNatureAction(this);
	}

	public boolean isAceOfSpades() {
		return isAceOfSpades;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isAceOfSpades ? 1231 : 1237);
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
		NatureAoSAction other = (NatureAoSAction) obj;
		if (isAceOfSpades != other.isAceOfSpades)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return isAceOfSpades ? "Ace of spades" : "Another card";
	}

}
