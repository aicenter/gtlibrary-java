package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.*;

public class GadgetChanceNode implements ChanceNode, GadgetNode {
    private final GadgetChanceState state;
    private final Random random;
    private final Expander<MCTSInformationSet> originalExpander;

    private Map<Action, GadgetInnerNode> resolvingInnerNodes;
    private Map<Action, Double> chanceProbabilities;
    private List<Action> actions;
    private MCTSInformationSet informationSet;
    private Double rootReach;

    public GadgetChanceNode(GadgetChanceState chanceState, Expander<MCTSInformationSet> originalExpander, Random random) {
        this.state = chanceState;
        this.originalExpander = originalExpander;
        this.random = random;
    }

    public void createChildren(Map<Action, GadgetInnerNode> resolvingInnerNodes) {
        this.resolvingInnerNodes = resolvingInnerNodes;

        actions = new ArrayList<>();

        rootReach = resolvingInnerNodes.keySet().stream()
                .map(resolvingInnerNodes::get)
                .map(GadgetInnerNode::getOriginalReachPr)
                .reduce(0.0, Double::sum);

        assert rootReach > 0; // at least IS must be reachable

        chanceProbabilities = new HashMap<>();
        for(Action action: resolvingInnerNodes.keySet()) {
            GadgetInnerNode node = resolvingInnerNodes.get(action);
            if(node.getOriginalReachPr() == 0.) continue;

            chanceProbabilities.put(action, node.getOriginalReachPr() / rootReach);
            actions.add(action);

            node.setParent(this);
            node.setLastAction(action);
            node.createChildren(rootReach);
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
        if(prob == null) {
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
    public MCTSInformationSet getInformationSet() {
        return informationSet;
    }

    @Override
    public void setInformationSet(MCTSInformationSet informationSet) {
        this.informationSet = informationSet;
    }

    @Override
    public Map<Action, Node> getChildren() {
        return (Map) this.resolvingInnerNodes;
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
        throw new NotImplementedException();
    }

    @Override
    public Action getLastAction() {
        return null;
    }

    @Override
    public AlgorithmData getAlgorithmData() {
        return null;
    }

    @Override
    public MCTSPublicState getPublicState() {
        throw new NotImplementedException();
    }

    @Override
    public void setChildren(Map<Action, Node> children) {
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
    public void setActions(List<Action> actions) {
        throw new NotImplementedException();
    }

    @Override
    public GameState getGameState() {
        return this.state;
    }

    @Override
    public void setParent(InnerNode parent) {
        throw new NotImplementedException();
    }

    @Override
    public void setLastAction(Action lastAction) {
        throw new NotImplementedException();
    }

    @Override
    public void setAlgorithmData(AlgorithmData algorithmData) {
        throw new NotImplementedException();
    }

    public void setResolvingInnerNodes(Map<Action, GadgetInnerNode> resolvingInnerNodes) {
        this.resolvingInnerNodes = resolvingInnerNodes;
    }

    public void setChanceProbabilities(Map<Action, Double> chanceProbabilities) {
        this.chanceProbabilities = chanceProbabilities;
    }


}
