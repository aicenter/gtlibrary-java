package cz.agents.gtlibrary.domain.mpochm;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

public class MPoCHMExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public MPoCHMExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        MPoCHMGameState state = (MPoCHMGameState) gameState;

        if (state.getPlayerToMove().equals(MPoCHMGameInfo.FIRST_PLAYER))
            return getActionsForP1(state);
        return getActionsForP2(state);
    }

    private List<Action> getActionsForP1(MPoCHMGameState state) {
        if (state.getCoinState().equals(MPoCHMGameState.CoinState.NOT_SET))
            return getCoinActions(state);
        return getGiftActions(state);
    }

    private List<Action> getActionsForP2(MPoCHMGameState state) {
        return getCoinActions(state);
    }

    private List<Action> getGiftActions(MPoCHMGameState state) {
        List<Action> actions = new ArrayList<Action>(2);

        actions.add(new GiftAction(MPoCHMGameState.GiftState.GIVEN, getAlgorithmConfig().getInformationSetFor(state)));
        actions.add(new GiftAction(MPoCHMGameState.GiftState.NOT_GIVEN, getAlgorithmConfig().getInformationSetFor(state)));
        return actions;
    }

    public List<Action> getCoinActions(MPoCHMGameState state) {
        List<Action> actions = new ArrayList<Action>(2);

        actions.add(new CoinAction(MPoCHMGameState.CoinState.HEAD, getAlgorithmConfig().getInformationSetFor(state)));
        actions.add(new CoinAction(MPoCHMGameState.CoinState.TAIL, getAlgorithmConfig().getInformationSetFor(state)));
        return actions;
    }
}
