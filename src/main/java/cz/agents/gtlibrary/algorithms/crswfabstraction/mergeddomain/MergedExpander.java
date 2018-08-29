package cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;
import java.util.Map;

public class MergedExpander<I extends InformationSet> implements Expander<I> {

    private Expander<I> expander;
    private GameInfo gameInfo;

    public MergedExpander(Expander<I> expander) {
        this.expander = expander;
    }

    @Override
    public AlgorithmConfig<I> getAlgorithmConfig() {
        return expander.getAlgorithmConfig();
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        return expander.getActions(((MergedGameState) gameState).getState());
    }

    @Override
    public List<Action> getActionsForUnknownIS(GameState gameState) {
        return expander.getActionsForUnknownIS(((MergedGameState) gameState).getState());
    }

    @Override
    public List<Action> getActions(I informationSet) {
        return expander.getActions(informationSet);
    }

    @Override
    public void setAlgConfig(AlgorithmConfig<I> algConfig) {
        expander.setAlgConfig(algConfig);
    }

    @Override
    public GameInfo getGameInfo() {
        return gameInfo;
    }

    @Override
    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo=gameInfo;
    }
}
