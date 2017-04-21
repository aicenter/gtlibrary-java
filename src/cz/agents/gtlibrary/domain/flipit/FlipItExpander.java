package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
        for (Node node : FlipItGameInfo.graph.getAllNodes().values()){
            actions.add(new FlipItAction(node, getAlgorithmConfig().getInformationSetFor(gameState)));
        }
        actions.add(new FlipItAction(getAlgorithmConfig().getInformationSetFor(gameState)));
        return actions;
    }

    protected List<Action> getAttackerActions(GameState gameState){

//        System.out.println("Expander : attacker");
        // take only public nodes + nodes accessible from last known controlled
        HashSet<Node> nodesToAttack = new HashSet<Node>();
        List<Action> actions = new ArrayList<Action>();
        for (Node parent : ((FlipItGameState)gameState).getAttackerPossiblyControlledNodes()){
//            System.out.println("------> adding possibly controlled node");
            for (Edge edge : FlipItGameInfo.graph.getEdgesOf(parent)){
//                System.out.println("------> adding possibly controlled node");
                nodesToAttack.add(edge.getTarget());
            }
        }
        for(Node node : FlipItGameInfo.graph.getPublicNodes()){
            nodesToAttack.add(node);
        }
        for (Node node : nodesToAttack){
            actions.add(new FlipItAction(node, getAlgorithmConfig().getInformationSetFor(gameState)));
        }
        actions.add(new FlipItAction(getAlgorithmConfig().getInformationSetFor(gameState)));
//        System.out.println("att " + actions.size());
        return actions;
    }
}
