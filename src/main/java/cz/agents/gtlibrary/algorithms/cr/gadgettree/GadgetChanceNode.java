package cz.agents.gtlibrary.algorithms.cr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

public class GadgetChanceNode implements ChanceNode, GadgetNode {
    private final GadgetChanceState state;
    private final Random random;
    private final Expander originalExpander;
    private final PublicState ps;

    private Map<Action, GadgetInnerNode> resolvingInnerNodes;

    public Map<Action, Double> getChanceProbabilities() {
        return chanceProbabilities;
    }

    public Map<Action, GadgetInnerNode> getResolvingInnerNodes() {
        return resolvingInnerNodes;
    }

    private Map<Action, Double> chanceProbabilities;
    private List<Action> actions;
    private Double rootReach;

    public GadgetChanceNode(
            GadgetChanceState chanceState,
            Expander originalExpander,
            Random random,
            PublicState ps) {
        this.state = chanceState;
        this.originalExpander = originalExpander;
        this.random = random;
        this.ps = ps;
    }

    public void createChildren(Map<Action, GadgetInnerNode> allResolvingInnerNodes) {
        resolvingInnerNodes = allResolvingInnerNodes;

        actions = new ArrayList<>();

        rootReach = resolvingInnerNodes.keySet().stream()
                .map(resolvingInnerNodes::get)
                .map(GadgetInnerNode::getOriginalNode)
                .map(InnerNode::getReachPrPlayerChance)
                .reduce(0.0, Double::sum);

        assert rootReach > 0; // at least one IS must be reachable

        chanceProbabilities = new HashMap<>();
        for (Action action : resolvingInnerNodes.keySet()) {
            GadgetInnerNode node = resolvingInnerNodes.get(action);
//            if (node.getOriginalNode().getReachPrPlayerChance() == 0.) continue;

            double p = node.getOriginalNode().getReachPrPlayerChance() / rootReach;
            assert p <= 1 && p >= 0;
            chanceProbabilities.put(action, p);
            actions.add(action);

            node.setParent(this);
            node.setLastAction(action);
            node.setReachPrByPlayer(2, p);

            MCTSInformationSet gadgetIs = node.getInformationSet();
            List<Action> gadgetActions;
            if(gadgetIs.getActions() == null) {
                gadgetActions = new ArrayList<>();
                GadgetInnerAction followAction = new GadgetInnerAction(true, gadgetIs);
                gadgetActions.add(followAction); // order is important!
                boolean resolveForAugInfoSetsTerminate = gadgetIs.getAllNodes().size() != ps.getAllNodes().size();
                if (resolveForAugInfoSetsTerminate) {
                    GadgetInnerAction terminateAction = new GadgetInnerAction(false, gadgetIs);
                    gadgetActions.add(terminateAction);
                }
            } else {
                gadgetActions = gadgetIs.getActions();
            }

            node.createChildren(gadgetActions);
            if (gadgetIs.getAlgorithmData() == null) {
                gadgetIs.setAlgorithmData(new OOSAlgorithmData(gadgetActions));
            }

            assert gadgetIs.getAllNodes().size() != ps.getAllNodes().size() ||
                    resolvingInnerNodes.values().stream()
                            .map(GadgetInnerNode::getInformationSet)
                            .filter(is -> !is.equals(gadgetIs)).count() == 0;
        }

        state.setChanceProbabilities(chanceProbabilities);
    }

    public Double getRootReachPr() {
        return rootReach;
    }

    @Override
    public Action getRandomAction() {
        double move = random.nextDouble();

        for (Action action : actions) {
            move -= getProbabilityOfNatureFor(action);
            if (move < 0) {
                return action;
            }
        }
        return actions.get(actions.size() - 1);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        Double prob = chanceProbabilities.get(action);
        if (prob == null) {
            throw new NullPointerException();
        }
        return prob;
    }

    @Override
    public Node getChildOrNull(Action action) {
        return (Node) resolvingInnerNodes.get(action);
    }

    @Override
    public Node getChildFor(Action action) {
        Node selected = (Node) resolvingInnerNodes.get(action);

        if (selected == null) {
            throw new RuntimeException("All children should exist for resolving chance node");
        }
        return selected;
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
    public MCTSInformationSet getInformationSet() {
        return null;
    }

    @Override
    public void setInformationSet(MCTSInformationSet informationSet) {
        throw new NotImplementedException();
    }

    @Override
    public Map<Action, Node> getChildren() {
        return (Map) this.resolvingInnerNodes;
    }

    @Override
    public void setChildren(Map<Action, Node> children) {
        throw new NotImplementedException();
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public Expander<MCTSInformationSet> getExpander() {
        return originalExpander;
    }

    @Override
    public MCTSConfig getAlgConfig() {
        return null;
    }

    @Override
    public InnerNode getParent() {
        return null;
    }

    @Override
    public void setParent(InnerNode parent) {
        throw new NotImplementedException();
    }

    @Override
    public Action getLastAction() {
        return null;
    }

    @Override
    public void setLastAction(Action lastAction) {
        throw new NotImplementedException();
    }

    @Override
    public AlgorithmData getAlgorithmData() {
        return null;
    }

    @Override
    public void setAlgorithmData(AlgorithmData algorithmData) {
        throw new NotImplementedException();
    }

    @Override
    public MCTSPublicState getPublicState() {
        throw new NotImplementedException();
    }

    @Override
    public double getPlayerReachPr() {
        throw new NotImplementedException();
    }

    @Override
    public double getReachPrByPlayer(int player) {
        throw new NotImplementedException();
    }

    @Override
    public void setReachPrByPlayer(int player, double meanStrategyPr) {
        throw new NotImplementedException();
    }

    @Override
    public double getChanceReachPr() {
        throw new NotImplementedException();
    }

    @Override
    public double getExpectedValue(int iterationNum) {
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
    public void resetData() {
        throw new NotImplementedException();
    }

    @Override
    public GameState getGameState() {
        return this.state;
    }

    public void setResolvingInnerNodes(Map<Action, GadgetInnerNode> resolvingInnerNodes) {
        this.resolvingInnerNodes = resolvingInnerNodes;
    }

    public void setChanceProbabilities(Map<Action, Double> chanceProbabilities) {
        this.chanceProbabilities = chanceProbabilities;
    }

    @Override
    public String toString() {
        GadgetInnerNode aNode = resolvingInnerNodes.values().iterator().next();

        return "GadgetChance PL" + aNode.getPlayerToMove().getId() + " " + aNode.getOriginalNode().getDepth();
    }
}
