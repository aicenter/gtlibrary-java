package cz.agents.gtlibrary.domain.oshizumo;

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

public class OshiZumoExpander<I extends InformationSet> extends ExpanderImpl<I> {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -2513008286051108758L;

    public OshiZumoExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        OshiZumoGameState gsState = (OshiZumoGameState) gameState;
        List<Action> actions = new ArrayList<Action>();

        addBidsForPlayerToMove(gsState, actions);
        Collections.shuffle(actions, new HighQualityRandom(OZGameInfo.seed));

        return actions;
    }

    public void addBidsForPlayerToMove(OshiZumoGameState gsState, List<Action> actions) {
        for (Integer actionValue : gsState.getBidsForPlayerToMove()) {
            actions.add(new OshiZumoAction(actionValue, gsState.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(gsState)));
        }
    }
}
