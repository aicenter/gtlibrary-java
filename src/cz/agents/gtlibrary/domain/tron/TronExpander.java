package cz.agents.gtlibrary.domain.tron;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.HighQualityRandom;

public class TronExpander<I extends InformationSet> extends ExpanderImpl<I> {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -2513008286051108758L;

    public TronExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        TronGameState gsState = (TronGameState) gameState;
        List<Action> actions = new ArrayList<Action>();

        addActionsForPlayerToMove(gsState, actions);
        Collections.shuffle(actions, new HighQualityRandom(TronGameInfo.seed));

        return actions;
    }

    public void addActionsForPlayerToMove(TronGameState gsState, List<Action> actions) {
        for (Integer actionValue : gsState.getActionsForPlayerToMove()) {
            actions.add(new TronAction(actionValue, gsState.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(gsState)));
        }
    }
}
