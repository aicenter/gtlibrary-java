package cz.agents.gtlibrary.experimental.stochastic.experiment;

import cz.agents.gtlibrary.experimental.stochastic.StochasticAction;
import cz.agents.gtlibrary.interfaces.GameState;

public class PatrollerAction extends StochasticAction {
	
	private static final long serialVersionUID = 5799630828028763732L;
	
	private Commitment commitment;
	
	public PatrollerAction(Commitment commitment) {
		this.commitment = commitment;
	}

	@Override
	public void perform(GameState gameState) {
		((ExperimentGameState)gameState).setLastCommitment(commitment);
	}
	
	public Commitment getCommitment() {
		return commitment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commitment == null) ? 0 : commitment.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		PatrollerAction other = (PatrollerAction) obj;
		
		if (commitment == null) {
			if (other.commitment != null)
				return false;
		} else if (!commitment.equals(other.commitment))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[PA: " + commitment + "]";
	}

}
