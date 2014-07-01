package cz.agents.gtlibrary.strategy;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.util.*;

public class UniformStrategyForMissingSequences extends StrategyImpl {

	@Override
	protected Map<Action, Double> getMissingSeqDistribution(Collection<Action> actions) {
		Map<Action, Double> distribution = new HashMap<Action, Double>();
		
		for (Action action : actions) {
			distribution.put(action, 1./actions.size());
		}
		return distribution;
	}
	
	public static class Factory implements Strategy.Factory {

		@Override
		public Strategy create() {
			return new UniformStrategyForMissingSequences();
		}
		
	}
        
        public static UniformStrategyForMissingSequences computeMeanStrategy(Collection<Strategy> strategies, GameState root, Expander expander){
            UniformStrategyForMissingSequences out = new UniformStrategyForMissingSequences();
            Player pl = strategies.iterator().next().keySet().iterator().next().getPlayer();
            out.put(new ArrayListSequenceImpl(pl), 1.0);
            Deque<GameState> q = new ArrayDeque();
            q.add(root);
            while(!q.isEmpty()){//DFS
                GameState curState = q.removeFirst();
                if (curState.isGameEnd()) continue;
                List<Action> actions = expander.getActions(curState);
                if (curState.getPlayerToMove().equals(pl)){
                    Sequence tmp = new ArrayListSequenceImpl(curState.getSequenceForPlayerToMove());
                    tmp.addLast(actions.get(0));
                    if (!out.containsKey(tmp)){
                        Map<Action, Double> meanDist = new HashMap();
                        for (Action a : actions) meanDist.put(a, 0.0);
                        for (Strategy s : strategies){
                            Map<Action, Double> dist = s.getDistributionOfContinuationOf(curState.getSequenceForPlayerToMove(), actions);
                            for (Action a : actions) meanDist.put(a, meanDist.get(a)+dist.get(a)/strategies.size());
                        }
                        double prefProb = out.get(curState.getSequenceForPlayerToMove());
                        for (Action a : actions){
                            tmp = new ArrayListSequenceImpl(curState.getSequenceForPlayerToMove());
                            tmp.addLast(a);
                            out.put(tmp, prefProb*meanDist.get(a));
                        }
                    }
                }
                for (Action a : actions){
                    q.addFirst(curState.performAction(a));
                }
            }
            return out;
        }

        public static UniformStrategyForMissingSequences fromBehavioralStrategy(Map<Pair<Integer,Sequence>, Map<Action,Double>> behav, GameState root, Expander expander){
            UniformStrategyForMissingSequences out = new UniformStrategyForMissingSequences();
            Player pl = behav.entrySet().iterator().next().getKey().getRight().getPlayer();
            out.put(new ArrayListSequenceImpl(pl), 1.0);
            Deque<GameState> q = new ArrayDeque();
            q.add(root);
            while(!q.isEmpty()){//DFS
                GameState curState = q.removeFirst();
                if (curState.isGameEnd()) continue;
                List<Action> actions = expander.getActions(curState);
                if (curState.getPlayerToMove().equals(pl)){
                    Sequence tmp = new ArrayListSequenceImpl(curState.getSequenceForPlayerToMove());
                    tmp.addLast(actions.get(0));
                    if (!out.containsKey(tmp)){
                        Map<Action, Double> curDist = behav.get(curState.getISKeyForPlayerToMove());
                        if (curDist == null){
                            curDist = new FixedSizeMap(actions.size());
                            for (Action a : actions){
                                curDist.put(a, 1.0/actions.size());
                            }
                        }
                        double sum=0;
                        for (Double d : curDist.values()) sum += d;
                        assert sum>0;
                        double prefProb = out.get(curState.getSequenceForPlayerToMove());
                        for (Action a : actions){
                            tmp = new ArrayListSequenceImpl(curState.getSequenceForPlayerToMove());
                            tmp.addLast(a);
                            out.put(tmp, prefProb*curDist.get(a)/sum);
                        }
                    }
                }
                for (Action a : actions){
                    q.addFirst(curState.performAction(a));
                }
            }
            return out;
        }
        
}

