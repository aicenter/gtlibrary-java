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
    final private int simLenght;

    public OOSSimulator(int simLength, Expander expander, Random random) {
        rnd = random;
        this.expander = expander;
        this.simLenght = simLength;
    }
    
    public OOSSimulator(Expander expander, Random random) {
        this(Integer.MAX_VALUE, expander, random);
    }

    public OOSSimulator(Expander expander) {
        this(Integer.MAX_VALUE, expander, new HighQualityRandom());
    }
    
    public OOSSimulator(Expander expander, long seed) {
        this(Integer.MAX_VALUE, expander, new HighQualityRandom(seed));
    }

    public double playOutProb;
    public double playersProb;
    
    @Override
    public double[] simulate(GameState gameState) {
        //fact.l = (fact.bs == 1 && fact.us == 1 ? fact.s : fact.delta*fact.bs + (1-fact.delta)*fact.us);
        playOutProb = 1;
        playersProb = 1;

        GameStateImpl state = (GameStateImpl) gameState.copy();
        
        int step=0;
        while (!state.isGameEnd()) {
                if (step==simLenght) return state.evaluate();
                List<Action> actions = expander.getActions(new MCTSInformationSet(state));
                playOutProb *= 1.0/actions.size();//TODO: use correct chance probability
                if (!state.isPlayerToMoveNature()) {
                        playersProb *= 1.0/actions.size();
                }
                state.performActionModifyingThisState(actions.get(rnd.nextInt(actions.size())));
                step++;
        }
        return state.getUtilities();
    }
    
    
}
