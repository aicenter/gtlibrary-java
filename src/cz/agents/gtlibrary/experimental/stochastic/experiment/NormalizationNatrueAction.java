/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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
