package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.mccr.gadgettree.*;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.PublicState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SubgameImpl implements Subgame {

    private final PublicState publicState;
    private final MCTSConfig originalConfig;
    private Expander<MCTSInformationSet> expander;

    public SubgameImpl(PublicState publicState,
                       MCTSConfig originalConfig,
                       Expander<MCTSInformationSet> expander) {
        this.publicState = publicState;
        this.originalConfig = originalConfig;
        this.expander = expander;
    }

    @Override
    public PublicState getOriginalRootPublicState() {
        return publicState;
    }

    @Override
    public Set<GameState> getOriginalRootStates() {
        return publicState.getAllStates();
    }

    @Override
    public Set<InnerNode> getOriginalRootNodes() {
        return publicState.getAllNodes();
    }

    @Override
    public Set<MCTSInformationSet> getOriginalRootInformationSets() {
        return publicState.getAllInformationSets();
    }

    @Override
    public GadgetChanceNode getGadgetRoot() {
        // by creating the chance node, we construct the whole gadget game
        GadgetChanceState chanceState = new GadgetChanceState(publicState.getAllNodes().iterator().next().getGameState());
        GadgetChanceNode chanceNode = new GadgetChanceNode(chanceState, expander, originalConfig.getRandom());
        MCTSInformationSet chanceIS = new MCTSInformationSet(chanceState);
        chanceNode.setInformationSet(chanceIS);

        Map<Action, GadgetInnerNode> resolvingInnerNodes = createInnerNodes(chanceIS);
        chanceNode.createChildren(resolvingInnerNodes);
        chanceIS.setAlgorithmData(new OOSAlgorithmData(resolvingInnerNodes.size()));

        return chanceNode;
    }

    private Map<Action, GadgetInnerNode> createInnerNodes(MCTSInformationSet chanceIS) {
        Map<Action, GadgetInnerNode> resolvingInnerNodes = new HashMap<>();
        int idxChanceNode = 0;

        for (MCTSInformationSet informationSet : getOriginalRootInformationSets()) {
            GadgetISKey isKey = new GadgetISKey((PerfectRecallISKey) informationSet.getISKey());
            Iterator<InnerNode> nodeIterator = informationSet.getAllNodes().iterator();

            MCTSInformationSet gadgetIS = null;
            while (nodeIterator.hasNext()) {
                InnerNode origNode = nodeIterator.next();
                GameState origState = origNode.getGameState();
                GadgetInnerState gadgetState = new GadgetInnerState(origState, isKey);

                if (gadgetIS == null) {
                    gadgetIS = new MCTSInformationSet(gadgetState);
                    gadgetIS.setAlgorithmData(new OOSAlgorithmData(2));
                }
                gadgetState.setInformationSet(gadgetIS);

                GadgetInnerNode gadgetNode = new GadgetInnerNode(gadgetState, origNode);
                gadgetNode.setInformationSet(gadgetIS);

                gadgetIS.addNode(gadgetNode);
                gadgetIS.addStateToIS(gadgetState);

                GadgetChanceAction gadgetAction = new GadgetChanceAction(idxChanceNode++, chanceIS);
                resolvingInnerNodes.put(gadgetAction, gadgetNode);
            }
        }

        return resolvingInnerNodes;
    }
}
