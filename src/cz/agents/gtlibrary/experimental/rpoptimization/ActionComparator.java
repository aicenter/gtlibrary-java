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
