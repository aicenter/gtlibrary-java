package cz.agents.gtlibrary.domain.nonlocality;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class NonLocExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public NonLocExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);
        InformationSet is;
        if (gameState.isPlayerToMoveNature()) is = null;
        else is = getAlgorithmConfig().getInformationSetFor(gameState);
        actions.add(new NonLocAction(is, "L"));
        actions.add(new NonLocAction(is, "R"));
        return actions;
    }
}
