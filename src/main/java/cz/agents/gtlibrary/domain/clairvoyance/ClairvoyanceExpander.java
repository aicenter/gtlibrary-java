package cz.agents.gtlibrary.domain.clairvoyance;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class ClairvoyanceExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public ClairvoyanceExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        ClairvoyanceGameState clairvoyanceGameState = (ClairvoyanceGameState) gameState;

        if (clairvoyanceGameState.getPlayerToMove().equals(ClairvoyanceInfo.FIRST_PLAYER))
            return getP1Actions(clairvoyanceGameState);
        if (clairvoyanceGameState.getPlayerToMove().equals(ClairvoyanceInfo.SECOND_PLAYER))
            return getP2Actions(clairvoyanceGameState);
        return getNatureActions(clairvoyanceGameState);
    }

    private List<Action> getNatureActions(ClairvoyanceGameState state) {
        List<Action> natureActions = new ArrayList<>(2);

        natureActions.add(new NatureAction(getAlgorithmConfig().getInformationSetFor(state), true));
        natureActions.add(new NatureAction(getAlgorithmConfig().getInformationSetFor(state), false));
        return natureActions;
    }

    private List<Action> getP1Actions(ClairvoyanceGameState state) {
        List<Action> p1Actions = new ArrayList<>(ClairvoyanceInfo.betCount);

        for (int i = 0; i < ClairvoyanceInfo.betCount; i++) {
            p1Actions.add(new P1Action(getAlgorithmConfig().getInformationSetFor(state), i));
        }
        return p1Actions;
    }

    private List<Action> getP2Actions(ClairvoyanceGameState state) {
        List<Action> p2Actions = new ArrayList<>(2);

        p2Actions.add(new P2Action(getAlgorithmConfig().getInformationSetFor(state), true));
        p2Actions.add(new P2Action(getAlgorithmConfig().getInformationSetFor(state), false));
        return p2Actions;
    }
}
