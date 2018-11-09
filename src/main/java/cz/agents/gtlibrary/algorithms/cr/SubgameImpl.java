package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.cr.gadgettree.*;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;
import java.util.stream.Collectors;

public class SubgameImpl implements Subgame {

    private static final int MAX_GADGET_NODES = 1000;
    private final PublicState publicState;
    private final HashMap<GadgetISKey, GadgetInfoSet> gadgetISs;
    private final Set<GadgetInnerNode> gadgetNodes;
    private final GadgetChanceNode gadgetRoot;


    public SubgameImpl(PublicState publicState,
                       MCTSConfig originalConfig,
                       Expander<MCTSInformationSet> expander,
                       MCTSInformationSet currentIs) {
        this.publicState = publicState;
        this.gadgetISs = new HashMap<>();

        // by creating the chance node, we construct the whole gadget game
        this.gadgetRoot = createRoot(expander, originalConfig.getRandom());
        Map<Action, GadgetInnerNode> resolvingInnerNodes = createInnerNodes(currentIs);
        gadgetNodes = new HashSet<>(resolvingInnerNodes.values());
        gadgetRoot.createChildren(resolvingInnerNodes);
    }

    private GadgetChanceNode createRoot(Expander expander, Random rnd) {
        GadgetChanceState chanceState = new GadgetChanceState(
                publicState.getAllNodes().iterator().next().getGameState());
        return new GadgetChanceNode(chanceState, expander, rnd, publicState);
    }

    private Map<Action, GadgetInnerNode> createInnerNodes(MCTSInformationSet currentIs) {
        Map<Action, GadgetInnerNode> resolvingInnerNodes = new HashMap<>();
        int idxChanceNode = 0;

        List<InnerNode> topMostOriginalNodes = new ArrayList<>(getTopMostOriginalNodes());

        double tmpRootReach = topMostOriginalNodes.stream()
                .map(InnerNode::getReachPrPlayerChance)
                .reduce(0.0, Double::sum);

        if(topMostOriginalNodes.size() > MAX_GADGET_NODES) {
            Comparator<InnerNode> reachPrComparator = Comparator
                    .comparing((InnerNode n)->n.getInformationSet().equals(currentIs))
                    .thenComparing(InnerNode::getReachPrPlayerChance);
            topMostOriginalNodes.sort(reachPrComparator.reversed());
        }

        for (InnerNode origNode : topMostOriginalNodes) {
            // filter out nodes that have small chance probability and are not leading to current IS (if it is known)
            if(
                !origNode.getInformationSet().equals(currentIs) &&
                origNode.getReachPrPlayerChance() / tmpRootReach < (1e-4 / publicState.getAllNodes().size())) continue;

            GadgetISKey isKey = new GadgetISKey(origNode.getOpponentAugISKey());
            GameState origState = origNode.getGameState();
            GadgetInnerState gadgetState = new GadgetInnerState(origState, isKey);

            MCTSInformationSet gadgetIS = getGadgetIS(isKey, gadgetState);
            gadgetState.setInformationSet(gadgetIS);

            GadgetInnerNode gadgetNode = new GadgetInnerNode(gadgetState, origNode,
                    publicState.getResolvingIterations(), publicState.getResolvingMethod());
            gadgetNode.setInformationSet(gadgetIS);

            gadgetIS.addNode(gadgetNode);
            gadgetIS.addStateToIS(gadgetState);

            GadgetChanceAction gadgetAction = new GadgetChanceAction(idxChanceNode++);
            resolvingInnerNodes.put(gadgetAction, gadgetNode);

            if(resolvingInnerNodes.size() >= MAX_GADGET_NODES) break; // limit gadgets to max 1000 nodes
        }

        return resolvingInnerNodes;
    }

    @Override
    public PublicState getPublicState() {
        return publicState;
    }

    @Override
    public Set<GameState> getOriginalStates() {
        return publicState.getAllStates();
    }

    @Override
    public Set<InnerNode> getOriginalNodes() {
        return publicState.getAllNodes();
    }

    @Override
    public Set<MCTSInformationSet> getOriginalInformationSets() {
        return publicState.getAllInformationSets();
    }

    @Override
    public Set<GadgetInfoSet> getGadgetInformationSets() {
        return new HashSet<>(gadgetISs.values());
    }

    @Override
    public Set<GadgetInnerNode> getGadgetNodes() {
        return gadgetNodes;
    }

    @Override
    public Set<GadgetInnerState> getGadgetStates() {
        return gadgetNodes.stream()
                .map(in -> (GadgetInnerState) in.getGameState())
                .collect(Collectors.toSet());
    }

    @Override
    public GadgetChanceNode getGadgetRoot() {
        return this.gadgetRoot;
    }

    private GadgetInfoSet getGadgetIS(GadgetISKey gadgetISKey, GadgetInnerState state) {
        if (gadgetISs.containsKey(gadgetISKey)) {
            return gadgetISs.get(gadgetISKey);
        }
        GadgetInfoSet informationSet = new GadgetInfoSet(state, gadgetISKey);
        gadgetISs.put(gadgetISKey, informationSet);
        return informationSet;
    }

    private Set<InnerNode> getTopMostOriginalNodes() {
        return publicState.getTopMostOriginalNodes();
    }
}
