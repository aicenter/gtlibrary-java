package cz.agents.gtlibrary.domain.observationGame;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bbosansky on 11/3/17.
 */
public class ObsGameExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public ObsGameExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().equals(ObsGameInfo.FOLLOWER))
            return getFollowerActions((ObsGameState) gameState);
        return getLeaderActions((ObsGameState) gameState);
    }

    private List<Action> getFollowerActions(ObsGameState state) {
        List<Action> actions = new ArrayList<Action>();

        ObsGameFollowerAction.FollowerActionType type = null;
        if (state.timeStep == 0 ) {
            type = ObsGameFollowerAction.FollowerActionType.OBSERVE;
        } else if (state.timeStep >= ObsGameInfo.attackAfterTimeStep) {
            type = ObsGameFollowerAction.FollowerActionType.ATTACK;
            actions.add(new ObsGameFollowerAction(ObsGameFollowerAction.FollowerActionType.WAIT,0,getAlgorithmConfig().getInformationSetFor(state)));
        }
        if (type == null)
            throw new IllegalStateException("Attacker is not supposed to act here.");
        for (int i=0; i<ObsGameInfo.width; i++) {
            actions.add(new ObsGameFollowerAction(type,i,getAlgorithmConfig().getInformationSetFor(state)));
        }

        return actions;
    }

    private List<Action> getLeaderActions(ObsGameState state) {
        List<Action> actions = new ArrayList<Action>();
        int start = Math.max(0,state.defenderRow-2);
        int end =  Math.min(ObsGameInfo.width,state.defenderRow+3);
        if (state.timeStep == 0) {
            start = 0;
            end = ObsGameInfo.width;
        }
        for (int i = start; i< end; i++) {
            actions.add(new ObsGameLeaderAction(state.defenderRow,i,getAlgorithmConfig().getInformationSetFor(state)));
        }
        return actions;
    }

}
