package cz.agents.gtlibrary.algorithms.mcts.behavioral;

import cz.agents.gtlibrary.algorithms.mcts.SMMCTSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.Simulator;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMSelector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.Pair;

/**
 * Created by Jakub Cerny on 05/09/2018.
 */
public class DepthLimitedSMMCTSAlgorithm extends SMMCTSAlgorithm {

    protected int maxDepth;

    public DepthLimitedSMMCTSAlgorithm(Player searchingPlayer, Simulator simulator, SMBackPropFactory fact, GameState rootState, Expander expander, int maxDepth) {
        super(searchingPlayer, simulator, fact, rootState, expander);
        this.maxDepth = maxDepth;
    }

    @Override
    protected double iteration(Node node) {
        if (node instanceof LeafNode) {
            return ((LeafNode) node).getUtilities()[0];
        } else {
            InnerNode n = (InnerNode) node;
            double retValue = 0;
            Pair<Integer, Integer> selActionIdxs;
            Action selAction = null;
            SMSelector selector = null;

            if (node instanceof ChanceNode) {
                selAction = ((ChanceNode) node).getRandomAction();
                Node child = n.getChildFor(selAction);
                return iteration(child);
            }

            assert n.getInformationSet().getAllNodes().size() == 1;
            selector = (SMSelector) n.getInformationSet().getAlgorithmData();
            if (selector != null) {
//                System.out.println(selector.getClass().getSimpleName() + " / " + n.getGameState().getPlayerToMove().getId());
                selActionIdxs = selector.select();
                selAction = n.getActions().get(selActionIdxs.getLeft());
                InnerNode bottom = (InnerNode) n.getChildFor(selAction);
                Node child = bottom.getChildFor(bottom.getActions().get(selActionIdxs.getRight()));
                if (n.getDepth() == maxDepth) {
                    retValue = simulator.simulate(n.getGameState())[0];
                }
                else
                    retValue = iteration(child);
            } else {
                expandNode(n);
                selector = (SMSelector) n.getInformationSet().getAlgorithmData();
//                System.out.println(selector.getClass().getSimpleName() + " / " + n.getInformationSet().getPlayer().getId());
                selActionIdxs = selector.select();
                selAction = n.getActions().get(selActionIdxs.getLeft());
                InnerNode bottom = (InnerNode) n.getChildFor(selAction);
                Node child = bottom.getChildFor(bottom.getActions().get(selActionIdxs.getRight()));
                retValue = simulator.simulate(child.getGameState())[0];
            }
            selector.update(selActionIdxs, retValue);
            return retValue;
        }
    }
}
