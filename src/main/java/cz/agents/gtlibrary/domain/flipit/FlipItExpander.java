package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jakub on 13/03/17.
 */
public class FlipItExpander<I extends InformationSet> extends ExpanderImpl<I> {


    public FlipItExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.DEFENDER))
            return getDefenderActions(gameState);
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.ATTACKER))
            return getAttackerActions(gameState);
        return getRandomActions(gameState);

    }

    protected List<Action> getRandomActions(GameState gameState) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new FlipItAction(FlipItGameInfo.ATTACKER, getAlgorithmConfig().getInformationSetFor(gameState)));
        actions.add(new FlipItAction(FlipItGameInfo.DEFENDER, getAlgorithmConfig().getInformationSetFor(gameState)));
        return actions;
    }

    protected List<Action> getDefenderActions(GameState gameState){
        // get all possible nodes
        List<Action> actions = new ArrayList<Action>();
//        boolean first = true;
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
//            if (first){
//                System.out.println(node);
//                first=false;
//            }
            actions.add(new FlipItAction(node, getAlgorithmConfig().getInformationSetFor(gameState)));
        }
        if (FlipItGameInfo.ENABLE_PASS) actions.add(new FlipItAction(getAlgorithmConfig().getInformationSetFor(gameState)));
        return actions;
    }

    protected List<Action> getAttackerActions(GameState gameState){

        // take only public nodes + nodes accessible from assumed  controlled
        HashSet<Node> nodesToAttack = new HashSet<Node>();
        List<Action> actions = new ArrayList<Action>();
        for (Node parent : FlipItGameInfo.graph.getAllNodes().values() ){//((NodePointsFlipItGameState)gameState).getAttackerPossiblyControlledNodes()){
            if (!((NodePointsFlipItGameState)gameState).isPossiblyOwnedByAttacker(parent)) continue;
            for (Edge edge : FlipItGameInfo.graph.getEdgesOf(parent)){
                nodesToAttack.add(edge.getTarget());
            }
        }
        for(Node node : FlipItGameInfo.graph.getPublicNodes()){
            nodesToAttack.add(node);
        }
        Map<Node, Double> sorted;
        if (nodesToAttack.size() < FlipItGameInfo.graph.getAllNodes().size()) {
            Map<Node, Double> nodes = new HashMap<Node, Double>();
            for (Node node : nodesToAttack) {
                nodes.put(node, FlipItGameInfo.graph.getControlCost(node) / FlipItGameInfo.graph.getReward(node));
//            actions.add(new FlipItAction(node, getAlgorithmConfig().getInformationSetFor(gameState)));
            }
            sorted = sortByValue(nodes);
            nodes = null;
        }
        else{
            sorted = FlipItGameInfo.graph.getSortedNodes();
        }
//        System.out.println("/////");
        for(Map.Entry<Node, Double> entry : sorted.entrySet()) {
//            System.out.println(entry.getKey().getId() + " : " + entry.getValue());
            actions.add(new FlipItAction(entry.getKey(), getAlgorithmConfig().getInformationSetFor(gameState)));
        }
        // NOOP action
        if (FlipItGameInfo.ENABLE_PASS) actions.add(new FlipItAction(getAlgorithmConfig().getInformationSetFor(gameState)));

        sorted = null;
        nodesToAttack = null;
        assert actions.size() <= FlipItGameInfo.graph.getAllNodes().size() + 1;
        return actions;
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
