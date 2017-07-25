package cz.agents.gtlibrary.domain.ir.memoryloss;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class MLExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public MLExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new MLAction(getAlgorithmConfig().getInformationSetFor(gameState), "L" + (((MLGameState)gameState).getRound() + 1)));
        actions.add(new MLAction(getAlgorithmConfig().getInformationSetFor(gameState), "R" + (((MLGameState)gameState).getRound() + 1)));
        return actions;
    }
}
