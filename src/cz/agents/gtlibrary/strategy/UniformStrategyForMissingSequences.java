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


package cz.agents.gtlibrary.strategy;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;

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

        public static UniformStrategyForMissingSequences fromBehavioralStrategy(Map<ISKey, Map<Action,Double>> behav, GameState root, Expander expander){
            UniformStrategyForMissingSequences out = new UniformStrategyForMissingSequences();
            Player pl = ((PerfectRecallISKey)behav.entrySet().iterator().next().getKey()).getSequence().getPlayer();
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

