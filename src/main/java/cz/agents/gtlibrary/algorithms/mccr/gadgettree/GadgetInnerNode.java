package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GadgetInnerNode implements InnerNode, GadgetNode {
    private final GadgetInnerState state;
    private final InnerNode originalNode;
    private final int iterationsPerGadgetGame;
    private MCTSInformationSet informationSet;
    private List<Action> actions;
    private Map<Action, Node> children;
    private InnerNode parent;
    private Action lastAction;
    private GadgetLeafNode terminateNode;

    public GadgetInnerNode(
            GadgetInnerState state,
            InnerNode originalNode,
            int iterationsPerGadgetGame) {
        this.state = state;
        this.originalNode = originalNode;
        this.iterationsPerGadgetGame = iterationsPerGadgetGame;
    }

    public void createChildren(double rootReach) {
        GadgetInnerAction followAction = new GadgetInnerAction(true, originalNode.getInformationSet());
        GadgetInnerAction terminateAction = new GadgetInnerAction(false, originalNode.getInformationSet());
        actions = new ArrayList<>();
        actions.add(followAction); // order is important!
        actions.add(terminateAction);

        MCTSInformationSet origIs = originalNode.getInformationSet();
        MCTSInformationSet gadgetIs = getInformationSet();
        MCTSPublicState ps = originalNode.getPublicState();
        // todo: which isReach? gadget or orig IS? :/
        double isReach = gadgetIs.getAllNodes().stream()
                .map(in -> ((GadgetInnerNode) in).getOriginalReachPr())
                .reduce(0.0, Double::sum);
//        double isReach = origIs.getAllNodes().stream().map(InnerNode::getReachPr).reduce(0.0, Double::sum);
        assert origIs.getAlgorithmData() != null;

        OOSAlgorithmData data = ((OOSAlgorithmData) origIs.getAlgorithmData());
        double maxIsCFV = getExpander().getGameInfo().getMaxUtility();

        double isCFV;
//        if (data.getIsVisitsCnt() == 0) {
//            isCFV = -maxIsCFV;
//        } else {
//            isCFV = data.getIsCFV();
//        }
        isCFV = 0;
        for(InnerNode in: gadgetIs.getAllNodes()) {
            GadgetInnerNode n = (GadgetInnerNode) in;
            InnerNode o = n.getOriginalNode();
            isCFV += o.getReachPr() * o.getExpectedValue(iterationsPerGadgetGame);
        }

        // shouldnt happen often!
        if (isCFV < -maxIsCFV) {
            isCFV = -maxIsCFV;
//            System.err.println(">>> underflow");
        } else if (isCFV > maxIsCFV) {
            isCFV = maxIsCFV;
//            System.err.println(">>> overflow");
        }

        int playerSign = state.getPlayerToMove().getId() == 0 ? 1 : -1;
        double u = playerSign * isCFV / isReach; // rootReach is multipled by OOSAlgorithm.normalizingUtils
        this.terminateNode = new GadgetLeafNode(originalNode.getGameState(), u);
        terminateNode.setParent(this);
        terminateNode.setLastAction(terminateAction);

        children = new HashMap<>();
        children.put(followAction, originalNode);
        children.put(terminateAction, terminateNode);
    }

    public double getOriginalReachPr() {
        return originalNode.getReachPr();
    }

    @Override
    public Node getChildFor(Action action) {
        return children.get(action);
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<Action> actions) {
        throw new NotImplementedException();
    }

    @Override
    public GameState getGameState() {
        return state;
    }

    @Override
    public Node getChildOrNull(Action action) {
        if (children.containsKey(action)) {
            return children.get(action);
        }
        return null;
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public InnerNode getParent() {
        return parent;
    }

    @Override
    public void setParent(InnerNode parent) {
        this.parent = parent;
    }

    @Override
    public Action getLastAction() {
        return lastAction;
    }

    @Override
    public void setLastAction(Action lastAction) {
        this.lastAction = lastAction;
    }

    @Override
    public AlgorithmData getAlgorithmData() {
        return this.informationSet.getAlgorithmData();
    }

    @Override
    public void setAlgorithmData(AlgorithmData algorithmData) {
        throw new NotImplementedException();
    }

    @Override
    public Expander<MCTSInformationSet> getExpander() {
        return originalNode.getExpander();
    }

    @Override
    public Player getPlayerToMove() {
        return getGameState().getPlayerToMove();
    }

    @Override
    public double getExpectedValue(int iterationNum) {
        throw new NotImplementedException();
    }

    @Override
    public void updateExpectedValue(double offPolicyAproxSample) {
    }

    @Override
    public void resetData() {
        throw new NotImplementedException();
    }

    @Override
    public MCTSConfig getAlgConfig() {
        throw new NotImplementedException();
    }

    @Override
    public double getReachPr() {
        throw new NotImplementedException();
    }

    @Override
    public double getPlayerReachPr() {
        throw new NotImplementedException();
    }

    @Override
    public void setPlayerReachPr(double meanStrategyActionPr) {
        throw new NotImplementedException();
    }

    @Override
    public double getChanceReachPr() {
        throw new NotImplementedException();
    }

    @Override
    public Map<Action, Node> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Map<Action, Node> children) {
        throw new NotImplementedException();
    }

    public InnerNode getFollowNode() {
        return originalNode;
    }

    public GadgetLeafNode getTerminateNode() {
        return terminateNode;
    }

    @Override
    public MCTSInformationSet getInformationSet() {
        return informationSet;
    }

    @Override
    public void setInformationSet(MCTSInformationSet informationSet) {
        this.informationSet = informationSet;
    }

    @Override
    public MCTSPublicState getPublicState() {
        throw new NotImplementedException();
    }

    @Override
    public int hashCode() {
        return originalNode.hashCode() + "gadget".hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InnerNode))
            return false;
        if (!(obj instanceof GadgetInnerNode))
            return false;
        return ((GadgetInnerNode) obj).getOriginalNode().equals(this.originalNode);
    }

    public InnerNode getOriginalNode() {
        return originalNode;
    }

    @Override
    public String toString() {
        return "Gadget PL" + getPlayerToMove().getId() + " - Orig: " + getOriginalNode().toString();
    }
}
