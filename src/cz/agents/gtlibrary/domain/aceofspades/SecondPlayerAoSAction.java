package cz.agents.gtlibrary.domain.aceofspades;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class SecondPlayerAoSAction extends ActionImpl {

	private static final long serialVersionUID = -7419919581936745640L;

	private boolean guessedAceOfSpades;

	public SecondPlayerAoSAction(InformationSet informationSet, boolean guessedAceOfSpades) {
		super(informationSet);
		this.guessedAceOfSpades = guessedAceOfSpades;
	}

	@Override
	public void perform(GameState gameState) {
		((AoSGameState) gameState).performSecondPlayerAction(this);
	}

	public boolean guessedAceOfSpades() {
		return guessedAceOfSpades;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (guessedAceOfSpades ? 1231 : 1237);
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
		SecondPlayerAoSAction other = (SecondPlayerAoSAction) obj;
		if (guessedAceOfSpades != other.guessedAceOfSpades)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return guessedAceOfSpades ? "I am guessing ace of spades." : "I am guessing another card.";
	}
}
