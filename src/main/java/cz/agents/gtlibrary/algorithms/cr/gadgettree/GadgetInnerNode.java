package cz.agents.gtlibrary.algorithms.cr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.cr.CRExperiments;
import cz.agents.gtlibrary.algorithms.cr.ResolvingMethod;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.rps.RPSGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.agents.gtlibrary.algorithms.cr.ResolvingMethod.RESOLVE_MCCFR;

public class GadgetInnerNode implements InnerNode, GadgetNode {
    private final GadgetInnerState state;
    private final InnerNode originalNode;
    private final int expUtilityIterations;
    private final ResolvingMethod resolvingMethod;
    private MCTSInformationSet informationSet;
    private List<Action> actions;
    private Map<Action, Node> children;
    private InnerNode parent;
    private Action lastAction;
    private GadgetLeafNode terminateNode;

    public GadgetInnerNode(
            GadgetInnerState state,
            InnerNode originalNode,
            int expUtilityIterations,
            ResolvingMethod resolvingMethod) {
        this.state = state;
        this.originalNode = originalNode;
        this.expUtilityIterations = expUtilityIterations;
        this.resolvingMethod = resolvingMethod;
    }

    public void createChildren(List<Action> actions) {
        this.actions = actions;
        GadgetInfoSet gadgetIs = (GadgetInfoSet) getInformationSet();
        GadgetInnerAction followAction = (GadgetInnerAction) actions.get(0); // order is important!

        children = new HashMap<>();
        children.put(followAction, originalNode);

        if(actions.size() == 2) { // has terminate action
            GadgetInnerAction terminateAction = (GadgetInnerAction) actions.get(1);

            double maxIsCFV = getExpander().getGameInfo().getMaxUtility();

            double isCFV = gadgetIs.getIsCFV(resolvingMethod == RESOLVE_MCCFR ? expUtilityIterations : 1);
            double isReach = gadgetIs.getIsReach();

            // shouldnt happen often!
            if (isCFV < -maxIsCFV) {
                isCFV = -maxIsCFV;
//                System.err.println(">>> underflow");
            } else if (isCFV > maxIsCFV) {
                isCFV = maxIsCFV;
//                System.err.println(">>> overflow");
            }
            double u_opponent = isCFV / isReach; // rootReach is multipled by OOSAlgorithm.normalizingUtils
            int playerSign = state.getPlayerToMove().getId() == 0 ? 1 : -1;
            this.terminateNode = new GadgetLeafNode(originalNode.getGameState(), playerSign * u_opponent);
            terminateNode.setParent(this);
            terminateNode.setLastAction(terminateAction);
            children.put(terminateAction, terminateNode);
        }
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
    public double getExpectedValue(double iterationNum) {
        throw new NotImplementedException();
    }

    @Override
    public void updateExpectedValue(double offPolicyAproxSample) {
        throw new NotImplementedException();
    }

    @Override
    public void setExpectedValue(double offPolicyAproxSample) {
        throw new NotImplementedException();
    }

    @Override
    public double getSumReachp() {
        throw new NotImplementedException();
    }

    @Override
    public void updateSumReachp(double currentReachP) {
        throw new NotImplementedException();
    }

    @Override
    public void setSumReachp(double sumReachP) {
        throw new NotImplementedException();
    }


    @Override
    public void resetData() {
        throw new NotImplementedException();
    }

    @Override
    public MCTSConfig getAlgConfig() {
        throw new NotImplementedException();
    }

    private double[] playerReachPr = new double[] {1.,1.,1.};

    @Override
    public double getReachPrPlayerChance() {
        return playerReachPr[getPlayerToMove().getId()] * playerReachPr[2];
    }

    @Override
    public double getPlayerReachPr() {
        return playerReachPr[getPlayerToMove().getId()];
    }

    @Override
    public double getReachPrByPlayer(int player) {
        return playerReachPr[player];
    }

    @Override
    public void setReachPrByPlayer(int player, double meanStrategyPr) {
        playerReachPr[player] = meanStrategyPr;
    }

    @Override
    public double getChanceReachPr() {
        return playerReachPr[2];
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


    private int[] terminateCnt = new int[2];
    private int[] followCnt = new int[2];

    public int getFollowCnt(int playerId) {
        return followCnt[playerId];
    }
    public int getTerminateCnt(int playerId) {
        return terminateCnt[playerId];
    }
    public void incrFollowCnt(int playerId) {
        followCnt[playerId]++;
    }
    public void incrTerminateCnt(int playerId) {
        terminateCnt[playerId]++;
    }

    public void setTerminateCnt(int terminateCnt, int playerId) {
        this.terminateCnt[playerId] = terminateCnt;
    }

    public void setFollowCnt(int followCnt, int playerId) {
        this.followCnt[playerId] = followCnt;
    }

    @Override
    public void setPublicState(MCTSPublicState ps) {
        throw new NotImplementedException();
    }

    @Override
    public void destroy() {
    }
}
