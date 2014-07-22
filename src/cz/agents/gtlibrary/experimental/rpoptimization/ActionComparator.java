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


package cz.agents.gtlibrary.experimental.rpoptimization;

import java.util.Comparator;
import java.util.Map;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class ActionComparator implements Comparator<Action>{

	private Map<Sequence, Double> plan;
	private GameState gameState;
	
	public ActionComparator(Map<Sequence, Double> plan, GameState gameState) {
		this.plan = plan;
		this.gameState = gameState;
	}
	
	private double getValueOfSequence(Sequence sequence) {
		Double value = plan.get(sequence);
		
		if(value == null)
			return -1;
		return value;
	}
	
	private Sequence createSequence(Action action) {
		Sequence sequence = new LinkedListSequenceImpl(gameState.getSequenceForPlayerToMove());
		
		sequence.addLast(action);
		return sequence;
	}

	@Override
	public int compare(Action action1, Action action2) {
		double value1 = getValueOfSequence(createSequence(action1));
		double value2 = getValueOfSequence(createSequence(action2));
		
		return Double.compare(value1, value2);
	}
	
	
}
