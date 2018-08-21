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

    private static final double CFV_NOT_VISITED_VALUE = -100000;
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

        MCTSInformationSet is = originalNode.getInformationSet();
        double isReach = is.getAllNodes().stream().map(InnerNode::getReachPr).reduce(0.0, Double::sum);
        if(1.0 - isReach < 1e-9) isReach = 1.0; // sometimes sums can not sum up to 1

        assert is.getAlgorithmData() != null;
        OOSAlgorithmData data = ((OOSAlgorithmData) is.getAlgorithmData());
        double isCFV;
        if(data.getIsVisitsCnt() == 0) {
            isCFV = CFV_NOT_VISITED_VALUE;
            System.err.println(">>> CFV!");
        } else {
            isCFV = data.getIsCFV(iterationsPerGadgetGame);
        }

        int playerSign = getPlayerToMove().getId() == 0 ? 1 : -1;


        double u = playerSign * rootReach * isCFV / isReach;
        if(originalNode.getDepth() == 0) {
            u = 0.3235250255823229;
        }
        if(originalNode.getDepth() == 1) {
            u = 0.9558721200504063;
        }
        this.terminateNode = new GadgetLeafNode(
                originalNode.getGameState(), u);
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

    public List<Action> getOriginalActions() {
        return originalNode.getActions();
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
        throw new NotImplementedException();
    }

    @Override
    public Player getPlayerToMove() {
        return getGameState().getPlayerToMove();
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

    public InnerNode getFollowNode() {
        return originalNode;
    }

    public GadgetLeafNode getTerminateNode() {
        return terminateNode;
    }


    @Override
    public void setChildren(Map<Action, Node> children) {
        throw new NotImplementedException();
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
        return "Gadget PL"+getPlayerToMove().getId() + " - Orig: "+getOriginalNode().toString();
    }
}
