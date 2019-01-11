package cz.agents.gtlibrary.algorithms.cr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.cr.CRExperiments;
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
import java.util.stream.Collectors;

public class GadgetChanceNode implements ChanceNode, GadgetNode {
    private final GadgetChanceState state;
    private final Random random;
    private final Expander originalExpander;
    private final PublicState ps;

    private Map<Action, GadgetInnerNode> resolvingInnerNodes;
    public static boolean useRootResolving = false;
    public static double rootResolvingEpsilon = 0.;

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

        if (CRExperiments.safeResolving) {
            rootReach = resolvingInnerNodes.keySet().stream()
                    .map(resolvingInnerNodes::get)
                    .map(GadgetInnerNode::getOriginalNode)
                    .map(InnerNode::getReachPrPlayerChance)
                    .reduce(0.0, Double::sum);
        } else {
            rootReach = resolvingInnerNodes.keySet().stream()
                    .map(resolvingInnerNodes::get)
                    .map(GadgetInnerNode::getOriginalNode)
                    .map(InnerNode::getReachPr)
                    .reduce(0.0, Double::sum);
        }

        assert rootReach > 0; // at least one IS must be reachable

        chanceProbabilities = new HashMap<>();
        boolean isRootResolving = ps.getPlayerParentPublicState() == null;
        for (Action action : resolvingInnerNodes.keySet()) {
            GadgetInnerNode node = resolvingInnerNodes.get(action);
//            if (node.getOriginalNode().getReachPrPlayerChance() == 0.) continue;

            double p;
            if(CRExperiments.safeResolving) {
                if (isRootResolving && useRootResolving) {
                    p = (1 - rootResolvingEpsilon) * node.getOriginalNode().getReachPr()
                            + rootResolvingEpsilon * (1. / resolvingInnerNodes.size());
                } else {
                    p = node.getOriginalNode().getReachPrPlayerChance() / rootReach;
                }
            } else {
                p = node.getOriginalNode().getReachPr() / rootReach;
            }
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

                // don't add terminate if aug infoset spans the whole public state
                boolean resolveForAugInfoSetsTerminate = gadgetIs.getAllNodes().size() != ps.getAllNodes().size();
                if (CRExperiments.safeResolving && resolveForAugInfoSetsTerminate) {
                    GadgetInnerAction terminateAction = new GadgetInnerAction(false, gadgetIs);
                    gadgetActions.add(terminateAction);
                }
            } else {
                gadgetActions = gadgetIs.getActions();
            }

            node.createChildren(gadgetActions);
            if (gadgetIs.getAlgorithmData() == null) {
                gadgetIs.setAlgorithmData(new OOSAlgorithmData(gadgetActions, getAlgConfig().useEpsilonRM));
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
        if(actions.size() == 1) return actions.get(0);

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
        return (MCTSConfig) originalExpander.getAlgorithmConfig();
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
    public double getEVWeighted() {
        throw new NotImplementedException();
    }

    @Override
    public void updateEVWeighted(double offPolicyAproxSample) {
        throw new NotImplementedException();
    }

    @Override
    public void setEVWeighted(double offPolicyAproxSample) {
        throw new NotImplementedException();
    }

    @Override
    public double getEVTime(double iterationNum) {
        throw new NotImplementedException();
    }

    @Override
    public void updateEVTime(double offPolicyAproxSample) {
        throw new NotImplementedException();
    }

    @Override
    public void setEVTime(double offPolicyAproxSample) {
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

    @Override
    public void destroy() {
    }

    @Override
    public void setPublicState(MCTSPublicState ps) {
        throw new NotImplementedException();
    }

    private double[] cachedBiasedProbs;
    private double cachedBsum;

    public double getBiasedProbs(double[] biasedProbs, MCTSInformationSet trackingIS, double gadgetEpsilon, double gadgetDelta) {
        if(cachedBiasedProbs == null) {
            int N = getActions().size();
            double p_unif = 1./N;

            // calc probability of action in infoset
            Set<GadgetInnerNode> gins2trackingIS = getChildren().entrySet().stream()
                .filter(entry -> ((GadgetInnerNode) entry.getValue()).getOriginalNode().getInformationSet().equals(trackingIS))
                .map(entry -> (GadgetInnerNode) entry.getValue())
                .collect(Collectors.toSet());
            HashMap<Action, Double> p_infoset = new HashMap<>();
            gins2trackingIS.forEach(gin -> p_infoset.put(gin.getLastAction(), gin.getOriginalNode().getReachPr()));
            double norm_p_infoset = p_infoset.values().stream().reduce(0., Double::sum);

            // calc probability of action in subgame
            HashMap<Action, Double> p_subgame= new HashMap<>();
            resolvingInnerNodes.values().forEach(gin -> p_subgame.put(gin.getLastAction(), gin.getOriginalNode().getReachPr()));
            double norm_p_subgame = p_subgame.values().stream().reduce(0., Double::sum);

            // epsilon-convex of uniform and (delta-convex of infoset and subgame)
            cachedBiasedProbs = new double[actions.size()];
            cachedBsum = 0;
            int i = 0;
            for (Action ai : actions) {
                if (resolvingInnerNodes.get(ai).getOriginalNode().getInformationSet().equals(trackingIS)) {
                    cachedBiasedProbs[i] = gadgetEpsilon*p_unif + (1-gadgetEpsilon)*(
                            gadgetDelta * p_infoset.get(ai) / norm_p_infoset +
                            (1 - gadgetDelta) * p_subgame.get(ai) / norm_p_subgame
                    );
                } else {
                    cachedBiasedProbs[i] = gadgetEpsilon*p_unif + (1-gadgetEpsilon)*(
                            (1 - gadgetDelta) * p_subgame.get(ai) / norm_p_subgame
                    );
                }
                cachedBsum += cachedBiasedProbs[i];
                i++;
            }
            i++;
        }

        System.arraycopy(this.cachedBiasedProbs, 0, biasedProbs, 0, this.cachedBiasedProbs.length);
        return cachedBsum;
    }
}
