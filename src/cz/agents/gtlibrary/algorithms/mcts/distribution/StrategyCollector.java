/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.Pair;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class StrategyCollector {
    
        static public Strategy getStrategyFor(InnerNode node, Player player, Distribution distribution){
            return getStrategyFor(node, player, distribution, Integer.MAX_VALUE);
        }

        
	static public Strategy getStrategyFor(InnerNode rootNode, Player player, Distribution distribution, int cutOffDepth) {
            Strategy strategy = new UniformStrategyForMissingSequences();
            strategy.put(new ArrayListSequenceImpl(player),1.0);
            HashSet<MCTSInformationSet> processed = new HashSet();
            ArrayDeque<InnerNode> q = new ArrayDeque();
            q.add(rootNode);
            while (!q.isEmpty()){
                InnerNode curNode = q.removeFirst();
                MCTSInformationSet curNodeIS = curNode.getInformationSet();
//                if (((NbSamplesProvider)curNodeIS.getAlgorithmData()).getNbSamples() < 10)
//                    continue;
 
                if (curNodeIS.getPlayer().equals(player) && !processed.contains(curNodeIS)){
                    Map<Action, Double> actionDistribution = distribution.getDistributionFor(curNodeIS.getAlgorithmData());
                    double prefix = strategy.get(curNodeIS.getPlayersHistory());
                    if (actionDistribution == null || !(prefix>1e-4)) continue; //unreachable/unreached state
                    for (Map.Entry<Action, Double> en : actionDistribution.entrySet()){
                        if (en.getValue()>0){
                            Sequence sq = new ArrayListSequenceImpl(curNodeIS.getPlayersHistory());
                            sq.addLast(en.getKey());
                            strategy.put(sq, en.getValue()*prefix);
                        }
                    }
                    processed.add(curNodeIS);
                }
                
                for(Node n : curNode.getChildren().values()){
                    if ((n instanceof InnerNode) && n.getDepth()<=cutOffDepth) q.addLast((InnerNode)n);
                }
            }
            return strategy;
        }

    static public Strategy getStrategyFor(GameState rootState, Player player, Distribution distribution, Map<Pair<Integer, Sequence>, MCTSInformationSet> informationSets, Expander expander) {
        Strategy strategy = new UniformStrategyForMissingSequences();
        strategy.put(new ArrayListSequenceImpl(player),1.0);
        HashSet<MCTSInformationSet> processed = new HashSet();
        ArrayDeque<GameState> q = new ArrayDeque();
        q.add(rootState);
        while (!q.isEmpty()){
            GameState curNode = q.removeFirst();
            MCTSInformationSet curNodeIS = informationSets.get(curNode.getISKeyForPlayerToMove());

            if (curNode.getPlayerToMove().equals(player) && !processed.contains(curNodeIS)){
                Map<Action, Double> actionDistribution = distribution.getDistributionFor(curNodeIS.getAlgorithmData());
                double prefix = strategy.get(curNodeIS.getPlayersHistory());
                if (actionDistribution == null || !(prefix>0)) continue; //unreachable/unreached state
                for (Map.Entry<Action, Double> en : actionDistribution.entrySet()){
                    if (en.getValue()>0){
                        Sequence sq = new ArrayListSequenceImpl(curNodeIS.getPlayersHistory());
                        sq.addLast(en.getKey());
                        strategy.put(sq, en.getValue()*prefix);
                    }
                }
                processed.add(curNodeIS);
            }

            List<Action> tmp = (curNodeIS != null) ? ((OOSAlgorithmData)curNodeIS.getAlgorithmData()).getActions() : expander.getActions(curNode);
            for(Action a : tmp) {
                GameState newState = curNode.performAction(a);
                if (!newState.isGameEnd()) q.addLast(newState);
            }
        }
        return strategy;
    }

}
