/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.OOSBackPropFactory;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class OOSSimulator extends DefaultSimulator {
    private OOSBackPropFactory fact;
    final private Random rnd;

    public OOSSimulator(OOSBackPropFactory fact, long seed) {
        rnd = new HighQualityRandom(seed);
        this.fact = fact;
    }

    public OOSSimulator(OOSBackPropFactory fact) {
         rnd = new HighQualityRandom();
        this.fact = fact;
    }

    @Override
    public double[] simulate(GameState gameState, Expander<MCTSInformationSet> expander) {
        fact.l = fact.s;
        fact.x = 1;

        GameStateImpl state = (GameStateImpl) gameState.copy();
        
        while (!state.isGameEnd()) {
                List<Action> actions = expander.getActions(new MCTSInformationSet(state));
                fact.l *= 1.0/actions.size();
                if (!state.isPlayerToMoveNature()) {
                        fact.x *= 1.0/actions.size();
                }
                state.performActionModifyingThisState(actions.get(rnd.nextInt(actions.size())));
        }
        return state.getUtilities();
    }
    
    
}
