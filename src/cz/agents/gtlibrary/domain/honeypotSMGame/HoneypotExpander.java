package cz.agents.gtlibrary.domain.honeypotSMGame;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HoneypotExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public HoneypotExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().equals(HoneypotGameInfo.DEFENDER))
            return getDefenderActions((HoneypotGameState) gameState);
        if (gameState.getPlayerToMove().equals(HoneypotGameInfo.ATTACKER))
            return getAttackerActions((HoneypotGameState) gameState);
        return null;
    }

    private List<Action> getDefenderActions(HoneypotGameState gameState) {
        I is = getAlgorithmConfig().getInformationSetFor(gameState);
        List<Action> actions = new ArrayList<>();
        for(HashSet<HoneypotGameNode> action : HoneypotGameInfo.defenderActions){
            actions.add(new HoneypotDefenderAction(action,
                        is, gameState.getPlayerToMove()));
        }
//        HashSet<HoneypotGameNode> nodes = new HashSet<>();
//        int n = HoneypotGameInfo.allNodes.length;
//        for (int i = 0; i < (1<<n); i++) {
//            nodes.clear();
//            for (int j = 0; j < n; j++) {
//                if ((i & (1 << j)) > 0) {
//                    nodes.add(HoneypotGameInfo.allNodes[j]);
//                }
//            }
//            if(nodes.isEmpty()) continue;
//            double sum = 0.0;
//            for (HoneypotGameNode node : nodes){
//                sum += node.defendCost;
//                if(sum > HoneypotGameInfo.initialDefenderBudget)
//                    break;
//            }
//
//            if(sum <= HoneypotGameInfo.initialDefenderBudget && isMaximalSet(nodes, sum)){
//                actions.add(new HoneypotDefenderAction(nodes,
//                        is, gameState.getPlayerToMove()));
//            }
//        }

//        System.out.println(actions.size());

//        actions.add(new HoneypotAttackerAction(new HoneypotGameNode(HoneypotGameInfo.NO_ACTION_ID, 0, 0, 0),
//                getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));
        return actions;
}

    protected boolean isMaximalSet(HashSet<HoneypotGameNode> nodes, double sum){
        for(HoneypotGameNode node : HoneypotGameInfo.allNodes){
            if(!nodes.contains(node) && sum + node.defendCost <= HoneypotGameInfo.initialDefenderBudget)
                return false;
        }
        return true;
    }



    private List<Action> getAttackerActions(HoneypotGameState gameState) {
        List<Action> actions = new ArrayList<>();
        I is = getAlgorithmConfig().getInformationSetFor(gameState);

        for (HoneypotGameNode node : HoneypotGameInfo.allNodes) {
            actions.add(new HoneypotAttackerAction(node, is, gameState.getPlayerToMove()));
        }

        // attacker can also pass -> then the game ends
        if (HoneypotGameInfo.ENABLE_PASS)
            actions.add(new HoneypotAttackerAction(new HoneypotGameNode(HoneypotGameInfo.NO_ACTION_ID, 0, 0, 0), is, gameState.getPlayerToMove()));

        return actions;
    }


}
