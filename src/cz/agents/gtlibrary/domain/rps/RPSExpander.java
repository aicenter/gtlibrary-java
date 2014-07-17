package cz.agents.gtlibrary.domain.rps;

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

public class RPSExpander<I extends InformationSet> extends ExpanderImpl<I> {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -2513008286051108758L;

    public RPSExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        RPSGameState gsState = (RPSGameState) gameState;
        List<Action> actions = new ArrayList<Action>();

        addBidsForPlayerToMove(gsState, actions);
        Collections.shuffle(actions, new HighQualityRandom(RPSGameInfo.seed));

        return actions;
    }

    public void addBidsForPlayerToMove(RPSGameState gsState, List<Action> actions) {
        for (int actionValue = 1; actionValue <= 3; actionValue++) {
            actions.add(new RPSAction(actionValue, gsState.getPlayerToMove(), getAlgorithmConfig().getInformationSetFor(gsState)));
        }
    }
}
