package cz.agents.gtlibrary.experimental.stochastic.experiment;

import cz.agents.gtlibrary.experimental.stochastic.StochasticAction;
import cz.agents.gtlibrary.interfaces.GameState;

public class NormalizationNatrueAction extends StochasticAction {

	private Commitment commitment;
	private double probability;
	
	public NormalizationNatrueAction(Commitment commitment) {
		this.commitment = commitment;
	}
	
	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	public double getProbability() {
		return probability;
	}

	@Override
	public void perform(GameState gameState) {
		((ExperimentGameState)gameState).normalizeCommitment(commitment);
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
		NormalizationNatrueAction other = (NormalizationNatrueAction) obj;
		
		if (commitment == null) {
			if (other.commitment != null)
				return false;
		} else if (!commitment.equals(other.commitment))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[NA: " + commitment + "]";
	}

}
