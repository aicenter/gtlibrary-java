package cz.agents.gtlibrary.strategy;

import java.util.Collection;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import java.io.Serializable;
import java.util.Random;

/**
 * Strategy holds mapping of sequences to their probability of occurrence,
 * only sequences with nonzero probability are stored.
 */
public abstract class Strategy implements Map<Sequence, Double>, Serializable  {

	private static final long serialVersionUID = -4151229061368151006L;

	public abstract Map<Action, Double> getDistributionOfContinuationOf(Sequence sequence, Collection<Action> actions);
	
	public abstract String fancyToString(GameState root, Expander<? extends InformationSet> expander, Player player);

	public static interface Factory  extends Serializable {
		public Strategy create();
	}
        
	public double maxDifferenceFrom(Strategy other) {
		double max = -Double.MAX_VALUE;
		for (Map.Entry<Sequence, Double> en : entrySet()) {
			final Double otherVal = other.get(en.getKey());
			double diff = (otherVal == null ? en.getValue() : Math.abs(en.getValue() - otherVal));
			if (diff > max)
				max = diff;
		}
		for (Map.Entry<Sequence, Double> en : other.entrySet()) {
			if (get(en.getKey()) == null && en.getValue() > max) {
				max = en.getValue();
			}
		}
		return max;
	}
	
	public void sanityCheck(GameState root, Expander<? extends InformationSet> expander) {
		double probability = get(root.getSequenceForPlayerToMove());
		double probabilitySum = 0;
		
		if(root.isGameEnd())
			return;
		for (Action action : expander.getActions(root)) {
			GameState child = root.performAction(action);
			
			probabilitySum += get(child.getSequenceFor(root.getPlayerToMove()));
			sanityCheck(child, expander);
		}
		if(!root.isPlayerToMoveNature())
			if(Math.abs(probability - probabilitySum) > 1e-2 && Math.abs(probabilitySum - 1) > 1e-2)
				throw new IllegalStateException("Inconsistent strategy, expected " + probability + " or 1 but was " + probabilitySum);
	}
        
        public static Action selectAction(Map<Action, Double> distribution, Random rnd){
            double r = rnd.nextDouble();
            for(Map.Entry<Action, Double> en : distribution.entrySet()){
                assert en.getValue() <= 1.0;
                if (en.getValue() > r) return en.getKey();
                else r -= en.getValue();
            }
            assert false;
            return null;
        }

}
