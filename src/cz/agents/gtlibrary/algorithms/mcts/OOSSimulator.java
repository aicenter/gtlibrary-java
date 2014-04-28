/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class OOSSimulator implements Simulator {
    final private Random rnd;
    final private Expander expander;

    public OOSSimulator(Expander expander, long seed) {
        rnd = new HighQualityRandom(seed);
        this.expander = expander;
    }

    public OOSSimulator(Expander expander) {
         rnd = new HighQualityRandom();
        this.expander = expander;
    }

    public double playOutProb;
    public double playersProb;
    
    @Override
    public double[] simulate(GameState gameState) {
        //fact.l = (fact.bs == 1 && fact.us == 1 ? fact.s : fact.delta*fact.bs + (1-fact.delta)*fact.us);
        playOutProb = 1;
        playersProb = 1;

        GameStateImpl state = (GameStateImpl) gameState.copy();
        
        while (!state.isGameEnd()) {
                List<Action> actions = expander.getActions(new MCTSInformationSet(state));
                playOutProb *= 1.0/actions.size();
                if (!state.isPlayerToMoveNature()) {
                        playersProb *= 1.0/actions.size();
                }
                state.performActionModifyingThisState(actions.get(rnd.nextInt(actions.size())));
        }
        return state.getUtilities();
    }
    
    
}
