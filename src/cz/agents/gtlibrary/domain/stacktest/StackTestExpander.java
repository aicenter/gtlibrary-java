package cz.agents.gtlibrary.domain.stacktest;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class StackTestExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public StackTestExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if(gameState.getPlayerToMove().equals(StackTestGameInfo.FIRST_PLAYER))
            return getP1Actions(gameState);
        return getP2Actions(gameState);
    }

    private List<Action> getP2Actions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new StackTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "L2"));
        actions.add(new StackTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "R2"));
        return actions;

    }

    private List<Action> getP1Actions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new StackTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "L1"));
        actions.add(new StackTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "R1"));
        return actions;
    }
}
