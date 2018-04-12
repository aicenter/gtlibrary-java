package cz.agents.gtlibrary.domain.wichardtne;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WichardtExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public WichardtExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        int round = ((WichardtGameState) gameState).getRound();

        if (round == 0)
            return getFirstRoundActions(getAlgorithmConfig().getInformationSetFor(gameState));
        if (round == 1)
            return getSecondRoundActions(getAlgorithmConfig().getInformationSetFor(gameState));
        if (round == 2)
            return getThirdRoundActions(getAlgorithmConfig().getInformationSetFor(gameState));
        return Collections.emptyList();
    }

    private List<Action> getFirstRoundActions(InformationSet informationSet) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new WichardtAction("l1", informationSet));
        actions.add(new WichardtAction("r1", informationSet));
        return actions;
    }

    private List<Action> getSecondRoundActions(InformationSet informationSet) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new WichardtAction("l2", informationSet));
        actions.add(new WichardtAction("r2", informationSet));
        return actions;
    }

    private List<Action> getThirdRoundActions(InformationSet informationSet) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new WichardtAction("L", informationSet));
        actions.add(new WichardtAction("R", informationSet));
        return actions;
    }
}
