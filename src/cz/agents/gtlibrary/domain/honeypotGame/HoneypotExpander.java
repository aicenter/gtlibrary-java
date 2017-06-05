package cz.agents.gtlibrary.domain.honeypotGame;

import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr Tomasek on 29.4.2017.
 */
public class HoneypotExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public HoneypotExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.DEFENDER))
            return getDefenderActions((HoneypotGameState) gameState);
        if (gameState.getPlayerToMove().equals(FlipItGameInfo.ATTACKER))
            return getAttackerActions((HoneypotGameState) gameState);
        return null;
    }

    private List<Action> getDefenderActions(HoneypotGameState gameState) {
        List<Action> actions = new ArrayList<>();

        for (HoneypotGameNode node : gameState.possibleNodes) {
            if (node.value <= gameState.getDefenderBudget()
                    && !gameState.honeypots[node.id - 1]
                    && node.id > gameState.lastDefendedNode)
                actions.add(new HoneypotAction(node, getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));
        }
        actions.add(new HoneypotAction(new HoneypotGameNode(HoneypotGameInfo.NO_ACTION_ID, 0), getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));

        return actions;
    }

    private List<Action> getAttackerActions(HoneypotGameState gameState) {
        List<Action> actions = new ArrayList<>();

        for (HoneypotGameNode node : gameState.possibleNodes) {
            if (!gameState.observedHoneypots[node.id - 1]) {
                actions.add(new HoneypotAction(node, getAlgorithmConfig().getInformationSetFor(gameState), gameState.getPlayerToMove()));
            }
        }

        return actions;
    }
}
