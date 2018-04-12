package cz.agents.gtlibrary.domain.imperfectrecall.brtest;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class BRTestExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public BRTestExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if(gameState.getSequenceFor(BRTestGameInfo.FIRST_PLAYER).size() == 0)
            return getFirstRoundActions(gameState);
        if(gameState.getSequenceFor(BRTestGameInfo.FIRST_PLAYER).size() == 1)
            return getSecondRoundActions(gameState);
        return getThirdRoundActions(gameState);
    }

    public List<Action> getFirstRoundActions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new BRTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "a", 0, 0));
        actions.add(new BRTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "b", 0, 0));
        return actions;
    }

    public List<Action> getSecondRoundActions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new BRTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "c", 0, 0));
        actions.add(new BRTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "d", 0, 0));
        return actions;
    }

    public List<Action> getThirdRoundActions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new BRTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "e", 0, 0));
        actions.add(new BRTestAction(getAlgorithmConfig().getInformationSetFor(gameState), "f", 0, 0));
        return actions;
    }
}
