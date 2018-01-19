package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jakub Cerny on 07/11/2017.
 */
public class TLExpander extends ExpanderImpl {

    HashMap<GameState, ArrayList<GameState>> temporaryLeaves;
    Expander expander;

    public TLExpander(AlgorithmConfig algConfig, HashMap tLs, Expander expdr) {
        super(algConfig);
        temporaryLeaves = tLs;
        expander = expdr;
    }

    public Expander getExpander(){return expander;}

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.isGameEnd()) return new ArrayList<>();
        if(!temporaryLeaves.containsKey(gameState)) return expander.getActions(gameState);
        else {
            ArrayList<Action> actions = new ArrayList<>();
            for (int i = 0; i < temporaryLeaves.get(gameState).size(); i++) {
                if (temporaryLeaves.get(gameState).get(i) instanceof TLGameState)
                    actions.add(new TLAction(getAlgorithmConfig().getInformationSetFor(gameState), i, temporaryLeaves, ((TLGameState)temporaryLeaves.get(gameState).get(i)).gameState));
                else
                    actions.add(new TLAction(getAlgorithmConfig().getInformationSetFor(gameState), i, temporaryLeaves, temporaryLeaves.get(gameState).get(i)));

            }
            return actions;
        }
    }

}
