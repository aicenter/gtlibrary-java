package cz.agents.gtlibrary.utils;

import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

public class BasicGameBuilder {
    public static void build(GameState rootState, AlgorithmConfig<? extends InformationSet> algConfig, Expander<? extends InformationSet> expander) {
        ArrayDeque<GameState> queue = new ArrayDeque<>();
        int stateCounter = 0;

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            stateCounter++;
            algConfig.addInformationSetFor(currentState);
            if (currentState.isGameEnd())
                continue;
            queue.addAll(expander.getActions(currentState).stream().map(currentState::performAction).collect(Collectors.toList()));
            if (stateCounter % 100000 == 0) {
                System.out.println("states: " + stateCounter);
                System.out.println("iss: " + algConfig.getAllInformationSets().size());
            }
        }
        System.out.println("State count: " + stateCounter);
    }

    public static void build(InnerNode r) {
        System.err.println("Building complete tree.");
        int nodes = 0, infosets = 0, publicStates = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            if (!(n instanceof ChanceNode)) {
                MCTSInformationSet is = n.getInformationSet();
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                }
            }
            if(!(n instanceof GadgetNode)) {
                MCTSPublicState ps = n.getPublicState();
                if (ps.getAlgorithmData() == null) {
                    publicStates++;
                    ps.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                }
            }
            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        System.err.println("Created nodes: " + nodes + "; infosets: " + infosets + "; public states: " + publicStates);
    }

}
