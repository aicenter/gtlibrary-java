package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.mccr.gadgettree.*;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.PublicState;

import java.util.*;

public class SubgameImpl implements Subgame {

    private final PublicState publicState;
    private final MCTSConfig originalConfig;
    private final HashMap<GadgetISKey, GadgetInfoSet> gadgetISs;
    private final int iterationsPerGadgetGame;
    private Expander<MCTSInformationSet> expander;

    public SubgameImpl(PublicState publicState,
                       MCTSConfig originalConfig,
                       Expander<MCTSInformationSet> expander,
                       int iterationsPerGadgetGame) {
        this.publicState = publicState;
        this.originalConfig = originalConfig;
        this.expander = expander;
        this.iterationsPerGadgetGame = iterationsPerGadgetGame;

        this.gadgetISs = new HashMap<>();
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

        Map<Action, GadgetInnerNode> resolvingInnerNodes = createInnerNodes();
        chanceNode.createChildren(resolvingInnerNodes);

        return chanceNode;
    }

    private Map<Action, GadgetInnerNode> createInnerNodes() {
        Map<Action, GadgetInnerNode> resolvingInnerNodes = new HashMap<>();
        int idxChanceNode = 0;

        for (MCTSInformationSet informationSet : getOriginalRootInformationSets()) {
            for (InnerNode origNode : informationSet.getAllNodes()) {
                GadgetISKey isKey = new GadgetISKey(origNode.getOpponentAugISKey());
                GameState origState = origNode.getGameState();
                GadgetInnerState gadgetState = new GadgetInnerState(origState, isKey);

                MCTSInformationSet gadgetIS = getGadgetIS(isKey, gadgetState);
                gadgetState.setInformationSet(gadgetIS);

                GadgetInnerNode gadgetNode = new GadgetInnerNode(gadgetState, origNode, iterationsPerGadgetGame);
                gadgetNode.setInformationSet(gadgetIS);

                gadgetIS.addNode(gadgetNode);
                gadgetIS.addStateToIS(gadgetState);

                GadgetChanceAction gadgetAction = new GadgetChanceAction(idxChanceNode++);
                resolvingInnerNodes.put(gadgetAction, gadgetNode);
            }
        }

        return resolvingInnerNodes;
    }

    private GadgetInfoSet getGadgetIS(GadgetISKey gadgetISKey, GadgetInnerState state) {
        if(gadgetISs.containsKey(gadgetISKey)) {
            return gadgetISs.get(gadgetISKey);
        }
        GadgetInfoSet informationSet = new GadgetInfoSet(state, gadgetISKey);
        informationSet.setAlgorithmData(new OOSAlgorithmData(2));
        gadgetISs.put(gadgetISKey, informationSet);
        return informationSet;
    }

    @Override
    public void resetData() {
        int infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.addAll(publicState.getAllNodes());
//        for (InnerNode node : publicState.getAllNodes()) {
//            for (Map.Entry<Action, Node> entry : node.getChildren().entrySet()) {
//                Node ch = entry.getValue();
//                if (ch instanceof InnerNode) {
//                    q.add((InnerNode) ch);
//                }
//            }
//        }
        while (!q.isEmpty()) {
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if(is != null && is.getAlgorithmData() != null) {
                OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();
                data.resetData();
            }

            for (Map.Entry<Action, Node> entry : n.getChildren().entrySet()) {
                Node ch = entry.getValue();
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
    }
}
