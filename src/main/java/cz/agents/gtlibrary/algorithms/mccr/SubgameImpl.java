package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.mccr.gadgettree.*;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;
import java.util.stream.Collectors;

public class SubgameImpl implements Subgame {

    private final PublicState publicState;
    private final MCTSConfig originalConfig;
    private final HashMap<GadgetISKey, GadgetInfoSet> gadgetISs;
    private final int expUtilityIterations;
    private Expander<MCTSInformationSet> expander;

    public SubgameImpl(PublicState publicState,
                       MCTSConfig originalConfig,
                       Expander<MCTSInformationSet> expander,
                       int expUtilityIterations) {
        this.publicState = publicState;
        this.originalConfig = originalConfig;
        this.expander = expander;
        this.expUtilityIterations = expUtilityIterations;

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
        GadgetChanceState chanceState = new GadgetChanceState(
                publicState.getAllNodes().iterator().next().getGameState());
        GadgetChanceNode chanceNode = new GadgetChanceNode(
                chanceState, expander, originalConfig.getRandom(), publicState);

        Map<Action, GadgetInnerNode> resolvingInnerNodes = createInnerNodes();
        chanceNode.createChildren(resolvingInnerNodes);

        return chanceNode;
    }

    private Map<Action, GadgetInnerNode> createInnerNodes() {
        Map<Action, GadgetInnerNode> resolvingInnerNodes = new HashMap<>();
        int idxChanceNode = 0;

        for (InnerNode origNode : getTopMostOriginalNodes()) {
            GadgetISKey isKey = new GadgetISKey(origNode.getOpponentAugISKey());
            GameState origState = origNode.getGameState();
            GadgetInnerState gadgetState = new GadgetInnerState(origState, isKey);

            MCTSInformationSet gadgetIS = getGadgetIS(isKey, gadgetState);
            gadgetState.setInformationSet(gadgetIS);

            GadgetInnerNode gadgetNode = new GadgetInnerNode(gadgetState, origNode, expUtilityIterations);
            gadgetNode.setInformationSet(gadgetIS);

            gadgetIS.addNode(gadgetNode);
            gadgetIS.addStateToIS(gadgetState);

            GadgetChanceAction gadgetAction = new GadgetChanceAction(idxChanceNode++);
            resolvingInnerNodes.put(gadgetAction, gadgetNode);
        }

        return resolvingInnerNodes;
    }

    private GadgetInfoSet getGadgetIS(GadgetISKey gadgetISKey, GadgetInnerState state) {
        if (gadgetISs.containsKey(gadgetISKey)) {
            return gadgetISs.get(gadgetISKey);
        }
        GadgetInfoSet informationSet = new GadgetInfoSet(state, gadgetISKey);
        gadgetISs.put(gadgetISKey, informationSet);
        return informationSet;
    }

    @Override
    public void resetData(Player resettingPlayer, Set<PublicState> doNotResetAtPlayerPs) {
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.addAll(publicState.getAllNodes());

        while (!q.isEmpty()) {
            InnerNode n = q.removeFirst();

            if(n.getPlayerToMove().equals(resettingPlayer) && !doNotResetAtPlayerPs.contains(n.getPublicState())) {
//                System.err.println("Reset at "+n.getPublicState());
                n.resetData();

                MCTSInformationSet is = n.getInformationSet();
                if (is != null && is.getAlgorithmData() != null) {
                    OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();
                    data.resetData();
                }
            }

            // update all of existing tree
            for (Map.Entry<Action, Node> entry : n.getChildren().entrySet()) {
                Node ch = entry.getValue();
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
    }

    @Override
    public void resetData() {
        ArrayDeque<Node> q = new ArrayDeque<Node>();
        q.addAll(publicState.getAllNodes());

        while (!q.isEmpty()) {
            Node n = q.removeFirst();
            if(n instanceof LeafNode) continue;;
            InnerNode in = (InnerNode) n;
            in.resetData();

            MCTSInformationSet is = in.getInformationSet();
            if (is != null && is.getAlgorithmData() != null) {
                OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();
                data.resetData();
            }

            for (Map.Entry<Action, Node> entry : in.getChildren().entrySet()) {
                Node ch = entry.getValue();
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
    }


    private Set<InnerNode> getTopMostOriginalNodes() {
        Set<InnerNode> originalNodes = new HashSet<>();
        getOriginalRootInformationSets().stream()
                .map(MCTSInformationSet::getAllNodes)
                .forEach(originalNodes::addAll);

        return originalNodes.stream()
                .filter(n -> !originalNodes.contains(n.getParent()))
                .collect(Collectors.toSet());
    }
}
